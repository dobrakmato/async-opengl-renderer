package eu.matejkormuth.ogge.engine.content;

import eu.matejkormuth.ogge.engine.Stoppable;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

@Slf4j
class ContentLoader implements Stoppable {

    private final PriorityQueue<Request<?, ?>> ioQueue = new PriorityQueue<>(1024);
    private final PriorityQueue<Request<?, ?>> workerQueue = new PriorityQueue<>(1024);

    private final DiskLoader loader = new DiskLoader(this, ioQueue);
    private final WorkerGroup workers = new WorkerGroup(workerQueue, 4);

    public void start() {
        loader.start();
        workers.start();
    }

    public void addRequest(@Nonnull Request<?, ?> request) {
        if (request.timing != null) {
            request.timing.markQueueEnter();
        }

        if (request.getState() == RequestState.CREATED) {
            ioQueue.put(request, request.getPriority());
            loader.wakeUp();
        } else {
            workerQueue.put(request, request.getPriority());
            workers.wakeUp();
        }
    }

    @Override
    public void stop() {
        log.debug("Exiting ContentLoader and abandoning all work...");
        loader.stop();
        workers.stop();
    }
}
