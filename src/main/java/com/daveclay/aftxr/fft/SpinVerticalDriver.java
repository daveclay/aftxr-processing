package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PShader;
import peasy.PeasyCam;

public class SpinVerticalDriver extends PApplet {

    public static void main(String[] args) {
        new SpinVerticalDriver().runSketch();
    }

    PeasyCam cam;

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 600;

    // Declare a drawing variable for calculating the width of the
    int barWidth;

    Analyzer analyzer;

    int heightGrowthAmount = 0;
    int widthGrowthAmount = 0;
    boolean growthDirection = true;
    float rotation = 0f;

    PShader blur;
    PFont mono11;
    PFont mono24;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        mono11 = loadFont("PTMono-Regular-11.vlw");
        mono24 = loadFont("PTMono-Regular-24.vlw");
        background(0);
        textFont(mono11);

        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Lense 5.wav")
                .lowPowerThreshold(.1f)
                .smoothingFactor(.3f)
                .millisecondsToHoldHit(100)
                .numberOfLowBandsToSkip(7)
                .lowPassCutoff(8)
                .buildAnalyzer();

        cam = new PeasyCam(this, 400);
        cam.lookAt(-197.94, -119.22, -94.51);
        cam.setRotations(-1.33f, -0.75, -.13f);
        cam.setDistance(400);

        colorMode(HSB, 1, 1, 1, 1);
        background(0);
        blur = loadShader("sepBlur.glsl");
        blur.set("blurSize", 3);
        blur.set("sigma", 2f);
        blur.set("horizontalPass", 0);
        barWidth = 3;
    }

    public void draw() {
        analyzer.update();
        boolean hit = analyzer.lowPassHold;

        heightGrowthAmount = growthDirection ? heightGrowthAmount + 1 : heightGrowthAmount - 1;
        background(0);

        if (hit) {
            if (analyzer.lowPassTriggered) {
                heightGrowthAmount = 0;
                widthGrowthAmount = 0;
            }

            float lerp = lerp(5, 50, analyzer.timeSinceHit / 200f);
            background(.0f, 1f, .1f);
            blur.set("blurSize", (int)lerp);
            blur.set("sigma", lerp);
        }

        float hueOffset = hit ? .01f : 0;
        noFill();

        for (int i = 0; i < bandsToDisplay; i++) {
            // 1 - 4000, apply power between?
            float scale = norm(i + 1, 0, bandsToDisplay) * 10;
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip] * scale;
            if (hit) {
                power *= 2;
            }

            float hue = (power * .3f) + hueOffset;
            float saturation = max(0, .3f - (power));
            float value = 1;

            int xShiftDirection = i % 2 == 0 ? -1 : 1;
            int x = (xShiftDirection * (i * barWidth));

            stroke(hue, saturation, value, lerp(0, .8f, power));
            fill(hue, saturation - .2f, value, lerp(.0f, .8f, power));

            pushMatrix();
            float depth = lerp(1, 1000, power);
            translate(x / 2f, 0, (0 + depth / 2));
            box(barWidth, 1000, depth);
            popMatrix();

            if (analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip] > 0.01 && !hit) {
                textSize(11);;
                text(power, x, 500 + (this.height / 2f), 10);
            }
        }

        if (heightGrowthAmount > 100) {
            // widthGrowthAmount++;
            // maybe blue?
            heightGrowthAmount += 9;
        }

        if (hit) {
            filter(blur);
        }

        rotation += .2f;

        cam.beginHUD();
        fill(1, 0, 1, 1);
        /*
        float[] lookAt = cam.getLookAt();
        String[] lookAtStrs = nfc(lookAt, 2);
        text("lookAt: ", 10, yPos);
        text(lookAtStrs[0], 10, 2 * yPos);
        text(lookAtStrs[1], 10, 3 * yPos);
        text(lookAtStrs[2], 10, 4 * yPos);

        float[] rotations = cam.getRotations();
        String[] rotationStrings = nfc(rotations, 2);
        text("rotations: ", 10, 5 * yPos);
        text(rotationStrings[0], 10, 6 * yPos);
        text(rotationStrings[1], 10, 7 * yPos);
        text(rotationStrings[2], 10, 8 * yPos);

        text("distance: ", 10, 9 * yPos);
        text(nfc((float)cam.getDistance(), 2), 10, 10 * yPos);
         */

        fill(1, 0, 1, .5f);
        textFont(mono11);
        textSize(11);
        int yPos = 18;

        float num = analyzer.lowPowerSum * 100;
        text(String.valueOf(num).substring(0, 4), 100, yPos);
        text(hit ? "ACC" : "---", 100, 2 * yPos);

        textFont(mono24);
        textSize(36);
        text("AFTXR", 140, 36);

        cam.endHUD();
        // camera(xpos, ypos, zpos, 0, 0, 0, -1, -1, 0);
    }
}