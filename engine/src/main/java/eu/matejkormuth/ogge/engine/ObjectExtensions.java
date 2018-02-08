package eu.matejkormuth.ogge.engine;

public class ObjectExtensions {
    public static <T> T or(T object, T ifNull) {
        return object != null ? object : ifNull;
    }
}