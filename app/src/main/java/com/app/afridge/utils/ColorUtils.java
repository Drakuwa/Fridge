package com.app.afridge.utils;


import android.graphics.Color;

import java.util.Random;


public class ColorUtils {

    public static int randomColor() {
        // http://developer.android.com/reference/android/graphics/Color.html#HSVToColor%28float%5B%5D%29
        float hue = new Random().nextInt(360); // 0 - 360
        float saturation = randomInRange(0.5f,
                0.8f); // 0.5 to 0.8, away from white. but not too intense
        float brightness = randomInRange(0.5f,
                0.8f); // 0.5 to 0.8, away from black, but not too light

        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }

    private static float randomInRange(float min, float max) {

        float randomFloat = new Random().nextFloat();
        return randomFloat < 0.5
                ? ((1 - randomFloat) * (max - min) + min)
                : (randomFloat * (max - min) + min);
    }
}
