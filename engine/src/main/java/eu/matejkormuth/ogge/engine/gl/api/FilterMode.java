package eu.matejkormuth.ogge.engine.gl.api;

/**
 * An enum for the texture filtering modes.
 */
public enum FilterMode {
    LINEAR(0x2601, false), // GL11.GL_LINEAR
    NEAREST(0x2600, false), // GL11.GL_NEAREST
    NEAREST_MIPMAP_NEAREST(0x2700, true), // GL11.GL_NEAREST_MIPMAP_NEAREST
    LINEAR_MIPMAP_NEAREST(0x2701, true), //GL11.GL_LINEAR_MIPMAP_NEAREST
    NEAREST_MIPMAP_LINEAR(0x2702, true), // GL11.GL_NEAREST_MIPMAP_LINEAR
    LINEAR_MIPMAP_LINEAR(0x2703, true); // GL11.GL_LINEAR_MIPMAP_LINEAR

    private final int glConstant;
    private final boolean mipmaps;

    FilterMode(int glConstant, boolean mipmaps) {
        this.glConstant = glConstant;
        this.mipmaps = mipmaps;
    }

    /**
     * Gets the OpenGL constant for this texture filter.
     *
     * @return The OpenGL Constant
     */
    public int getGLConstant() {
        return glConstant;
    }

    /**
     * Returns true if the filtering mode required generation of mipmaps.
     *
     * @return Whether or not mipmaps are required
     */
    public boolean needsMipmaps() {
        return mipmaps;
    }
}