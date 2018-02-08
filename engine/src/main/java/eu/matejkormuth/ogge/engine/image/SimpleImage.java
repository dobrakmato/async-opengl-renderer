package eu.matejkormuth.ogge.engine.image;

import eu.matejkormuth.ogge.engine.Deallocatable;
import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.image.bif.Format;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Simple one layer raster image.
 */
@Data
@RequiredArgsConstructor
@Slf4j
public class SimpleImage implements Disposable, Deallocatable<SimpleImage> {

    private final Consumer<SimpleImage> deallocator;
    private final int width;
    private final int height;
    private final Format format;
    private final /* malloc()-ated */ ByteBuffer pixels;

    private transient boolean disposed = false;

    @Override
    public Consumer<SimpleImage> deallocator() {
        return deallocator;
    }

    @Override
    public void dispose() {
        if (disposed) {
            log.debug("Tried to dispose already disposed {}", this);
            return;
        }

        deallocator.accept(this);
        disposed = true;
    }
}
