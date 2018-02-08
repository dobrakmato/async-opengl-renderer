package eu.matejkormuth.ogge.engine;

import java.lang.annotation.*;

/**
 * Denotes that this resource will be automatically free()-d by called method or class..
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.METHOD})
public @interface AutoFreed {
}
