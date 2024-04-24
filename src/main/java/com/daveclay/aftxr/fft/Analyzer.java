package com.daveclay.aftxr.fft;

import processing.core.PApplet;
import processing.sound.FFT;
import processing.sound.SoundFile;

public class Analyzer {

    public static class Options {
        PApplet parent;
        String soundFile;
        int bands = 4096;
        float smoothingFactor = 0.7f;
        int numberOfLowBandsToSkip = 8;
        int millisecondsToHoldHit = 200;
        float lowPowerThreshold = .1f;
        int lowPassCutoff;

        private Options(PApplet sketch) {
            this.parent = sketch;
        }

        public static Options forSketch(PApplet sketch) {
            return new Options(sketch);
        }

        public Options soundFile(String soundFile) {
            this.soundFile = soundFile;
            return this;
        }

        /**
         * Define how many FFT bands to use (this needs to be a power of two)
         */
        public Options bands(int bands) {
            this.bands = bands;
            return this;
        }

        /**
         * Define a smoothing factor which determines how much the spectrums of consecutive
         * points in time should be combined to create a smoother visualisation of the spectrum.
         * A smoothing factor of 1.0 means no smoothing (only the data from the newest analysis
         * is rendered), decrease the factor down towards 0.0 to have the visualisation update
         * more slowly, which is easier on the eye.
         */
        public Options smoothingFactor(float smoothingFactor) {
            this.smoothingFactor = smoothingFactor;
            return this;
        }

        public Options numberOfLowBandsToSkip(int numberOfLowBandsToSkip) {
            this.numberOfLowBandsToSkip = numberOfLowBandsToSkip;
            return this;
        }

        public Options lowPassCutoff(int lowPassCutoff) {
            this.lowPassCutoff = lowPassCutoff;
            return this;
        }

        public Options millisecondsToHoldHit(int millisecondsToHoldHit) {
            this.millisecondsToHoldHit = millisecondsToHoldHit;
            return this;
        }

        public Options lowPowerThreshold(float lowPowerThreshold) {
            this.lowPowerThreshold = lowPowerThreshold;
            return this;
        }

        public Analyzer buildAnalyzer() {
            return new Analyzer(this);
        }
    }

    private final PApplet parent;
    private final SoundFile soundFile;
    private final FFT fft;
    private final int bands;
    private final float smoothingFactor;
    private final float millisecondsToHoldHit;
    private final float lowPowerThreshold;
    private final int lowPassCutoff;

    // calculated
    private long lastHitTime = 0;

    // public state
    public final float[] summedBandValues;
    public final int numberOfLowBandsToSkip;

    // public calculated values
    public long timeSinceHit = -1;
    public boolean lowPassTriggered = false;
    public boolean lowPassHold = false;
    public float lowPowerSum = 0f;

    public Analyzer(Options analyzerOptions) {
        // Load and play a soundfile and loop it.
        this.parent = analyzerOptions.parent;

        this.soundFile = new SoundFile(parent, analyzerOptions.soundFile);
        soundFile.loop();

        this.bands = analyzerOptions.bands;

        // Create the FFT analyzer and connect the playing soundfile to it.
        this.fft = new FFT(parent, bands);
        fft.input(soundFile);

        this.summedBandValues = new float[bands];

        this.smoothingFactor = analyzerOptions.smoothingFactor;
        this.numberOfLowBandsToSkip = analyzerOptions.numberOfLowBandsToSkip;
        this.millisecondsToHoldHit = analyzerOptions.millisecondsToHoldHit;
        this.lowPowerThreshold = analyzerOptions.lowPowerThreshold;
        this.lowPassCutoff = analyzerOptions.lowPassCutoff;
    }

    public void update() {
        lowPassTriggered = false;

        fft.analyze();
        calculateLowPowerSum();

        if (lowPassHold) {
            timeSinceHit = System.currentTimeMillis() - lastHitTime;
            if (timeSinceHit > millisecondsToHoldHit && lowPowerSum < lowPowerThreshold) {
                lastHitTime = 0;
                timeSinceHit = -1;
                lowPassTriggered = false;
                lowPassHold = false;
            }
        } else {
            if (lowPowerSum >= lowPowerThreshold) {
                long now = System.currentTimeMillis();
                lowPassTriggered = true;
                lowPassHold = true;
                lastHitTime = now;
                timeSinceHit = 0;
            }
        }
    }

    private void calculateLowPowerSum() {
        for (int i = 0; i < bands; i++) {
            // Smooth the FFT spectrum data by smoothing factor
            summedBandValues[i] += (fft.spectrum[i] - summedBandValues[i]) * smoothingFactor;

            if (withinLowPassRange(i)) {
                lowPowerSum += summedBandValues[i];
            }
        }
        lowPowerSum = lowPowerSum / (float) numberOfLowBandsToSkip;
    }

    private boolean withinLowPassRange(int i) {
        return i > numberOfLowBandsToSkip && i < numberOfLowBandsToSkip + lowPassCutoff;
    }
}
