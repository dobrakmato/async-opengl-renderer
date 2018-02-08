package eu.matejkormuth.ogge.engine.gl.api;

/**
 * An enum for the texture wrapping modes.
 */
public enum WrapMode {
    REPEAT(0x2901), // GL11.GL_REPEAT
    CLAMP_TO_EDGE(0x812F), // GL12.GL_CLAMP_TO_EDGE
    CLAMP_TO_BORDER(0x812D), // GL13.GL_CLAMP_TO_BORDER
    MIRRORED_REPEAT(0x8370); // GL14.GL_MIRRORED_REPEAT

    private final int glConstant;

    WrapMode(int glConstant) {
        this.glConstant = glConstant;
    }

    /**
     * Gets the OpenGL constant for this texture wrap.
     *
     * @return The OpenGL Constant
     */
    public int getGLConstant() {
        return glConstant;
    }
}