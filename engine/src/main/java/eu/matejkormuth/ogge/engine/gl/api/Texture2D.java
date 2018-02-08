package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import eu.matejkormuth.ogge.engine.MathUtils;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.*;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

@ToString
@Slf4j
public class Texture2D implements Disposable, Labelable {

    private static float MAX_ANISOTROPY = glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);

    static {
        if (MAX_ANISOTROPY > 0) {
            log.info("Anisotropic filtering ({}x) available!", MAX_ANISOTROPY);
        } else {
            log.warn("Anisotropic filtering not available.");
        }
    }

    @Getter
    private int name;
    @Getter
    private String label;
    private boolean textureStorageCreated = false;

    private Texture2D(int name) {
        this.name = name;
    }

    public static Texture2D create() {
        int newName = GL45.glCreateTextures(GL_TEXTURE_2D);
        return new Texture2D(newName);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, name);
    }

    public static void activateSampler(int sampler) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + sampler);
    }

    public void generateMipmaps() {
        GL45.glGenerateTextureMipmap(name);
    }

    public void enableMaxAF() {
        if (MAX_ANISOTROPY != 0.0f) {
            GL45.glTextureParameterf(name, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, MAX_ANISOTROPY);
        }
    }

    public void setWraps(WrapMode horizontal, WrapMode vertical) {
        GL45.glTextureParameterIi(name, GL11.GL_TEXTURE_WRAP_S, horizontal.getGLConstant());
        GL45.glTextureParameterIi(name, GL11.GL_TEXTURE_WRAP_T, vertical.getGLConstant());
    }

    public void setFilters(FilterMode minFilter, FilterMode magFilter) {
        if (magFilter.needsMipmaps()) {
            throw new IllegalArgumentException("Mag filter cannot use mipmaps!");
        }

        GL45.glTextureParameterIi(name, GL11.GL_TEXTURE_MIN_FILTER, minFilter.getGLConstant());
        GL45.glTextureParameterIi(name, GL11.GL_TEXTURE_MAG_FILTER, magFilter.getGLConstant());
    }

    public void createImageData(int width, int height, int internalFormat) {
        textureStorageCreated = true;

        int levels = (int) (MathUtils.log2(width) + 1);
        createImageData(width, height, internalFormat, levels);
    }

    public void createImageData(int width, int height, int internalFormat, int levels) {
        GL45.glTextureStorage2D(name, levels, internalFormat, width, height);
    }

    public void setImageData(int width, int height, int level, int format, ByteBuffer pixels) {
        setImageData(width, height, level, 0, 0, format, pixels);
    }

    public void setImageData(int width, int height, int level, int xOffset, int yOffset, int format, ByteBuffer pixels) {
        GL45.glTextureSubImage2D(name, level, xOffset, yOffset, width, height, format, GL_UNSIGNED_BYTE, pixels);
    }

    public void setImageData(int width, int height, int level, int format, long offset) {
        setImageData(width, height, level, 0, 0, format, offset);
    }

    // For PBOs
    public void setImageData(int width, int height, int level, int xOffset, int yOffset, int format, long offset) {
        GL45.glTextureSubImage2D(name, level, xOffset, yOffset, width, height, format, GL_UNSIGNED_BYTE, offset);
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL_TEXTURE, name, label == null ? "" : label.substring(0, 255));
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Texture already disposed");
        }

        glDeleteTextures(name);
        name = -1;
        label = null;
    }
}
