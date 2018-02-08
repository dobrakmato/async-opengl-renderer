package eu.matejkormuth.ogge.engine.gl;

import eu.matejkormuth.ogge.engine.Application;
import eu.matejkormuth.ogge.engine.Stoppable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL;

import javax.annotation.Nonnull;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

@Slf4j
public final class GLUploaderThread implements Stoppable {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition workAvailable = lock.newCondition();
    private final ArrayBlockingQueue<Runnable> tasks = new ArrayBlockingQueue<>(512);
    @Getter
    private final Object monitor = new Object();

    @Getter
    private final Thread thread = new Thread(this::run);

    /* Handle to main (Renderer) window. Used to create context with shared resources */
    private final long parentHandle;
    /* Handle of this context window. */
    private long thisHandle;

    private boolean shouldRun = true;

    public GLUploaderThread(long shareHandle) {
        this.parentHandle = shareHandle;
    }

    public void start() {
        thread.setName("GL/Uploader #1");
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(Application.getUncaughtExceptionHandler());
        thread.start();
    }

    private void wakeUp() {
        lock.lock();
        workAvailable.signal();
        lock.unlock();
    }

    public void add(@Nonnull Runnable runnable) {
        tasks.add(runnable);
        wakeUp();
    }

    private void run() {
        log.debug("Starting GLUploaderThread...");

        // Create hidden window to create OpenGL context with shared resources.
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        thisHandle = glfwCreateWindow(16, 16, "Upload Thread", 0, parentHandle);

        if (thisHandle == 0) {
            throw new RuntimeException("Can't create another window/context! Error: " + glfwGetError());
        }

        // Notify caller thread to continue.
        synchronized (monitor) {
            monitor.notifyAll();
        }

        // Make thisHandle's context current to this thread.
        glfwMakeContextCurrent(thisHandle);

        // Init LWJGL.
        log.debug("Creating OpenGL capabilities...");
        GL.createCapabilities();

        while (shouldRun) {
            if (tasks.isEmpty()) {
                try {
                    log.trace("GL/Uploader waiting...");
                    lock.lock();
                    workAvailable.await();
                    lock.unlock();
                    log.trace("GL/Uploader resumed...");
                } catch (InterruptedException ignored) {
                }
            }

            Runnable task = tasks.poll();
            if (task != null) {
                work(task);
            }
        }

        glfwSetWindowShouldClose(thisHandle, true);
        glfwFreeCallbacks(thisHandle);
        glfwDestroyWindow(thisHandle);

        log.debug("Stopping GLUploaderThread...");
    }


    private void work(Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("Exception while executing task: ", e);
        }
    }

    @Override
    public void stop() {
        log.debug("Stopping GLUploaderThread...");
        shouldRun = false;
        wakeUp();
    }
}
