package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.opengl.PShader;

public class BlackSunCircleDriver extends PApplet {

    public static void main(String[] args) {
        new BlackSunCircleDriver().runSketch();
    }

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 1000;

    // Declare a drawing variable for calculating the width of the
    int barWidth;

    Analyzer analyzer;

    int growthAmount = 1;

    PShader verticalBlur;
    PShader horizontalBlur;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Rigit Body III video.wav")
                .smoothingFactor(.1f)
                .lowPowerThreshold(.1f)
                .millisecondsToHoldHit(700)
                .numberOfLowBandsToSkip(7)
                .lowPassCutoff(8)
                .buildAnalyzer();

        verticalBlur = loadShader("sepBlur.glsl");
        verticalBlur.set("blurSize", 10);
        verticalBlur.set("sigma", 13.2f);
        verticalBlur.set("horizontalPass", 0);

        horizontalBlur = loadShader("sepBlur.glsl");
        horizontalBlur.set("blurSize", 10);
        horizontalBlur.set("sigma", 13.2f);
        horizontalBlur.set("horizontalPass", 1);

        background(255);

        // Calculate the width of the rects depending on how many bands we have
        barWidth = width / bandsToDisplay;
    }

    // TODO: https://github.com/daveclay/kinect/blob/master/src/main/java/com/daveclay/processing/kinect/bodylocator/AfterRebelBellyMultiBody.java

    public void draw() {
        analyzer.update();
        boolean hit = analyzer.lowPassHold;
        background(255);

        if (hit) {
            if (analyzer.lowPassTriggered) {
                growthAmount = 500;
            } else {
                growthAmount -= 30;
            }
        } else {
            growthAmount = 1;
        }

        fill(0, 0, 0, 240);
        circle(width / 2f, height / 2f, max(growthAmount, 100));
        noFill();

        noFill();
        for (int i = 0; i < bandsToDisplay; i++) {
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip];

            float alpha = power * 10000f;
            stroke(0, 0, 0, alpha);
            strokeWeight(1);
            float x = (width / 2f) + (power * (hit ? 200 : 80));
            float y = (height / 2f) + (hit ? (10 - (power * 300)) : (power * 100));
            int extent = (i * 2) + 100;
            circle(x, y, extent);
        }

        filter(verticalBlur);
        filter(horizontalBlur);
    }
}