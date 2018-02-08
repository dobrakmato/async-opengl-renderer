package eu.matejkormuth.ogge.engine;

import eu.matejkormuth.ogge.engine.content.Content;
import eu.matejkormuth.ogge.engine.content.Decoders;
import eu.matejkormuth.ogge.engine.content.Request;
import eu.matejkormuth.ogge.engine.content.Uploaders;
import eu.matejkormuth.ogge.engine.content.uploaders.SimpleAsyncTextureUploader;
import eu.matejkormuth.ogge.engine.gl.GL;
import eu.matejkormuth.ogge.engine.gl.api.*;
import eu.matejkormuth.ogge.engine.image.SimpleImage;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static org.lwjgl.opengl.GL30.GL_MAX_COLOR_ATTACHMENTS;
import static org.lwjgl.opengl.GL43.GL_MAX_LABEL_LENGTH;

@ExtensionMethod({Object.class, ObjectExtensions.class})
@Slf4j
public final class Renderer {

    private void printDebugInformation() {
        log.debug("GL_VERSION: " + glGetString(GL11.GL_VERSION));
        log.debug("GL_RENDERER: " + glGetString(GL11.GL_RENDERER));
        log.debug("GL_VENDOR: " + glGetString(GL11.GL_VENDOR));

        log.trace("GL_SHADING_LANGUAGE_VERSION: " + glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
        log.trace("GL_MAX_VERTEX_ATTRIBS: " + glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS));
        log.trace("GL_MAX_LABEL_LENGTH: " + glGetInteger(GL_MAX_LABEL_LENGTH));
        log.trace("GL_MAX_TEXTURE_SIZE: " + glGetInteger(GL_MAX_TEXTURE_SIZE));
        log.trace("GL_MAX_TEXTURE_IMAGE_UNITS: " + glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS));
        log.trace("GL_MAX_COLOR_ATTACHMENTS: " + glGetInteger(GL_MAX_COLOR_ATTACHMENTS));
    }

    public void initialize() {
        printDebugInformation();
        String contentRoot = "C:\\Users\\Matej\\IdeaProjects\\ogge\\engine\\src\\main\\resources";

        // Set GL state defaults.
        // GL.faceCullingCCW();
        GL.clearColor(0.3f, 0.5f, 1);

        // Create objects.
        vao = VAO.create();
        vbo = BufferObject.create();
        vbo.allocate(4 * 4 * Float.BYTES, UsageHint.STATIC_DRAW);

        vao.enableAttribute(0);
        vao.attributeFormat(0, VAO.AttributeType.VEC3, false, 0);
        vao.attributeSource(0, vbo, 0L, 4 * Float.BYTES);
        vao.attributeBindSource(0, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buff = stack.mallocFloat(4 * 4);

            //  [ ------ vec3 ----- ] [ align ]
            buff.put(0).put(1).put(0).put(0);
            buff.put(1).put(1).put(0).put(0);
            buff.put(1).put(0).put(0).put(0);
            buff.put(0).put(0).put(0).put(0);

            buff.flip();
            vbo.uploadSubData(0L, buff);
        }

        program = Program.create();
        program.attach(new Shader(Content.loadText(contentRoot + "\\shaders\\test.vert"), ShaderType.VERTEX));
        program.attach(new Shader(Content.loadText(contentRoot + "\\shaders\\test.frag"), ShaderType.FRAGMENT));
        program.link();

        textureUniform = program.getUniform("image");

        // Start async texture load.
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<Request<Texture2D, SimpleImage>> requests = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                Path path = Paths.get(contentRoot + "\\textures\\floor_yellow" + i + ".png");
                int finalI = i;
                requests.add(Content.<Texture2D, SimpleImage>request()
                        .path(path)
                        .timings(true)
                        .uploader(Uploaders.get(SimpleAsyncTextureUploader.class))
                        .decoder(Decoders.choosePerfect(path))
                        .success((r) -> {
                            if (finalI == 0) {
                                tex = r.getItem();
                                tex.bind();
                                tex.setFilters(FilterMode.NEAREST, FilterMode.NEAREST);
                                tex.setWraps(WrapMode.CLAMP_TO_BORDER, WrapMode.CLAMP_TO_BORDER);
                            } else {
                                r.getItem().dispose();
                            }

                            if (finalI == 9) {
                                for (Request req : requests) {
                                    log.info("{}", req.getTiming());
                                }
                            }
                        })
                        .dispatch());
            }
        }).start();
    }

    private Texture2D tex;
    private VAO vao;
    private BufferObject vbo;
    private Program program;
    private Uniform textureUniform;

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        program.use();
        if (tex != null) {
            textureUniform.set(0);
            Texture2D.activateSampler(0);
            tex.bind();
        }
        vao.bind();
        glDrawArrays(GL_QUADS, 0, 4);

        // bind HDR buffer

        // determine exposure

        // resolve HDR -> LDR
    }

    public void cleanUp() {

    }
}
