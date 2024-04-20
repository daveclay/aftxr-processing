package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.opengl.PShader;
import processing.sound.*;

public class Driver1 extends PApplet {

    public static void main(String[] args) {
        new Driver1().runSketch();
    }

    // Declare the sound source and FFT analyzer variables
    SoundFile soundFile;
    FFT fft;
    BeatDetector beatDetector;

    // Define how many FFT bands to use (this needs to be a power of two)
    int bands = 4096;

    // Define a smoothing factor which determines how much the spectrums of consecutive
    // points in time should be combined to create a smoother visualisation of the spectrum.
    // A smoothing factor of 1.0 means no smoothing (only the data from the newest analysis
    // is rendered), decrease the factor down towards 0.0 to have the visualisation update
    // more slowly, which is easier on the eye.
    float smoothingFactor = 0.99f;

    // Create a vector to store the smoothed spectrum data in
    float[] sum = new float[bands];
    int bandsToDisplay = 600;
    int numberOfLowBandsToSkip = 8;

    // Variables for drawing the spectrum:
    // Declare a scaling factor for adjusting the height of the rectangles
    float scale = 3;
    // Declare a drawing variable for calculating the width of the
    int barWidth;
    float lowPowerSum = 0f;

    int spectrographHeight = 400;
    boolean hit = false;

    int growthAmount = 0;
    boolean growthDirection = true;
    long lastHitTime = 0;

    PShader blur;

    public void settings() {
        size(1440, 860, P2D);
    }

    public void setup() {
        colorMode(HSB, 1, 1, 1, 1);
        blur = loadShader("sepBlur.glsl");

        // Calculate the width of the rects depending on how many bands we have
        barWidth = (int) (width/((float) bandsToDisplay));

        // Load and play a soundfile and loop it.
        soundFile = new SoundFile(this, "Prometheus.wav");
        soundFile.loop();

        // Create the FFT analyzer and connect the playing soundfile to it.
        fft = new FFT(this, bands);
        fft.input(soundFile);

        beatDetector = new BeatDetector(this);
        beatDetector.input(soundFile);
        beatDetector.sensitivity(20);
    }

    public void draw() {
        // Perform the analysis
        fft.analyze();
        hit = false;
        growthAmount = growthDirection ? growthAmount + 1 : growthAmount - 1;

        for (int i = 0; i < bands; i++) {
            // Smooth the FFT spectrum data by smoothing factor
            sum[i] += (fft.spectrum[i] - sum[i]) * smoothingFactor;

            if (i > numberOfLowBandsToSkip + 3 && i < numberOfLowBandsToSkip + 10) {
                lowPowerSum += sum[i];
            }
        }

        filter(blur);
        noStroke();
        textSize(20);
        fill(1, 1, 1);

        lowPowerSum = lowPowerSum / 7f;
        text(lowPowerSum, 50, 600);

        // background(0);
        if (lowPowerSum > .15) {
            long now = System.currentTimeMillis();
            if (now - lastHitTime > 500) {
                hit = true;
                growthAmount = 0;
                lastHitTime = now;
            }
        }

        for (int i = 0; i < bandsToDisplay; i++) {
            float power = sum[i + numberOfLowBandsToSkip];

            float offset = hit ? 0.3f : 0;
            fill((power * .45f) + offset, 1 - (power * 4), 1, power * 10);

            int xShiftDirection = i % 2 == 0 ? -1 : 1;
            int x = (width / 2) + (xShiftDirection * (i * barWidth / 2));
            // float height = power * scale * spectrographHeight;
            // float y = (spectrographHeight / 2f) - (height / 2f);
            float barHeight = min(770, 20 + (growthAmount * ( power * 100)));
            // rect(x, y, barWidth, height);
            rect(x, 30, barWidth, barHeight);
            rect((width - x), 800 - barHeight, barWidth, barHeight);

            if (sum[i] > 0.01) {
                textSize(11);
                text(power, x - 5, power * 500 + (height / 2f));
            }
        }

        if (growthAmount > 300) {
            growthAmount = 0;
        }
    }
}