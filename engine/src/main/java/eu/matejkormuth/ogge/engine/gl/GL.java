package eu.matejkormuth.ogge.engine.gl;

import lombok.experimental.UtilityClass;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class GL {

    public void faceCullingCW() {
        GL11.glFrontFace(GL11.GL_CW);
    }

    public void faceCullingCCW() {
        GL11.glFrontFace(GL11.GL_CCW);
    }

    public static void clearColor(float r, float g, float b) {
        clearColor(r, g, b, 1);
    }

    public static void clearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }
}
