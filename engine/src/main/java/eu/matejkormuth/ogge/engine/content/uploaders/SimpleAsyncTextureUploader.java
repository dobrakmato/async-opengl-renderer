package eu.matejkormuth.ogge.engine.content.uploaders;

import eu.matejkormuth.ogge.engine.Application;
import eu.matejkormuth.ogge.engine.ThreadChecks;
import eu.matejkormuth.ogge.engine.ThreadSafe;
import eu.matejkormuth.ogge.engine.content.Request;
import eu.matejkormuth.ogge.engine.content.RequestState;
import eu.matejkormuth.ogge.engine.content.Uploader;
import eu.matejkormuth.ogge.engine.gl.api.BufferObject;
import eu.matejkormuth.ogge.engine.gl.api.Texture2D;
import eu.matejkormuth.ogge.engine.gl.api.UsageHint;
import eu.matejkormuth.ogge.engine.image.SimpleImage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

import static eu.matejkormuth.ogge.engine.gl.api.BufferObjectTarget.PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;

@Slf4j
@ThreadSafe
public class SimpleAsyncTextureUploader implements Uploader<Texture2D, SimpleImage> {

    private long pboLastUsed = 0;
    private int pboSize = 0;
    private BufferObject pbo = null;

    public BufferObject getPBO(int bytes) {
        if (pbo == null) {
            createPBO();
        }

        if (bytes > pboSize) {
            resizePBO(pboSize);
        }

        pboLastUsed = System.currentTimeMillis();
        return pbo;
    }

    private void resizePBO(int pboSize) {
        this.pboSize = pboSize;
        pbo.allocate(pboSize, UsageHint.STREAM_DRAW);
    }

    private void createPBO() {
        pbo = BufferObject.create();
    }

    @Override
    public RequestState upload(Request<Texture2D, SimpleImage> request, @Nonnull SimpleImage decoded) throws Exception {
        ThreadChecks.runOnGLUploadThread(() -> {
            final ByteBuffer pixels = decoded.getPixels();
            final Texture2D texture = Texture2D.create();
            final BufferObject pbo = BufferObject.create();

            final int bytes = pixels.remaining();
            final int width = decoded.getWidth();
            final int height = decoded.getHeight();
            final int internalFormat = decoded.getFormat().getGLInternalFormat();
            final int format = decoded.getFormat().getGLFormat();

            texture.createImageData(width, height, internalFormat, 1);

            pbo.allocate(bytes, UsageHint.STREAM_DRAW);
            log.trace("Copying to PBO {} bytes...", bytes);
            boolean success = pbo.map(GL_WRITE_ONLY, (glBuf) -> glBuf.put(pixels).flip());
            if (!success) {
                // shit. buffer corrupted. what now?
                log.error("Buffer corrupted during mapping!");
            }

            texture.bind();
            pbo.bind(PIXEL_UNPACK_BUFFER);
            texture.setImageData(width, height, 0, 0, 0, format, 0L);
            pbo.unbind(PIXEL_UNPACK_BUFFER);

            texture.generateMipmaps();

            Application.getFenceSync().insertFence(() -> {
                log.trace("Disposing PBO...");


                // decoded.dispose();

                request.complete(texture);
            });
        });
        return RequestState.UPLOADING_ASYNC;
    }
}
