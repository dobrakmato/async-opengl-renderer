package eu.matejkormuth.ogge.engine.content.decoders;

import eu.matejkormuth.ogge.engine.AutoFreed;
import eu.matejkormuth.ogge.engine.ThreadSafe;
import eu.matejkormuth.ogge.engine.content.Decoder;
import eu.matejkormuth.ogge.engine.content.Request;
import eu.matejkormuth.ogge.engine.gl.api.Texture2D;
import eu.matejkormuth.ogge.engine.image.SimpleImage;
import eu.matejkormuth.ogge.engine.image.bif.Format;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

@Slf4j
public final class STBImageDecoder implements Decoder<Texture2D, SimpleImage> {

    private final Consumer<SimpleImage> SIMPLE_IMAGE_DEALLOCATOR =
            (image) -> STBImage.stbi_image_free(image.getPixels());

    public STBImageDecoder() {
        // Force load of STB library.
        try {
            int[] a = null;
            STBImage.stbi_info("", a, a, a);
        } catch (Exception ignored) {
        }
    }

    @ThreadSafe
    @Override
    public SimpleImage decode(Request<Texture2D, SimpleImage> request, @AutoFreed ByteBuffer read) throws Exception {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.callocInt(1);
            IntBuffer height = stack.callocInt(1);
            IntBuffer channels = stack.callocInt(1);

            read.flip();

            ByteBuffer pixels = STBImage.stbi_load_from_memory(read, width, height, channels, 0);

            if (pixels == null) {
                throw new RuntimeException("Can't decode image!");
            } else {
                log.trace("[STBImageDecoder] Image: width: {}, height: {}, channels: {}, pixels: {}", width.get(0), height.get(0), channels.get(0), pixels.remaining());
            }

            return new SimpleImage(SIMPLE_IMAGE_DEALLOCATOR, width.get(0), height.get(0), Format.SRGB8, pixels);
        }
    }

}
