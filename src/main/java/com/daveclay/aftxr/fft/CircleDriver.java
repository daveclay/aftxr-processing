package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.opengl.PShader;

public class CircleDriver extends PApplet {

    public static void main(String[] args) {
        new CircleDriver().runSketch();
    }

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 1000;

    // Declare a drawing variable for calculating the width of the
    int barWidth;

    Analyzer analyzer;

    int heightGrowthAmount = 0;
    int widthGrowthAmount = 0;
    boolean growthDirection = true;

    PShader blur;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Rigit Body III video.wav")
                .smoothingFactor(.7f)
                .buildAnalyzer();

        colorMode(HSB, 1, 1, 1, 1);
        background(0);

        // Calculate the width of the rects depending on how many bands we have
        barWidth = width / bandsToDisplay;
    }

    // TODO: https://github.com/daveclay/kinect/blob/master/src/main/java/com/daveclay/processing/kinect/bodylocator/AfterRebelBellyMultiBody.java

    public void draw() {
        analyzer.update();
        boolean hit = analyzer.lowPassHold;
        background(0);
        fill(1, 1, 1);
        text(barWidth, 10, 10);
        noFill();

        if (hit) {
            if (analyzer.lowPassTriggered) {
                heightGrowthAmount = 0;
                widthGrowthAmount = 0;
            }
        }

        float hueOffset = hit ? .01f : 0;

        for (int i = 0; i < bandsToDisplay; i++) {
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip] * 4;

            float hue = (power * .45f) + hueOffset;
            float saturation = 1 - (power * 2);
            float value = 1;
            float alpha = power * 10;
            noFill();
            stroke(hue, saturation, value, alpha);
            strokeWeight(1);
            float x = width / 2f;
            float y = height / 2f;
            circle(x, y, i * barWidth);
        }
    }
}