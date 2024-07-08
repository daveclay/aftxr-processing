package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PShader;

import static com.daveclay.processing.Utils.interpolate;

public class GridDriver extends PApplet {

    public static void main(String[] args) {
        new GridDriver().runSketch();
    }

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 1000;

    // Declare a drawing variable for calculating the width of the
    int barWidth;

    Analyzer analyzer;

    int heightGrowthAmount = 0;
    int widthGrowthAmount = 0;
    boolean growthDirection = true;

    PGraphics background;

    PShader blur;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Rigit Body III video.wav")
                .smoothingFactor(.2f)
                .lowPowerThreshold(.1f)
                .millisecondsToHoldHit(700)
                .numberOfLowBandsToSkip(2)
                .lowPassCutoff(2)
                .buildAnalyzer();

        blur = loadShader("sepBlur.glsl");

        background = createGraphics(width, height, P3D);

        // Calculate the width of the rects depending on how many bands we have
        barWidth = width / bandsToDisplay;
    }

    // TODO: https://github.com/daveclay/kinect/blob/master/src/main/java/com/daveclay/processing/kinect/bodylocator/AfterRebelBellyMultiBody.java

    public void draw() {
        analyzer.update();
        boolean hit = analyzer.lowPassHold;

        if (hit) {
            if (analyzer.lowPassTriggered) {
                heightGrowthAmount = 0;
                widthGrowthAmount = 0;
            }
        }

        float hueOffset = hit ? 30 : 0;

        background.beginDraw();
        background.colorMode(HSB, 255, 255, 255, 255);
        background.background(0, 0, 0);
        for (int i = 0; i < bandsToDisplay; i++) {
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip];

            int hue = (int) interpolate(power, 0, .07f, 0, 25);
            int fullRange = (int) interpolate(power, 0, .05f, 1, 255);
            int saturation = 255;
            int value = 255;
            int alpha = fullRange;
            background.fill(hue, saturation, value, alpha);

            int size = 20;
            int cols = width / size;
            int col = i % cols;
            int rows = height / size;
            int row = ((int) Math.floor((float) i / (float) rows) % rows);
            int y = row * size;
            int x = col * size;

            background.rect(x, y, size, size);
        }

        blur.set("blurSize", (int) interpolate(analyzer.lowPowerSum, .006f, .01f, 1, 60));
        blur.set("sigma", lerp(2f, 10f, analyzer.lowPowerSum * 1000));
        blur.set("horizontalPass", 0);
        background.filter(blur);

        background.endDraw();;
        image(background, 0, 0);
    }
}