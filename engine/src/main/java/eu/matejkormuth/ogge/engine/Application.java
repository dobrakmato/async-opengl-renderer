package eu.matejkormuth.ogge.engine;

import eu.matejkormuth.ogge.engine.content.Content;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ThreadFactory;

@Slf4j
public final class Application {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 1024;
    public static final boolean FULLSCREEN = false;
    public static final boolean VSYNC = true;
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_RENDERER = true;
    public static final boolean REQUEST_TIMING = true;

    private static Application instance;
    private ThreadFactory threadFactory = Thread::new;
    private Window window;
    private boolean exitRequested = false;

    public Application() {
        if (instance != null) {
            throw new RuntimeException("Only one instance of Application is allowed!");
        }
        instance = this;
        Thread.currentThread().setName("Main");
    }

    public static long getFrames() {
        return instance.window.frames;
    }

    public void start() {
        startDaemon("GL/Renderer", () -> {
            Window.GLFW.ensureInitialized();
            window = new Window.Builder()
                    .setWidth(WIDTH)
                    .setHeight(HEIGHT)
                    .setFullscreen(FULLSCREEN)
                    .setVsync(VSYNC)
                    .setTitle("Engine")
                    .build();

            window.setOnCloseListener(this::requestExit);
            window.setRenderer(new Renderer());
            window.loop();
            Window.GLFW.ensureDestroyed();
        });

        startDaemon("ContentInit", Content::initialize);

        this.keepMainThreadAlive();
    }

    private void keepMainThreadAlive() {
        synchronized (this) {
            while (!exitRequested) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        this.cleanUp();
    }

    private void cleanUp() {
        log.info("Cleaning up...");

        Content.shutdown();

        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();

        long nonDaemon = threads.keySet()
                .stream()
                .filter(thread -> !thread.isDaemon())
                .count();

        log.warn("{} non-daemon threads still running!", nonDaemon);
        if (nonDaemon > 1) {
            for (Map.Entry<Thread, StackTraceElement[]> entry : threads.entrySet()) {
                Thread thread = entry.getKey();
                StackTraceElement[] stackTrace = entry.getValue();

                log.warn("");
                log.warn("Thread ID: {}\tName: {}\tDaemon: {}", thread.getId(), thread.getName(), thread.isDaemon());
                log.warn("Stack trace:");
                for (StackTraceElement element : stackTrace) {
                    log.warn("\t{}", element);
                }
            }
        }

        log.info("Exiting...");
    }

    public void requestExit() {
        log.info("Exit requested.");
        synchronized (this) {
            exitRequested = true;
            this.notifyAll();
        }
    }

    private void startDaemon(String threadName, Runnable runnable) {
        start(threadName, runnable, true);
    }

    private void start(String threadName, Runnable runnable) {
        start(threadName, runnable, false);
    }

    private void start(String threadName, Runnable runnable, boolean daemon) {
        log.info("Starting {} thread...", threadName);
        Thread thread = threadFactory.newThread(runnable);
        thread.setName(threadName);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(this::uncaughtExceptionHandler);
        thread.start();
    }

    private void uncaughtExceptionHandler(Thread t, Throwable e) {
        log.error("Error in thread " + t.getName() + ": {}", e);
        requestExit();
    }

    public static Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return instance::uncaughtExceptionHandler;
    }

    public static FenceSync getFenceSync() {
        return instance.window.getFenceSync();
    }

    static Window getWindow() {
        return instance.window;
    }
}
