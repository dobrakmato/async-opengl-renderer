package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import lombok.Getter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL45.glCreateVertexArrays;


public class VAO implements Disposable, Labelable {

    private int name;
    @Getter
    private String label;

    private VAO(int name) {
        this.name = name;
    }

    public static VAO create() {
        int newName = glCreateVertexArrays();
        return new VAO(newName);
    }

    public void bind() {
        glBindVertexArray(name);
    }

    public void enableAttribute(int attrIndex) {
        GL45.glEnableVertexArrayAttrib(name, attrIndex);
    }

    public void attributeFormat(int attrIndex, @Nonnull AttributeType type, boolean normalized, int relativeOffset) {
        GL45.glVertexArrayAttribFormat(name, attrIndex, type.getSize(), type.getType(), normalized, relativeOffset);
    }

    public void attributeSource(int bindIndex, @Nonnull BufferObject buffer, long offset, int stride) {
        GL45.glVertexArrayVertexBuffer(name, bindIndex, buffer.getName(), offset, stride);
    }

    public void attributeBindSource(int attrIndex, int bindIndex) {
        GL45.glVertexArrayAttribBinding(name, attrIndex, bindIndex);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL11.GL_VERTEX_ARRAY, name, label == null ? "" : label.substring(0, 255));
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("VAO already disposed");
        }

        GL30.glDeleteVertexArrays(name);
        name = -1;
    }

    @Getter
    public enum AttributeType {
        VEC2(2, GL_FLOAT),
        VEC3(3, GL_FLOAT),
        VEC4(4, GL_FLOAT);

        private final int type;
        private final int size;

        AttributeType(int size, int type) {
            this.size = size;
            this.type = type;
        }
    }
}
