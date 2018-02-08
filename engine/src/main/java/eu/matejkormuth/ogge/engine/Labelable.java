package eu.matejkormuth.ogge.engine;

import javax.annotation.Nullable;

public interface Labelable {
    boolean SUPPORTED = true;

    @Nullable String getLabel();

    void setLabel(@Nullable String label);
}
