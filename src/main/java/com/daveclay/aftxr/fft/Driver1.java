package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.opengl.PShader;

public class Driver1 extends PApplet {

    public static void main(String[] args) {
        new Driver1().runSketch();
    }

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 600;

    // Declare a drawing variable for calculating the width of the
    int barWidth;

    Analyzer analyzer;

    int heightGrowthAmount = 0;
    int widthGrowthAmount = 0;
    boolean growthDirection = true;

    PShader blur;

    public void settings() {
        size(1440, 860, P2D);
    }

    public void setup() {
        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Prometheus.wav")
                .buildAnalyzer();

        colorMode(HSB, 1, 1, 1, 1);
        background(0);
        blur = loadShader("sepBlur.glsl");
        blur.set("blurSize", 1);
        blur.set("sigma", 1f);
        blur.set("horizontalPass", 0);

        // Calculate the width of the rects depending on how many bands we have
        barWidth = (int) (width/((float) bandsToDisplay));
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

            float lerp = lerp(5, 20, analyzer.timeSinceHit / 200f);
            background(.0f, 1f, .1f);
            blur.set("blurSize", (int)lerp);
            blur.set("sigma", lerp);
        }

        noStroke();
        textSize(500);
        fill(1, 1, 1, hit ? .2f : .02f);

        float hueOffset = hit ? .01f : 0;

        for (int i = 0; i < bandsToDisplay; i++) {
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip];

            float hue = (power * .45f) + hueOffset;
            float saturation = 1 - (power * 10);
            float value = 1;
            fill(hue, saturation, value, power * 10);

            int xShiftDirection = i % 2 == 0 ? -1 : 1;
            int x = (width / 2) + (xShiftDirection * (i * barWidth));
            // float height = power * scale * spectrographHeight;
            // float y = (spectrographHeight / 2f) - (height / 2f);
            float barPowerHeight = 20 + (heightGrowthAmount * (power * 100));
            float barHeight = min(770, barPowerHeight);
            // rect(x, y, barWidth, height);
            rect(x, 30, barWidth + widthGrowthAmount, barHeight);
            rect((width - x), 800 - barHeight, barWidth + widthGrowthAmount, barHeight);

            if (analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip] > 0.01 && !hit) {
                textSize(11);
                text(power, x - 5, power * 500 + (height / 2f));
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

        text(analyzer.lowPowerSum, 50, 600);
    }
}