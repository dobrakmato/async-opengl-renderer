package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Application;
import eu.matejkormuth.ogge.engine.Stoppable;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
final class DiskLoader implements Stoppable {

    private final static long POLL_MAX_TIME = 1000L * 5;

    private final ContentLoader contentLoader;
    private final PriorityQueue<Request<?, ?>> ioQueue;
    private final ReentrantLock lock;
    private final Condition workAvailable;
    private final Thread thread;

    private boolean shouldRun = true;

    DiskLoader(ContentLoader contentLoader, PriorityQueue<Request<?, ?>> ioQueue) {
        this.thread = new Thread(this::run, "Content/Disk IO");
        this.contentLoader = contentLoader;
        this.ioQueue = ioQueue;

        this.lock = new ReentrantLock();
        this.workAvailable = lock.newCondition();
    }

    void start() {
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(Application.getUncaughtExceptionHandler());
        thread.start();
    }

    public void wakeUp() {
        lock.lock();
        workAvailable.signal();
        lock.unlock();
    }

    private void run() {
        log.debug("Starting DiskLoader...");
        while (shouldRun) {
            if (ioQueue.isEmpty()) {
                try {
                    log.trace("Content/Disk IO waiting...");
                    lock.lock();
                    workAvailable.await();
                    lock.unlock();
                    log.trace("Content/Disk IO resumed...");
                } catch (InterruptedException ignored) {
                }
            }

            Request<?, ?> work = ioQueue.poll();

            if (work != null) {
                long readStart = 0;
                if (work.timing != null) {
                    work.timing.markQueueLeave();
                    readStart = System.nanoTime();
                }
                try {
                    read(work);
                    work.state = RequestState.IO_DONE;
                } catch (Exception e) {
                    log.error("Exception while reading " + work.toString() + "!", e);
                    work.state = RequestState.ERROR;
                    work.error = e.getMessage() + " For more information check logs.";
                }
                if (work.timing != null) {
                    long took = System.nanoTime() - readStart;
                    work.timing.diskTime = (took / 1_000_000.0);
                }

                contentLoader.addRequest(work);
            }
        }
        log.debug("Stopping DiskLoader...");
    }

    private void read(Request<?, ?> work) throws IOException {
        long start = System.nanoTime();
        Path path = work.getPath();
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        long bytes = attrs.size();

        log.trace("Reading file {} ({}MB)...", work.getPath(), bytes / 1024 / 1024f);
        ByteBuffer buffer = MemoryUtil.memAlloc((int) bytes);
        if (buffer == null) {
            throw new RuntimeException("Can't allocate memory for load.");
        }

        try (FileChannel file = FileChannel.open(path, StandardOpenOption.READ)) {
            int read = 0;
            while (read < bytes) {
                read += file.read(buffer);
            }
        }

        log.trace("File read in {}ms!", (System.nanoTime() - start) / 1_000_000f);
        work.internalFileContents = buffer;
    }

    @Override
    public void stop() {
        log.debug("Stopping DiskLoader...");
        shouldRun = false;
        wakeUp();
    }
}
