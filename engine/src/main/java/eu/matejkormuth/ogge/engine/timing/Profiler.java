package eu.matejkormuth.ogge.engine.timing;

import eu.matejkormuth.ogge.engine.Application;
import lombok.Getter;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Profiler implements Comparable<Profiler> {

    @Getter
    @Nullable
    private final Profiler parent;
    private final int level;
    private final List<Profiler> children = new ArrayList<>();

    @Getter
    private final String name;
    @Getter
    private long totalTime = 0L;
    @Getter
    private long invocations = 0L;

    private long lastStart = 0L;

    private Profiler(Profiler parent, String name, int level) {
        this.parent = parent;
        this.name = name;
        this.level = level;
    }

    public static Profiler createRoot() {
        return createRoot("root");
    }

    public static Profiler createRoot(String name) {
        return new Profiler(null, name, 0);
    }

    public Profiler createChild(String name) {
        Profiler p = new Profiler(this, name, level + 1);
        children.add(p);
        return p;
    }

    public void reset() {
        reset(false);
    }

    public void reset(boolean children) {
        this.invocations = 0;
        this.totalTime = 0;

        if (children) {
            for (Profiler profiler : this.children) {
                profiler.reset(true);
            }
        }
    }

    public void start() {
        invocations++;
        lastStart = System.nanoTime();
    }

    public long stop() {
        long timeSpent = (System.nanoTime() - lastStart);
        totalTime += timeSpent;
        return timeSpent;
    }

    public void end() {
        totalTime += (System.nanoTime() - lastStart);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    private static final DecimalFormat df = new DecimalFormat("##.##");

    private String humanReadableTime(float ns) {
        if (ns < 1000) { // ns
            return ns + "ns";
        } else if (ns < 1_000_000 * 1000) { // ms
            return df.format(ns / 1_000_000f) + "ms";
        } else if (ns < 1_000_000 * 1000 * 60) { // s
            return df.format(ns / (1_000_000 * 1000f)) + "se";
        } else { // m
            return df.format(ns / (1_000_000 * 1000 * 60f)) + "mi";
        }
    }

    private String humanReadableNumber(long n) {
        if (n < 1000) {
            return n + "";
        } else if (n < 1000 * 1000) {
            return df.format(n / 1000) + "K";
        } else if (n < 1000 * 1000 * 1000) {
            return df.format(n / (1000 * 1000)) + "M";
        } else {
            return df.format(n / (1000 * 1000 * 1000)) + "G";
        }
    }

    public float safeDiv(long divisor, long divider) {
        if (divider == 0) {
            return 0f;
        } else {
            return divisor / (float) divider;
        }
    }

    @Override
    public int compareTo(Profiler o) {
        if (o.totalTime == totalTime)
            return 0;

        return o.totalTime > totalTime ? 1 : -1;
    }

    public String toString(boolean includeChildren) {
        StringBuilder sb = new StringBuilder();

        String TAB = "\t\t";

        if (level != 0) {
            //char[] ident = new char[level];
            //Arrays.fill(ident, ' ');
            sb.append(new String(new char[level]).replace('\0', ' '));
        } else {
            sb.append("Name").append(TAB).append("Frame").append(TAB).append("Avg").append(TAB).append("Inv").append(TAB).append("Total")
                    .append("\n");
        }

        sb.append(name).append(TAB)
                .append(df.format(safeDiv(totalTime, Application.getFrames()) / 1_000_000f) + "ms").append(TAB)
                .append(df.format(safeDiv(totalTime, invocations) / 1_000_000f) + "ms").append(TAB)
                .append(humanReadableNumber(invocations)).append(TAB)
                .append(df.format(totalTime / 1_000_000f) + "ms").append("\n");

        if (includeChildren && !children.isEmpty()) {
            List<Profiler> ordered = new ArrayList<>(children);
            Collections.sort(ordered);
            for (Profiler child : ordered) {
                sb.append(child.toString(true));
            }
        }

        return sb.toString();
    }
}