package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.sound.*;

public class SpectrographTest extends PApplet {

    public static void main(String[] args) {
        new SpectrographTest().runSketch();
    }

    // Declare the sound source and FFT analyzer variables
    SoundFile sample;
    FFT fft;

    // Define how many FFT bands to use (this needs to be a power of two)
    int bands = 4096;

    // Define a smoothing factor which determines how much the spectrums of consecutive
    // points in time should be combined to create a smoother visualisation of the spectrum.
    // A smoothing factor of 1.0 means no smoothing (only the data from the newest analysis
    // is rendered), decrease the factor down towards 0.0 to have the visualisation update
    // more slowly, which is easier on the eye.
    float smoothingFactor = 0.9f;

    // Create a vector to store the smoothed spectrum data in
    float[] sum = new float[bands];
    int bandsToDisplay = 200;
    int numberOfLowBandsToSkip = 8;

    // Variables for drawing the spectrum:
    // Declare a scaling factor for adjusting the height of the rectangles
    float scale = 3;
    // Declare a drawing variable for calculating the width of the
    float barWidth;
    float lowPowerSum = 0f;

    int spectrographHeight = 400;
    boolean hit = false;

    public void settings() {
        size(1440, 860);
    }

    public void setup() {
        background(0);
        colorMode(HSB, 1, 1, 1, 1);

        // Calculate the width of the rects depending on how many bands we have
        barWidth = width/(float) bandsToDisplay;

        // Load and play a soundfile and loop it.
        sample = new SoundFile(this, "Rigit Body III video.wav");
        sample.loop();

        // Create the FFT analyzer and connect the playing soundfile to it.
        fft = new FFT(this, bands);
        fft.input(sample);
    }

    public void draw() {
        // Perform the analysis
        fft.analyze();
        hit = false;

        for (int i = 0; i < bands; i++) {
            // Smooth the FFT spectrum data by smoothing factor
            sum[i] += (fft.spectrum[i] - sum[i]) * smoothingFactor;

            if (i > numberOfLowBandsToSkip + 2 && i < numberOfLowBandsToSkip + 12) {
                lowPowerSum += sum[i];
            }
        }

        lowPowerSum = lowPowerSum / 10f;

        if (lowPowerSum > .15) {
            background(.99f, 1, .6f);
        } else {
            background(0);
        }

        noStroke();

        textSize(20);
        fill(1, 1, 1);
        text(lowPowerSum, 50, 600);


        for (int i = numberOfLowBandsToSkip; i < bands; i++) {
            float power = sum[i];
            fill(power * .45f, 1, 1, power * 10);

            float x = (i - numberOfLowBandsToSkip) * barWidth;
            float y = power * scale * spectrographHeight;
            // Draw the rectangles, adjust their height using the scale factor

            rect(x % width, (spectrographHeight / 2f) - (y / 2f), barWidth, y, .4f);

            if (sum[i] > 0.01) {
                textSize(11);
                text(i, x - 5, spectrographHeight - 50);
                text(power, x - 5, spectrographHeight - 10);
            }
        }
    }
}