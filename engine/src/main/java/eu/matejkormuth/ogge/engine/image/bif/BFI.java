package eu.matejkormuth.ogge.engine.image.bif;

import eu.matejkormuth.ogge.engine.AutoFreed;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BFI {

    public static final byte BF_HEADER_LZ4_BIT = 1;

    private static final LZ4FastDecompressor LZ4_DECOMPRESSOR = LZ4Factory.fastestInstance().fastDecompressor();
    private static final LZ4Compressor LZ4_COMPRESSOR = LZ4Factory.fastestInstance().highCompressor(17);

    public void readBF(@AutoFreed ByteBuffer in) {
        readMagicChar(in, 'B');
        readMagicChar(in, 'F');
        readMagicChar(in, 'I');

        byte flags = in.get();

        if ((flags & BF_HEADER_LZ4_BIT) == BF_HEADER_LZ4_BIT) {
            int uncompressedSize = in.getInt();

            ByteBuffer uncompressed = MemoryUtil.memAlloc(uncompressedSize);
            LZ4_DECOMPRESSOR.decompress(in, 4 + 4, uncompressed, 0, uncompressedSize);
            MemoryUtil.memFree(in);

            readBFI(uncompressed); // uncompressed freed by called method
        } else {
            readBFI(in); // in freed by called method
        }
    }

    private void readBFI(@AutoFreed ByteBuffer uncompressed) {
        short width = uncompressed.getShort();
        short height = uncompressed.getShort();

        Format format = Format.byId(uncompressed.get());
        byte cubemapFaces = uncompressed.get();
        byte mipmapLevels = uncompressed.get();
        byte flags = uncompressed.get();

        boolean isCubemap = cubemapFaces != 0;
        boolean hasMipmaps = mipmapLevels != 0;

        ByteBuffer pixels = null; // mipmap level 0
        ByteBuffer[] faces = null;
        ByteBuffer[] mipmaps = null; // mipmaps[0] is GL level 1

        if (isCubemap) {
            faces = new ByteBuffer[cubemapFaces];


        } else {
            // read level 0
            int bytes = format.getBytes(width, height);

            pixels = uncompressed.slice();
            pixels.limit(bytes);

            uncompressed.position(uncompressed.position() + bytes);

            if (hasMipmaps) {
                mipmaps = new ByteBuffer[mipmapLevels];

                int mipmapWidth = width;
                int mipmapHeight = height;

                for (int i = 0; i < mipmapLevels; i++) {
                    mipmapWidth /= 2;
                    mipmapHeight /= 2;

                    bytes = format.getBytes(mipmapWidth, mipmapHeight);

                    mipmaps[i] = uncompressed.slice();
                    mipmaps[i].limit(bytes);

                    uncompressed.position(uncompressed.position() + bytes);
                }
            }
        }

        // Free 'uncompressed' bytebuffer.
        MemoryUtil.memFree(uncompressed);
    }

    private void readMagicChar(ByteBuffer in, char b) {
        byte c = in.get();
        if (c != b) {
            throw new RuntimeException("Magic does nto match!");
        }
    }
}
