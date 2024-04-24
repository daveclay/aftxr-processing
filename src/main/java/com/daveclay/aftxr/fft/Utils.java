package com.daveclay.aftxr.fft;

public class Utils {
    public static float interpolate(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }
}
