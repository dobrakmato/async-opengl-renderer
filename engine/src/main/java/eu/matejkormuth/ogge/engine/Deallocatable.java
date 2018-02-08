package eu.matejkormuth.ogge.engine;

import java.util.function.Consumer;

public interface Deallocatable<T> {
    Consumer<T> deallocator();
}
