package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL30.*;

@ToString
@Slf4j
public class Renderbuffer implements Disposable, Labelable {

    @Getter
    private final int name;
    @Getter
    private String label;

    private Renderbuffer(int name) {
        this.name = name;
    }

    public Renderbuffer create() {
        int newName = GL45.glCreateRenderbuffers();
        return new Renderbuffer(newName);
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL_RENDERBUFFER, name, label == null ? "" : label.substring(0, 255));
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Renderbuffer already disposed");
        }
        glDeleteRenderbuffers(name);
        label = null;
    }
}
