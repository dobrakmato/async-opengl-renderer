package eu.matejkormuth.ogge.engine;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadChecks {

    static Thread glRenderThread;
    static Thread glUploadThread;

    public void ensureGLRenderThread() {
        if (Thread.currentThread() != glRenderThread) {
            throw new IllegalStateException("This method must be called from GL render thread");
        }
    }

    public void ensureGLUploadThread() {
        if (Thread.currentThread() != glRenderThread) {
            throw new IllegalStateException("This method must be called from GL uploader thread");
        }
    }

    public void ensureGLThread() {
        final Thread t = Thread.currentThread();
        if (t != glRenderThread && t != glUploadThread) {
            throw new IllegalStateException("This method must be called from GL thread");
        }
    }

    public void runOnGLRenderThread(Runnable runnable) {
        throw new UnsupportedOperationException("not yet implemeted!");
    }

    public void runOnGLUploadThread(Runnable runnable) {
        Application.getWindow().addRunOnGLUploadThreadTask(runnable);
    }

}
