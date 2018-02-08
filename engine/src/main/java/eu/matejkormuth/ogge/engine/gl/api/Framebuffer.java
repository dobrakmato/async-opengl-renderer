package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL30.*;

@Slf4j
public class Framebuffer implements Disposable, Labelable {

    public static final Framebuffer SCREEN = new Framebuffer(0);

    private /* ThreadLocal */ static int currentReadFbo = 0;
    private /* ThreadLocal */ static int currentWriteFbo = 0;

    @Getter
    private String label;
    @Getter
    private int name;
    private int bindTarget = 0;

    private Framebuffer(int name) {
        this.name = name;
    }

    public static Framebuffer create() {
        int newName = GL45.glCreateFramebuffers();
        return new Framebuffer(newName);
    }

    public void bindForWriting() {
        log.trace("Binding {} as WRITE framebuffer.", label != null ? label : name);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, name);
        bindTarget = GL_DRAW_FRAMEBUFFER;
        currentWriteFbo = name;
    }

    public void bindForReading() {
        log.trace("Binding {} as IO_DONE framebuffer.", label != null ? label : name);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, name);
        bindTarget = GL_READ_FRAMEBUFFER;
        currentReadFbo = name;
    }

    public void bind() {
        log.trace("Binding {} as IO_DONE and WRITE framebuffer.", label != null ? label : name);
        glBindFramebuffer(GL_FRAMEBUFFER, name);
        bindTarget = GL_FRAMEBUFFER;
        currentReadFbo = name;
        currentWriteFbo = name;
    }

    public boolean isBound() {
        switch (bindTarget) {
            case GL_DRAW_FRAMEBUFFER:
                return name == currentWriteFbo;
            case GL_READ_FRAMEBUFFER:
            case GL_FRAMEBUFFER:
                return name == currentReadFbo;
            default:
                return false;
        }
    }

    private void ensureBound() {
        if (!isBound()) {
            throw new RuntimeException("Framebuffer is not bound!");
        }
    }

    public void checkFramebuffer(int target) {
        if (GL45.glCheckNamedFramebufferStatus(name, target) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is broken! (" + GL45.glCheckNamedFramebufferStatus(name, target) + ")");
        }
    }

    public void attach(@Nonnull Texture2D texture, int attachment) {
        attach(texture, attachment, 0);
    }

    public void attach(@Nonnull Texture2D texture, int attachment, int level) {
        GL45.glNamedFramebufferTexture(name, attachment, texture.getName(), level);
    }

    public void attach(@Nonnull Renderbuffer renderbuffer, int attachment) {
        GL45.glNamedFramebufferRenderbuffer(name, attachment, GL_RENDERBUFFER, renderbuffer.getName());
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Framebuffer already disposed");
        }
        glDeleteFramebuffers(name);
        name = -1;
        label = null;
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL_FRAMEBUFFER, name, label == null ? "" : label.substring(0, 255));
    }
}
