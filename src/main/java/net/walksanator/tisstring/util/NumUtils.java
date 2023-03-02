package net.walksanator.tisstring.util;

public class NumUtils {
    private static final float HALF_FLOAT_MAX = HalfFloat.toFloat(HalfFloat.MAX_VALUE);
    private static final float HALF_FLOAT_MIN = HalfFloat.toFloat(HalfFloat.LOWEST_VALUE);

    public static float clampFloat(float value) {

        if(value < HALF_FLOAT_MIN) {
            return HALF_FLOAT_MIN;
        } else if (value > HALF_FLOAT_MAX) {
            return HALF_FLOAT_MAX;
        } else {
            return value;
        }
    }
}
