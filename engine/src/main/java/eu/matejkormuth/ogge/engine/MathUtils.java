package eu.matejkormuth.ogge.engine;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtils {

    public double log2(double n) {
        return Math.floor(Math.log(n) / Math.log(2));
    }

    public int log2(int n) {
        return (int) Math.floor(Math.log(n) / Math.log(2));
    }

}
