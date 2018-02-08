package eu.matejkormuth.ogge.engine.image.bif;

import eu.matejkormuth.ogge.engine.Disposable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Data
@RequiredArgsConstructor
@Slf4j
public class AdvancedImage implements Disposable {

    private final Consumer<AdvancedImage> deallocator;
    private final int width;
    private final int height;
    private final Format format;

    private final int mipmapLevels;
    private final int cubemapFaces;

    private final /* malloc()-ated */ ByteBuffer pixels; // used if has no mipmaps
    private final /* malloc()-ated */ ByteBuffer mipmaps[]; // used if has mipmaps
    private final /* malloc()-ated */ ByteBuffer faces[]; // used if cubemap

    private transient boolean disposed = false;

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
