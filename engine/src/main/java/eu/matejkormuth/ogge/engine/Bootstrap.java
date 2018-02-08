package eu.matejkormuth.ogge.engine;

import org.lwjgl.system.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Bootstrap {
    public static void main(String[] args) {
        System.out.println("Application started at: " + new SimpleDateFormat().format(new Date()));

        if (Application.DEBUG) {
            Configuration.DEBUG.set(true);
            Configuration.DEBUG_STACK.set(true);
            Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
            Configuration.GLFW_CHECK_THREAD0.set(true);
        }
        new Application().start();
    }
}
