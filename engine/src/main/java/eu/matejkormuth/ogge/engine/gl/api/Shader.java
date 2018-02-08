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

import static org.lwjgl.opengl.GL20.*;

@ToString
@Slf4j
public class Shader implements Disposable, Labelable {

    @Getter
    private int name;

    @Getter
    private String label;

    public Shader(@Nonnull String source, @Nonnull ShaderType type) {
        name = glCreateShader(type.getConstant());

        glShaderSource(name, source);
        glCompileShader(name);

        String infoLog = glGetShaderInfoLog(name);
        if (!infoLog.isEmpty()) {
            log.warn("Shader compilation output: {}", infoLog);
        }

        if (GL20.glGetShaderi(name, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            log.error(" Shader compilation errors!");
            // todo: better error report
        }
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL43.GL_SHADER, name, label == null ? "" : label.substring(0, 255));
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Shader already disposed");
        }
        glDeleteShader(name);
        name = -1;
        label = null;
    }
}
