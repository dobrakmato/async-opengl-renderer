package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.GL_PROGRAM;

@ToString
@Slf4j
public class Program implements Disposable, Labelable {

    @Getter
    private String label;
    @Getter
    private int name;
    private transient final List<Shader> shaders = new ArrayList<>();

    private Program(int name) {
        this.name = name;
    }

    public static Program create() {
        int newName = glCreateProgram();
        return new Program(newName);
    }

    @Nullable
    public Uniform getUniform(@Nonnull String name) {
        int location = glGetUniformLocation(this.name, name);

        if (location == -1) {
            log.error("Tried to get uniform {} bt it was not  found in program {}!", name, this);
            return null;
        }

        return new Uniform(this, name, location);
    }

    public void attach(@Nonnull Shader shader) {
        shaders.add(shader);
        glAttachShader(name, shader.getName());
    }

    public void link() {
        glLinkProgram(name);

        String infoLog = glGetProgramInfoLog(name);
        if (!infoLog.isEmpty()) {
            log.warn("Program linking info log: {}", infoLog);
        }

        // Check for errors.
        if (glGetProgrami(name, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program linking failed!");
        }

        // Detach shaders.
        for (Shader shader : shaders) {
            glDetachShader(name, shader.getName());
            shader.dispose();
        }
        shaders.clear();
    }

    public void validate() {
        glValidateProgram(name);

        // Check for errors.
        if (glGetProgrami(name, GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            String infolog = GL20.glGetProgramInfoLog(name);
            log.error("Program validation error: {}", infolog.trim());

            throw new RuntimeException("Program validation failed!");
        }
    }

    public void use() {
        glUseProgram(name);
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Program already disposed");
        }
        glDeleteProgram(name);
        name = -1;
        label = null;
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL_PROGRAM, name, label == null ? "" : label.substring(0, 255));
    }

}
