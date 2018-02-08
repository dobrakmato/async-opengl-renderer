package eu.matejkormuth.ogge.engine;

import java.lang.annotation.*;

/**
 * Denotes that this method is thread-safe.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ThreadSafe {
}
