package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.opengl.PShader;

public class Basic extends PApplet  {

    public static void main(String[] args) {
        new Basic().runSketch();
    }
    /**
     * Blur Filter
     *
     * Change the default shader to apply a simple, custom blur filter.
     *
     * Press the mouse to switch between the custom and default shader.
     */

    private PShader blur;

    public void settings() {
        size(640, 360, P2D);
    }

    public void setup() {
        blur = loadShader("blur.glsl");
        stroke(255, 0, 0);
        rectMode(CENTER);
    }

    public void draw() {
        filter(blur);
        rect(mouseX, mouseY, 150, 150);
        ellipse(mouseX, mouseY, 100, 100);
    }
}
