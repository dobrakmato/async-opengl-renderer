package eu.matejkormuth.ogge.engine.content.uploaders;

import eu.matejkormuth.ogge.engine.ThreadChecks;
import eu.matejkormuth.ogge.engine.ThreadSafe;
import eu.matejkormuth.ogge.engine.content.Request;
import eu.matejkormuth.ogge.engine.content.RequestState;
import eu.matejkormuth.ogge.engine.content.Uploader;
import eu.matejkormuth.ogge.engine.gl.api.FilterMode;
import eu.matejkormuth.ogge.engine.gl.api.Texture2D;
import eu.matejkormuth.ogge.engine.gl.api.WrapMode;
import eu.matejkormuth.ogge.engine.image.SimpleImage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

@Slf4j
@ThreadSafe
public class SimpleSyncTextureUploader implements Uploader<Texture2D, SimpleImage> {

    @Override
    public RequestState upload(Request<Texture2D, SimpleImage> request, @Nonnull SimpleImage decoded) throws Exception {
        ThreadChecks.runOnGLUploadThread(() -> {
            final ByteBuffer pixels = decoded.getPixels();
            final Texture2D texture = Texture2D.create();

            final int width = decoded.getWidth();
            final int height = decoded.getHeight();
            final int internalFormat = decoded.getFormat().getGLInternalFormat();
            final int format = decoded.getFormat().getGLFormat();

            log.trace("Creating image data...");
            texture.createImageData(width, height, internalFormat, 1);

            log.trace("Uploading image data...");
            texture.setImageData(width, height, 0, format, pixels);

            texture.setFilters(FilterMode.NEAREST, FilterMode.NEAREST);
            texture.setWraps(WrapMode.CLAMP_TO_BORDER, WrapMode.CLAMP_TO_BORDER);

            log.trace("Generating mipmaps...");
            texture.generateMipmaps();
            //decoded.dispose();
            request.complete(texture);
        });
        return RequestState.UPLOADING_ASYNC;
    }
}
