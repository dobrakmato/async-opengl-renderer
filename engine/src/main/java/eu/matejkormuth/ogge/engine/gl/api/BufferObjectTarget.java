package eu.matejkormuth.ogge.engine.gl.api;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

public enum BufferObjectTarget {
    /**
     * IBO
     */
    ELEMENT_ARRAY_BUFFER(GL15.GL_ELEMENT_ARRAY_BUFFER),
    /**
     * VBO
     */
    ARRAY_BUFFER(GL15.GL_ARRAY_BUFFER),
    /**
     * PBO Download (from GPU)
     */
    PIXEL_PACK_BUFFER(GL21.GL_PIXEL_PACK_BUFFER),
    /**
     * PBO Upload (to GPU)
     */
    PIXEL_UNPACK_BUFFER(GL21.GL_PIXEL_UNPACK_BUFFER);

    private final int constant;

    BufferObjectTarget(int constant) {
        this.constant = constant;
    }

    public int getConstant() {
        return constant;
    }
}
