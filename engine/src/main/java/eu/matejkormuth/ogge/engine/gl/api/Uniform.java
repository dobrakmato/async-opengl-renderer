package eu.matejkormuth.ogge.engine.gl.api;

import lombok.Data;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL41.*;

@Data
public class Uniform {

    private final int program;
    private final int location;
    private final String name;

    Uniform(Program program, String name, int location) {
        this.name = name;
        this.location = location;
        this.program = program.getName();
    }

    public void set(int value) {
        glProgramUniform1i(program, location, value);
    }

    public void set(int x, int y) {
        glProgramUniform2i(program, location, x, y);
    }

    public void set(int x, int y, int z) {
        glProgramUniform3i(program, location, x, y, z);
    }

    public void set(float value) {
        glProgramUniform1f(program, location, value);
    }

    public void set(float x, float y) {
        glProgramUniform2f(program, location, x, y);
    }

    public void set(float x, float y, float z) {
        glProgramUniform3f(program, location, x, y, z);
    }

    public void set(boolean transpose, FloatBuffer matrix4x4) {
        glProgramUniformMatrix4fv(program, location, true, matrix4x4);
    }
}
