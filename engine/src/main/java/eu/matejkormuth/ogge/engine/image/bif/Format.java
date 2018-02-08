package eu.matejkormuth.ogge.engine.image.bif;

import gnu.trove.map.hash.TIntObjectHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.lwjgl.opengl.*;

@Getter
public enum Format {
    /* LDR */
    RGB8(1, 3, 24, SizeCalculator.UNCOMPRESSED, GL11.GL_RGB, GL11.GL_RGB8),
    RGBA8(2, 4, 32, SizeCalculator.UNCOMPRESSED, GL11.GL_RGBA, GL11.GL_RGBA8),

    SRGB8(3, 3, 24, SizeCalculator.UNCOMPRESSED, GL11.GL_RGB, GL21.GL_SRGB8),
    SRGB8_A8(4, 4, 32, SizeCalculator.UNCOMPRESSED, GL11.GL_RGBA, GL21.GL_SRGB8_ALPHA8),

    /* LDR COMPRESSED */
    COMPRESSED_DXT1_RGB8(5, 3, -1, SizeCalculator.DXT1, GL11.GL_RGB, EXTTextureCompressionS3TC.GL_COMPRESSED_RGB_S3TC_DXT1_EXT), // 6:1
    COMPRESSED_DXT5_RGB8A_8(6, 4, -1, SizeCalculator.DXT5, GL11.GL_RGBA, EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT), // 4:1

    COMPRESSED_DXT1_SRGB8(7, 3, -1, SizeCalculator.DXT1, GL11.GL_RGB, EXTTextureSRGB.GL_COMPRESSED_SRGB_S3TC_DXT1_EXT), // 6:1
    COMPRESSED_DXT5_SRGB8A_8(8, 4, -1, SizeCalculator.DXT5, GL11.GL_RGBA, EXTTextureSRGB.GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT), // 4:1

    COMPRESSED_ETC2_RGB8(9, 3, -1, SizeCalculator.ETC2_RGB, GL11.GL_RGB, GL43.GL_COMPRESSED_RGB8_ETC2), // 6:1
    COMPRESSED_ETC2_RGBA8(10, 4, -1, SizeCalculator.ETC2_RGBA, GL11.GL_RGBA, GL43.GL_COMPRESSED_RGBA8_ETC2_EAC), // 6:1

    COMPRESSED_ETC2_SRGB8(11, 3, -1, SizeCalculator.ETC2_RGB, GL11.GL_RGB, GL43.GL_COMPRESSED_SRGB8_ETC2), // 6:1
    COMPRESSED_ETC2_SRGB8_A8(12, 4, -1, SizeCalculator.ETC2_RGBA, GL11.GL_RGBA, GL43.GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC), // 6:1

    /* HDR */
    R16(13, 1, 16, SizeCalculator.UNCOMPRESSED, GL11.GL_RED, GL30.GL_R16),
    RG16(14, 2, 32, SizeCalculator.UNCOMPRESSED, GL30.GL_RG, GL30.GL_RG16),
    RGB16(15, 3, 48, SizeCalculator.UNCOMPRESSED, GL11.GL_RGB, GL11.GL_RGB16),
    RGBA16(16, 4, 64, SizeCalculator.UNCOMPRESSED, GL11.GL_RGBA, GL11.GL_RGBA16),

    R16F(17, 1, 16, SizeCalculator.UNCOMPRESSED, GL11.GL_RED, GL30.GL_R16F),
    RG16F(18, 2, 32, SizeCalculator.UNCOMPRESSED, GL30.GL_RG, GL30.GL_RG16F),
    RGB16F(19, 3, 48, SizeCalculator.UNCOMPRESSED, GL11.GL_RGB, GL30.GL_RGB16F),
    RGBA16F(20, 4, 64, SizeCalculator.UNCOMPRESSED, GL11.GL_RGBA, GL30.GL_RGBA16F),

    R11F_G11F_B10F(21, 3, 32, SizeCalculator.UNCOMPRESSED, GL11.GL_RGB, GL30.GL_R11F_G11F_B10F),
    RGB10_A2(22, 4, 32, SizeCalculator.UNCOMPRESSED, GL11.GL_RGBA, GL11.GL_RGB10_A2);

    private interface SizeCalculator {
        int size(Format format, int width, int height);

        SizeCalculator UNCOMPRESSED = (f, w, h) -> w * h * f.bpp;
        SizeCalculator DXT1 = (f, w, h) -> (((w + 3) & ~0x03) * ((h + 3) & ~0x03) * f.channels * 8) / 6;
        SizeCalculator DXT5 = (f, w, h) -> (((w + 3) & ~0x03) * ((h + 3) & ~0x03) * f.channels * 8) / 4;

        SizeCalculator ETC2_RGB = (f, w, h) -> (int) (Math.ceil(w / 4.0) * Math.ceil(h / 4.0) * 8);
        SizeCalculator ETC2_RGBA = (f, w, h) -> (int) (Math.ceil(w / 4.0) * Math.ceil(h / 4.0) * 16);
    }

    private final byte id;
    private final int channels;
    private final int bpp;
    @Getter(AccessLevel.NONE)
    private final int glFormat;
    @Getter(AccessLevel.NONE)
    private final int glInternalSizedFormat;
    private final SizeCalculator sizeCalculator;

    Format(int id, int channels, int bpp, SizeCalculator sizeCalculator, int glFormat, int glInternalSizedFormat) {
        this.id = (byte) id;
        this.channels = channels;
        this.bpp = bpp;
        this.sizeCalculator = sizeCalculator;
        this.glFormat = glFormat;
        this.glInternalSizedFormat = glInternalSizedFormat;
    }

    static TIntObjectHashMap<Format> formats;

    static {
        Format[] fs = values();
        formats = new TIntObjectHashMap<>(fs.length);
        for (Format f : fs) {
            if (formats.containsKey(f.getId())) {
                throw new RuntimeException("Id " + f.getId() + " already used");
            }
            formats.put(f.getId(), f);
        }
    }

    public static Format byId(int id) {
        return formats.get(id);
    }

    public int getBytes(int width, int height) {
        return sizeCalculator.size(this, width, height);
    }

    public int getGLFormat() {
        return glFormat;
    }

    public int getGLInternalFormat() {
        return glInternalSizedFormat;
    }
}
