package eu.matejkormuth.ogge.engine.gl.api;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;

public enum ShaderType {
    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER),
    TESSELLATION_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
    TESSELLATION_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER);

    private final int constant;

    ShaderType(int constant) {
        this.constant = constant;
    }

    public int getConstant() {
        return constant;
    }
}
