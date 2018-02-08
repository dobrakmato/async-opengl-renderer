package eu.matejkormuth.ogge.engine;

import eu.matejkormuth.ogge.engine.gl.DebugMessageCallback;
import eu.matejkormuth.ogge.engine.gl.GLUploaderThread;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.nio.IntBuffer;
import java.util.Locale;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

@Slf4j
public final class Window implements Disposable {

    @Slf4j
    public static class GLFW {

        static GLFWErrorCallback errorCallback;

        static boolean initialized = false;

        public static synchronized void ensureInitialized() {
            if (!initialized) {
                initialized = true;
                initialize();
            }
        }

        private static void initialize() {
            log.debug("Initializing GLFW...");
            errorCallback = GLFWErrorCallback.createPrint(System.err).set();

            if (!glfwInit())
                throw new IllegalStateException("Unable to initialize GLFW");
        }

        public static synchronized void ensureDestroyed() {
            log.debug("Destroying GLFW...");

            glfwTerminate();
            glfwSetErrorCallback(null).free();
            errorCallback.free();
        }

    }

    @Slf4j
    public static class DebugRenderer {

        static class DebugInformation {

            double frameMs;
            long frames;
            double totalTime;
        }

        @Setter
        @Getter
        private DebugInformation info;

        private NanoVG vg;

        private NVGColor white;
        private NVGColor black;

        public DebugRenderer(int width, int height) {
            log.info("Creating debug renderer using NanoVG");

            vg = new NanoVG(width, height);
            vg.createFont("DejaVuSansMono", "C:\\Users\\Matej\\Downloads\\dejavu-fonts-ttf-2.37\\dejavu-fonts-ttf-2.37\\ttf\\DejaVuSansMono.ttf");
            white = vg.createColor(1, 1, 1);
            black = vg.createColor(0, 0, 0);
        }

        public void render() {
            vg.beginFrame();
            vg.fontFace("DejaVuSansMono");
            vg.fontSize(14f);
            vg.fillColor(white);
            vg.textMultiline(8, 16, String.format(Locale.ENGLISH, "Frame: %.2fms, %.2fms\nFps: %.2f, %.2f\nMem: %.0f/%.0f MB",
                    info.totalTime / info.frames,
                    info.frameMs,
                    1000 / (info.totalTime / info.frames),
                    1000 / info.frameMs,
                    (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1_000_000f,
                    Runtime.getRuntime().totalMemory() / 1_000_000f));
            vg.endFrame();
        }

        public void cleanUp() {
            vg.dispose();
        }

    }

    private long handle;
    long frames;

    private int width;
    private int height;
    private boolean shouldRun = true;
    private boolean loopStarted = false;
    @Setter
    private Renderer renderer;

    private DebugRenderer debugRenderer;
    private Runnable onCloseListener;
    private GLUploaderThread glUploaderThread;
    private GLDebugMessageCallback glDebugMessageCallback;
    private final FenceSync fenceSync = new FenceSync();
    private final DebugRenderer.DebugInformation info = new DebugRenderer.DebugInformation();
    private final DebugMessageCallback debugMessageCallback = new DebugMessageCallback();


    public Window(int width, int height, boolean fullscreen, boolean resizable, boolean vsync, String title, int msaa) {
        this.width = width;
        this.height = height;

        if (!Window.GLFW.initialized) {
            throw new IllegalStateException("GLFW has not been initialized!");
        }

        this.initialize(width, height, fullscreen, resizable, vsync, title, msaa);
    }

    private void initialize(int width, int height, boolean fullscreen, boolean resizable, boolean vsync, String title, int msaa) {
        log.debug("Creating new Window (width={}, height={}, fullscreen={}, resizable={}, vsync={}, title={}).",
                width, height, fullscreen, resizable, vsync, title);

        ThreadChecks.glRenderThread = Thread.currentThread();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, msaa);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);

        long monitor = glfwGetPrimaryMonitor();

        handle = glfwCreateWindow(width, height, title, fullscreen ? monitor : 0, 0);
        if (handle == 0)
            throw new RuntimeException("Failed to create the GLFW window");

        glUploaderThread = new GLUploaderThread(handle);
        glUploaderThread.start();

        ThreadChecks.glUploadThread = glUploaderThread.getThread();

        synchronized (glUploaderThread.getMonitor()) { // totally safe
            try {
                log.trace("Waiting for debug thread and context to create.");
                glUploaderThread.getMonitor().wait();
                log.trace("Continuing with initialization.");
            } catch (InterruptedException ignored) {
            }
        }

        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                close();
            }
        });

        this.centerWindow(monitor);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(vsync ? 1 : 0);

        // this only initializes LWJGL with already created context
        log.debug("Creating OpenGL capabilities...");
        GL.createCapabilities();

        this.show();
    }

    void loop() {
        if (loopStarted) {
            throw new IllegalStateException("Loop already running");
        }
        loopStarted = true;


        if (Application.DEBUG) {
            if (Application.DEBUG_RENDERER) {
                debugRenderer = new DebugRenderer(width, height);
                debugRenderer.setInfo(info);
            }

            GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS); // work around stuck thread issue
            glDebugMessageCallback = GLDebugMessageCallback.create(debugMessageCallback);
            GL43.glDebugMessageCallback(glDebugMessageCallback, 0);


            // Now setup and pass the callback to GL.
            //GLUtil.setupDebugMessageCallback();
        }

        renderer.initialize();

        long frameStart, frameTime;
        while (shouldRun) {
            frameStart = System.nanoTime();

            // Check fence state.
            fenceSync.runFenceCallbacks();

            renderer.render();

            if (debugRenderer != null) {
                debugRenderer.render();
            }

            glfwSwapBuffers(handle);
            glfwPollEvents();

            frameTime = System.nanoTime() - frameStart;
            frames++;
            if (debugRenderer != null) {
                double frameTimeMs = frameTime / 1_000_000f;
                debugRenderer.info.frameMs = frameTimeMs;
                debugRenderer.info.totalTime += frameTimeMs;
                debugRenderer.info.frames++;
            }
        }

        if (Application.DEBUG) {
            GL43.glDebugMessageCallback(null, 0);
            glDebugMessageCallback.free();
        }

        glUploaderThread.stop();

        renderer.cleanUp();

        if (debugRenderer != null) {
            debugRenderer.cleanUp();
        }

        log.debug("Closing window...");
        glfwSetWindowShouldClose(handle, true);
        dispose();


        if (onCloseListener != null) {
            onCloseListener.run();
        }
    }

    private void show() {
        glfwShowWindow(handle);
    }

    public void close() {
        shouldRun = false;
    }

    private void centerWindow(long monitor) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(handle, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(monitor);

            // Center the window
            glfwSetWindowPos(
                    handle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
    }

    public void setOnCloseListener(Runnable onCloseListener) {
        this.onCloseListener = onCloseListener;
    }

    FenceSync getFenceSync() {
        return fenceSync;
    }

    @ThreadSafe
    public void addRunOnGLUploadThreadTask(@Nonnull Runnable runnable) {
        glUploaderThread.add(runnable);
    }

    @Override
    public void dispose() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }

    public static class Builder {
        private int width = 1280;
        private int height = 720;
        private int msaa = 0;
        private boolean fullscreen = false;
        private boolean resizable = false;
        private boolean vsync = true;
        private String title = "Window";

        public Window build() {
            return new Window(width, height, fullscreen, resizable, vsync, title, msaa);
        }

        public Builder setMsaa(int msaa) {
            this.msaa = msaa;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            return this;
        }

        public Builder setResizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder setVsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
    }
}
