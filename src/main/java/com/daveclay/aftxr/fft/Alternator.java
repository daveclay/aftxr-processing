package com.daveclay.aftxr.fft;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PShader;

public class Alternator extends PApplet {

    public static void main(String[] args) {
        new Alternator().runSketch();
    }

    PeasyCam cam;

    // Create a vector to store the smoothed spectrum data in
    int bandsToDisplay = 100;

    // Declare a drawing variable for calculating the width of the
    float sphereSize;

    Analyzer analyzer;

    int heightGrowthAmount = 0;
    int widthGrowthAmount = 0;
    boolean growthDirection = true;
    float rotationX = 0f;
    float rotationY = 0f;
    float rotationZ = 0f;

    PShader blur;
    PFont monoSmall;
    PFont monoMid;
    PFont impactBig;
    int mediumFontSize = 42;
    int bigFontSize = 500;

    boolean logCoords = false;
    int glitch = 0;
    boolean changeCamera = false;
    int shotIndex = 0;

    int totalFrames = 22500;

    public void settings() {
        // 1280 × 720
        size(1280, 720, P3D);
    }

    public void setup() {
        monoSmall = createFont("Menlo-Regular", 11, true);
        monoMid = createFont("Menlo-Regular", mediumFontSize, true);
        impactBig = createFont("Impact", bigFontSize, true);
        background(0);
        textFont(monoSmall);
//
//        String[] fontNames = PFont.list();
//        for (int i = 0; i < fontNames.length; i++) {
//            System.out.println(fontNames[i]);
//        }

        analyzer = Analyzer.Options.forSketch(this)
                .soundFile("Disassociate 3.wav")
                .lowPowerThreshold(.12f)
                .smoothingFactor(.3f)
                .millisecondsToHoldHit(60)
                .numberOfLowBandsToSkip(2)
                .lowPassCutoff(4)
                .buildAnalyzer();

        cam = new PeasyCam(this, 400);
        cam.lookAt(-197.94, -119.22, -94.51);
        cam.setRotations(-1.33f, -0.75, -.13f);
        cam.setDistance(400);
        cam.setMinimumDistance(200);
        cam.setMaximumDistance(5000);

        colorMode(HSB, 1, 1, 1, 1);
        background(0);
        blur = loadShader("sepBlur.glsl");
        blur.set("blurSize", 3);
        blur.set("sigma", 2f);
        blur.set("horizontalPass", 0);
    }

    interface CameraShot {
        void go(PeasyCam peasyCam);
    }

    CameraShot[] shots = {
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-114.72, 274.54,-44.12);
                    cam.setRotations(-0.73, 1.25,-0.87);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-102.40, 274.39,-29.14);
                    cam.setRotations(-1.35, 0.79,-0.26);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(322.69, 474.56,105.91);
                    cam.setRotations(1.00, 0.86,-0.51);
                    cam.setDistance(591.536475324561);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-1420.55, 2,193.45,-356.4);
                    cam.setRotations(0.95, 1.15,2.94);
                    cam.setDistance(2000.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(0.00, 0.00,0.00);
                    cam.setRotations(-0.08, -1.05,2.01);
                    cam.setDistance(200.0);
                }
            }
    };

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
            // background(.0f, 1f, .1f);
            blur.set("blurSize", (int)lerp);
            blur.set("sigma", lerp);
        }

        float hueOffset = hit ? .01f : 0;
        noFill();

        for (int i = 0; i < bandsToDisplay; i++) {
            // 1 - 4000, apply power between?
            float scale = lerp(1f, 15f, ((float)i / (float)bandsToDisplay));
            float power = analyzer.summedBandValues[i + analyzer.numberOfLowBandsToSkip] * scale;
            if (hit) {
                power *= 1.5f;
            }

            float hue = (power * .3f) + hueOffset;
            float saturation = max(0, .3f - (power));
            float value = 1;

            //noStroke();
            noFill();
            stroke(hue, saturation - .2f, value, lerp(.0f, .8f, power * .8f));

            pushMatrix();
            float depth = lerp(1, 1000, power);
            translate(0, 0, 0);
            if (glitch > 0) {
                sphere((i * 200f));
                glitch++;
                if (glitch == 10000) {
                    glitch = 0;
                }
            } else {
                sphere((i * 20f));
            }
            popMatrix();
        }

        if (heightGrowthAmount > 100) {
            // widthGrowthAmount++;
            // maybe blue?
            heightGrowthAmount += 9;
        }

        if (hit) {
            filter(blur);
        }

        rotationX += .001f;
        rotationY += .001f;
        rotationZ += .001f;

        //cam.setRotations(rotationX, rotationY, rotationZ);

        cam.beginHUD();
        int yPos = 18;
        int xPos = 20;

        int barHeight = 50;
        fill(1, 0, 0, hit ? .1f : .3f);
        rect(0, 0, width, barHeight);
        rect(0, height - barHeight, width, barHeight);

        // texts
        fill(.05f, .2f, 1, .15f);

        textFont(monoMid);
        text("AFTXR", xPos, 2 * yPos + 2);
        textFont(monoSmall);

        fill(1, 0, 1, .3f);
        xPos = 160;
        float[] lookAt = cam.getLookAt();
        String[] lookAtStrs = nfc(lookAt, 2);
        text(" centxr: " + join(lookAtStrs, ", "), xPos, yPos);
        float[] rotations = cam.getRotations();
        String[] rotationStrings = nfc(rotations, 2);
        text("_rotate: " + join(rotationStrings, ", "), xPos, 2 * yPos);
        // text("distance: " + nfc((float)cam.getDistance(), 2), 10, 7 * yPos);

        float powerScaled = analyzer.lowPowerSum * 100;
        String s = String.valueOf(powerScaled);
        if (s.length() < 4) {
            s = "000" + s;
        }
        xPos = 400;
        text("   power: " + s.substring(0, 4), xPos, yPos);
        char i = parseChar(((int)random(0, 64) + 20));
        text("  status: " + (hit ? "E" + ( random(0, 1) > .5f ? "X" : "R") + "R" + i + "R" : "nominal"), xPos, 2 * yPos);

        xPos = 540;
        float percent = ((float)frameCount / (float)totalFrames) * 100f;
        text("delete: " + (nfc(percent, 2)) + "%", xPos, yPos);
        text(" _rate: " + nfc(frameRate, 4), xPos, 2 * yPos);

        xPos = 680;
        text("pr.ject: AFTXR", xPos, yPos);
        text("  dist: " + nfc((float)cam.getDistance(), 0), xPos, 2 * yPos);

        if (logCoords) {
            lookAt = cam.getLookAt();
            lookAtStrs = nfc(lookAt, 2);
            rotations = cam.getRotations();
            rotationStrings = nfc(rotations, 2);
            System.out.println("cam.lookAt(" + lookAtStrs[0] + ", " + lookAtStrs[1] + ","  + lookAtStrs[2] + ");");
            System.out.println("cam.setRotations(" + rotationStrings[0] + ", " + rotationStrings[1] + ","  + rotationStrings[2] + ");");
            System.out.println("cam.setDistance(" + cam.getDistance() + ");");
            logCoords = false;
        }

        if (changeCamera) {
            shots[shotIndex].go(cam);
            rotationX = rotations[0];
            rotationY = rotations[1];
            rotationZ = rotations[2];
            changeCamera = false;
        }

        if (hit) {
            textFont(impactBig);
            textSize(bigFontSize);
            fill(1, .01f, 0, .05f);
            text("AFTXR", 20, bigFontSize);
        }

        cam.endHUD();
    }

    @Override
    public void keyPressed() {
        if (keyCode == 67) {
            // 'c' key
            changeCamera = true;
            shotIndex++;
            if (shotIndex == shots.length) {
                shotIndex = 0;
            }
        } else if (keyCode == 88) {
            // 'x' key
            glitch = 1;
        } else if (keyCode == 76) {
            // 'a' Key
            logCoords = true;
        } else {
            System.out.println(keyCode);
        }
    }
}