package eu.matejkormuth.ogge.engine.gl;

import lombok.extern.slf4j.Slf4j;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryUtil.memByteBuffer;

@Slf4j
public final class DebugMessageCallback implements GLDebugMessageCallbackI {
    @Override
    public void invoke(int source, int type, int id, int severity, int length, long msg, long userParam) {

        if (type == GL43.GL_DEBUG_TYPE_PUSH_GROUP || type == GL43.GL_DEBUG_TYPE_POP_GROUP) {
            return;
        }

        String message = MemoryUtil.memUTF8(memByteBuffer(msg, length));

        if (message.contains("Buffer object 2") || message.contains("768 bytes")) {
            return;
        }

        String source_s;
        switch (source) {
            case 33350:
                source_s = "API";
                break;
            case 33351:
                source_s = "WINDOW_SYSTEM";
                break;
            case 33352:
                source_s = "SHADER_COMPILER";
                break;
            case 33353:
                source_s = "THIRD_PARTY";
                break;
            case 33354:
                source_s = "APPLICATION";
                break;
            case 33355:
                source_s = "OTHER_SOURCE";
                break;
            default:
                source_s = this.printUnknownToken(source);
        }

        String severity_s;
        switch (severity) {
            case 33387:
                severity_s = "NOTIFICATION";
                break;
            case 37190:
                severity_s = "HIGH";
                break;
            case 37191:
                severity_s = "MEDIUM";
                break;
            case 37192:
                severity_s = "LOW";
                break;
            default:
                severity_s = this.printUnknownToken(severity);
        }

        String type_s;
        switch (type) {
            case 33356:
                type_s = "ERROR";
                log.error("[GL] [{}/{}] [{}] {}", type_s, source_s, severity_s, message);
                return;
            case 33357:
                type_s = "DEPRECATED_BEHAVIOR";
                break;
            case 33358:
                type_s = "UNDEFINED_BEHAVIOR";
                break;
            case 33359:
                type_s = "PORTABILITY";
                break;
            case 33360:
                type_s = "PERFORMANCE";
                break;
            case 33361:
                type_s = "OTHER_TYPE";
                break;
            case 33384:
                type_s = "MARKER";
                break;
            default:
                type_s = this.printUnknownToken(type);
        }

        log.info("[GL] [{} {} {}] {}", type_s, source_s, severity_s, message);

    }

    private String printUnknownToken(int token) {
        return "Unknown (0x" + Integer.toHexString(token).toUpperCase() + ")";
    }
}
