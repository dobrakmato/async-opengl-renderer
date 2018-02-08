package eu.matejkormuth.ogge.engine;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL32;

import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL32.*;

@Slf4j
public final class FenceSync {

    private final ReentrantLock lock = new ReentrantLock();
    private final TLongObjectHashMap<Runnable> callbacks = new TLongObjectHashMap<>();

    FenceSync() {
    }

    public void insertFence(Runnable callback) {
        ThreadChecks.ensureGLThread();

        lock.lock();
        long fence = glFenceSync(GL32.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        log.trace("Fence {} inserted with callback {}.", fence, callback);
        callbacks.put(fence, callback);
        lock.unlock();
    }

    public void runFenceCallbacks() {
        ThreadChecks.ensureGLThread();

        lock.lock();
        for (TLongObjectIterator<Runnable> it = callbacks.iterator(); it.hasNext(); ) {
            it.advance();
            boolean signaled = checkSignaled(it.key());

            if (signaled) {
                glDeleteSync(it.key());
                log.trace("Fence {} signaled. Executing callback.", it.key());
                try {
                    it.value().run();
                } catch (Exception e) {
                    log.error("Error while executing callback. ", e);
                }
                it.remove();
            }
        }
        lock.unlock();
    }

    private boolean checkSignaled(long fence) {
        int state = glClientWaitSync(fence, 0, 0);
        return (state == GL_ALREADY_SIGNALED || state == GL_CONDITION_SATISFIED);
    }
}
