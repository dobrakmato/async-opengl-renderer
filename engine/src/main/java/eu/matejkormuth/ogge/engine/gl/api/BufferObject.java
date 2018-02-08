package eu.matejkormuth.ogge.engine.gl.api;

import eu.matejkormuth.ogge.engine.Disposable;
import eu.matejkormuth.ogge.engine.Labelable;
import lombok.Getter;
import lombok.ToString;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL43.GL_BUFFER;
import static org.lwjgl.opengl.GL45.*;

@ToString
public class BufferObject implements Disposable, Labelable {

    @Getter
    private int name;
    @Getter
    private String label;

    private BufferObject(int name) {
        this.name = name;
    }

    public static BufferObject create() {
        int newId = glCreateBuffers();
        return new BufferObject(newId);
    }

    public void bind(BufferObjectTarget target) {
        GL15.glBindBuffer(target.getConstant(), name);
    }

    public void bindAsVbo() {
        this.bind(BufferObjectTarget.ARRAY_BUFFER);
    }

    public void bindAsArrayBuffer() {
        this.bind(BufferObjectTarget.ARRAY_BUFFER);
    }

    public void bindAsIbo() {
        this.bind(BufferObjectTarget.ELEMENT_ARRAY_BUFFER);
    }

    public void allocate(long bytes, @Nonnull UsageHint hint) {
        glNamedBufferData(name, bytes, hint.getConstant());
    }

    public void uploadData(@Nonnull IntBuffer intBuffer, @Nonnull UsageHint hint) {
        glNamedBufferData(name, intBuffer, hint.getConstant());
    }

    public void uploadData(@Nonnull FloatBuffer floatBuffer, @Nonnull UsageHint hint) {
        glNamedBufferData(name, floatBuffer, hint.getConstant());
    }

    public void uploadSubData(long offset, @Nonnull IntBuffer intBuffer) {
        glNamedBufferSubData(name, offset, intBuffer);
    }

    public void uploadSubData(long offset, @Nonnull FloatBuffer floatBuffer) {
        glNamedBufferSubData(name, offset, floatBuffer);
    }

    public ByteBuffer map(int access) {
        return glMapNamedBuffer(name, access);
    }

    public boolean map(int access, Consumer<ByteBuffer> cb) {
        ByteBuffer bb = map(access);
        cb.accept(bb);
        return unmap();
    }

    public ByteBuffer map(ByteBuffer buffer, int access) {
        return glMapNamedBuffer(name, access, buffer);
    }

    public ByteBuffer map(ByteBuffer buffer, int access, long length) {
        return glMapNamedBuffer(name, access, length, buffer);
    }

    public void mapRange(ByteBuffer buffer, int access, long length, long offset) {
        glMapNamedBufferRange(name, offset, length, access, buffer);
    }

    public boolean unmap() {
        return glUnmapNamedBuffer(name);
    }

    @Override
    public void setLabel(@Nullable String label) {
        this.label = label;
        GL43.glObjectLabel(GL_BUFFER, name, label == null ? "" : label.substring(0, 255));
    }

    @Override
    public void dispose() {
        if (name == -1) {
            throw new IllegalStateException("Texture already disposed");
        }

        GL15.glDeleteBuffers(name);
        name = -1;
    }

    public void unbind(BufferObjectTarget target) {
        GL15.glBindBuffer(target.getConstant(), 0);
    }
}
