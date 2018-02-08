package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Application;
import eu.matejkormuth.ogge.engine.Stoppable;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
final class WorkerGroup implements Stoppable {

    private final PriorityQueue<Request<?, ?>> workerQueue;
    private final Thread[] workers;
    private final ReentrantLock[] locks;
    private final Condition[] workAvailable;

    private boolean shouldRun = true;

    public WorkerGroup(PriorityQueue<Request<?, ?>> workerQueue, int workers) {
        this.workerQueue = workerQueue;

        this.workers = new Thread[workers];
        this.locks = new ReentrantLock[workers];
        this.workAvailable = new Condition[workers];

        for (int i = 0; i < workers; i++) {
            final int workerIndex = i;
            this.workers[i] = new Thread(() -> worker(workerIndex), "Content/Worker #" + i);
            this.workers[i].setUncaughtExceptionHandler(Application.getUncaughtExceptionHandler());

            this.locks[i] = new ReentrantLock();
            this.workAvailable[i] = this.locks[i].newCondition();
        }
    }

    void start() {
        log.debug("Starting WorkerGroup...");
        for (Thread worker : workers) {
            worker.setDaemon(true);
            worker.start();
        }
    }

    void wakeUp() {
        int workersToWakeUp = Math.min(workerQueue.length(), workers.length);
        for (int i = 0; i < workersToWakeUp; i++) {
            locks[i].lock();
            workAvailable[i].signal();
            locks[i].unlock();
        }
    }

    private void worker(int index) {
        log.debug("Starting Worker #{}...", index);
        while (shouldRun) {
            if (workerQueue.isEmpty()) {
                try {
                    log.trace("Worker #{} thread waiting...", index);
                    locks[index].lock();
                    workAvailable[index].await();
                    locks[index].unlock();
                    log.trace("Worker #{} thread resuming...", index);
                } catch (InterruptedException ignored) {
                }
            }

            Request<?, ?> work = workerQueue.poll();

            if (work != null) {
                if (work.timing != null) {
                    work.timing.markQueueLeave();
                }

                switch (work.getState()) {
                    case ERROR:
                        work.callErrorCallback();
                        break;
                    case UPLOAD_DONE:
                        work.callSuccessCallback();
                        break;
                    default:
                        work(work);
                        break;
                }
            }

        }
        log.debug("Stopping Worker #{}...", index);
    }

    private static <T, D> void work(Request<T, D> work) {
        long decodeStart = 0;
        if (work.timing != null) {
            decodeStart = System.nanoTime();
        }
        D decoded = workDecode(work);
        if (work.timing != null) {
            work.timing.decodeTime = (System.nanoTime() - decodeStart) / 1_000_000.0;
        }

        if (decoded != null) {
            log.trace("Decoded: {}. Uploading...", decoded);
            workUpload(work, decoded);
        } else {
            log.error("Decoded returned \"null\" for {}", work);
            work.state = RequestState.ERROR;
            work.error = "Decoder returned null.";
        }
    }

    private static <T, D> void workUpload(Request<T, D> work, D decoded) {
        Uploader<T, D> uploader = work.getUploader();
        try {
            long uploadSyncStart = 0;
            if (work.timing != null) {
                uploadSyncStart = System.nanoTime();
            }
            RequestState state = uploader.upload(work, decoded);
            if (work.timing != null) {
                work.timing.uploadSyncTime = (System.nanoTime() - uploadSyncStart) / 1_000_000.0;
                if (state == RequestState.UPLOADING_ASYNC) {
                    work.timing.markAsyncUploadStart();
                }
            }

            log.info("Request {} completed with state {}!", work, state);

            if (state == RequestState.UPLOAD_DONE) {
                work.state = RequestState.UPLOAD_DONE;
                work.callSuccessCallback();
            } else if (state == RequestState.UPLOADING_ASYNC) {
                work.state = RequestState.UPLOADING_ASYNC;
                // it's up to uploader to free this memory
            } else {
                throw new RuntimeException("Uploader returned wrong RequestState after upload!");
            }
        } catch (Exception e) {
            log.error("Exception while UPLOADING " + work.toString() + "!", e);
            work.state = RequestState.ERROR;
            work.error = e.getMessage() + " For more information check logs.";
            work.callErrorCallback();
        }
    }

    private static <T, D> D workDecode(Request<T, D> work) {
        D result = null;
        Decoder<T, D> decoder = work.getDecoder();
        try {
            result = decoder.decode(work, work.internalFileContents);
            work.state = RequestState.DECODED;
        } catch (Exception e) {
            log.error("Exception while DECODING " + work.toString() + "!", e);
            work.state = RequestState.ERROR;
            work.error = e.getMessage() + " For more information check logs.";
        } finally {
            if (work.internalFileContents != null) {
                MemoryUtil.memFree(work.internalFileContents);
            }
        }
        return result;
    }

    @Override
    public void stop() {
        log.debug("Stopping WorkerGroup...");
        this.shouldRun = false;
        wakeUp();
    }
}
