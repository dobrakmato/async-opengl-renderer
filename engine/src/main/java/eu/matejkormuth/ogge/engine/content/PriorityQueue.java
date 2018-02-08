package eu.matejkormuth.ogge.engine.content;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
final class PriorityQueue<T> {

    private static final int PRIORITIES_COUNT = Priority.values().length;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(false);
    private final ArrayDeque<T>[] queues;

    public PriorityQueue(int size) {
        @SuppressWarnings("unchecked")
        final ArrayDeque<T>[] queues = (ArrayDeque<T>[])
                Array.newInstance(ArrayDeque.class, PRIORITIES_COUNT);

        this.queues = queues;
        for (int i = 0; i < PRIORITIES_COUNT; i++) {
            this.queues[i] = new ArrayDeque<>(size);
        }
    }

    public void put(T item) {
        put(item, Priority.NORMAL);
    }

    public void put(T item, Priority priority) {
        readWriteLock.writeLock().lock();

        ArrayDeque<T> queue = queues[priority.getId()];
        log.info("Queueing {}...", item);
        queue.addLast(item);
        readWriteLock.writeLock().unlock();
    }

    public boolean isEmpty() {
        readWriteLock.readLock().lock();
        try {
            return queues[1].isEmpty() && queues[0].isEmpty() && queues[2].isEmpty() && queues[3].isEmpty();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public int length() {
        readWriteLock.readLock().lock();
        try {
            return queues[0].size() + queues[1].size() + queues[2].size() + queues[3].size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public T poll() {
        readWriteLock.writeLock().lock();
        try {
            T item = queues[3].poll(); // CRITICAL
            if (item != null) {
                return item;
            }
            item = queues[2].poll(); // HIGH
            if (item != null) {
                return item;
            }
            item = queues[1].poll(); // NORMAL
            if (item != null) {
                return item;
            }
            return queues[0].poll(); // LOW
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
