package eu.matejkormuth.ogge.engine;

import lombok.extern.slf4j.Slf4j;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVGGL3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.nanovg.NanoVG.*;

@Slf4j
public final class NanoVG implements Disposable {

    private int windowWidth;
    private int windowHeight;
    private float devicePixelRatio = 1f;
    private long vg;
    private final List<NVGColor> colors = new ArrayList<>();

    public NanoVG(int windowWidth, int windowHeight) {
        log.debug("Creating NanoVG...");
        vg = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | (Application.DEBUG ? NanoVGGL3.NVG_DEBUG : 0));

        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    @AutoFreed
    public NVGColor createColor(float r, float g, float b) {
        NVGColor color = NVGColor.malloc();
        colors.add(color);
        nvgRGBf(r, g, b, color);
        return color;
    }

    @AutoFreed
    public NVGColor createColor(float r, float g, float b, float a) {
        NVGColor color = NVGColor.malloc();
        colors.add(color);
        nvgRGBAf(r, g, b, a, color);
        return color;
    }

    public void createFont(@Nonnull String fontFace, @Nonnull String path) {
        nvgCreateFont(vg, fontFace, path);
    }

    public void beginFrame(int width, int height, float devicePixelRatio) {
        this.devicePixelRatio = devicePixelRatio;
        windowWidth = width;
        windowHeight = height;
    }

    public void beginFrame() {
        nvgBeginFrame(vg, windowWidth, windowHeight, devicePixelRatio);
    }

    public void endFrame() {
        nvgEndFrame(vg);
    }

    public void fontFace(String fontFace) {
        nvgFontFace(vg, fontFace);
    }

    public void fontSize(float size) {
        nvgFontSize(vg, size);
    }

    public void fillColor(NVGColor color) {
        nvgFillColor(vg, color);
    }

    public void text(float x, float y, String text) {
        nvgText(vg, x, y, text);
    }

    public void textBox(float x, float y, float breakRowWidth, String text) {
        nvgTextBox(vg, x, y, breakRowWidth, text);
    }

    public void textMultiline(float x, float y, String text) {
        textBox(x, y, Float.MAX_VALUE, text);
    }

    @Override
    public void dispose() {
        log.debug("Disposing NanoVG...");
        int i = 0;
        Iterator<NVGColor> itr = colors.iterator();
        while (itr.hasNext()) {
            i++;
            NVGColor color = itr.next();
            color.free(); // todo: this can cause crash if user freed this before.
            itr.remove();
        }
        log.debug(" Freed {} NVGColors.", i);

        NanoVGGL3.nvgDelete(vg);
    }
}
