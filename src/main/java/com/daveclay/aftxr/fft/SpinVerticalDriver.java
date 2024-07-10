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
    float rotationX = 0f;
    float rotationY = 0f;
    float rotationZ = 0f;

    PShader blur;
    PFont mono11;
    PFont monoBig;
    int bigFontSize = 500;

    boolean logCoords = false;
    int shotIndex = 0;

    int totalFrames = 10000;

    public void settings() {
        // 1280 × 720
        size(1280, 720, P3D);
    }

    public void setup() {
        mono11 = loadFont("PTMono-Regular-11.vlw");
        monoBig = createFont("Impact", bigFontSize, true);
        background(0);
        textFont(mono11);
//
//        String[] fontNames = PFont.list();
//        for (int i = 0; i < fontNames.length; i++) {
//            System.out.println(fontNames[i]);
//        }

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

    interface CameraShot {
        void go(PeasyCam peasyCam);
    }

    CameraShot[] shots = {
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-3.20, 103.84, -67.69);
                    cam.setRotations(-1.07, -0.88, 0.31);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(154.03, -19.03,-153.57);
                    cam.setRotations(-0.23, -0.95,1.34);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-109.69, 170.15,84.07);
                    cam.setRotations(-1.82, 0.77,0.07);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-110.49, 178.48,47.49);
                    cam.setRotations(-1.33, -0.78,0.07);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(220.98, -0.39,-90.25);
                    cam.setRotations(1.30, 0.38,-1.23);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(220.98, -0.39,-90.25);
                    cam.setRotations(0.53, 1.15,-0.75);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-175.20, -45.56,22.06);
                    cam.setRotations(-0.99, -0.93,0.64);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(30.96, -218.39,-68.75);
                    cam.setRotations(-1.35, 0.69,-3.08);
                    cam.setDistance(1334.44983916654);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-157.17, 92.40,-47.61);
                    cam.setRotations(-1.54, -1.04,-0.36);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(117.60, -55.05,57.42);
                    cam.setRotations(1.43, 0.96,2.93);
                    cam.setDistance(1458.0260133938123);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-8.18, 135.31,55.35);
                    cam.setRotations(1.58, -0.99,-2.51);
                    cam.setDistance(1206.2815985154587);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(-581.65, -33.54, -266.78);
                    cam.setRotations(-0.09, -0.51, -0.03);
                    cam.setDistance(400.0);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(186.50, -100.01,46.82);
                    cam.setRotations(0.16, -1.39,-2.07);
                    cam.setDistance(956.4208356316658);
                }
            },
            new CameraShot() {
                @Override
                public void go(PeasyCam peasyCam) {
                    cam.lookAt(33.54, 388.19,-163.56);
                    cam.setRotations(-0.34, 0.74,3.05);
                    cam.setDistance(400.0);
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

        rotationX += .001f;
        rotationY += .001f;
        rotationZ += .001f;

        //cam.setRotations(rotationX, rotationY, rotationZ);

        cam.beginHUD();
        int yPos = 18;
        int xPos = 20;
        textFont(mono11);
        textSize(11);

        int barHeight = 50;
        fill(1, 0, 0, hit ? .1f : .3f);
        rect(0, 0, width, barHeight);
        rect(0, height - barHeight, width, barHeight);

        fill(1, 0, 1, .3f);
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
        xPos = 260;
        text("   power: " + s.substring(0, 4), xPos, yPos);
        char i = parseChar(((int)random(0, 64) + 20));
        text("  status: " + (hit ? "E" + ( random(0, 1) > .5f ? "X" : "R") + "R" + i + "R" : "-----"), xPos, 2 * yPos);

        xPos = 400;
        float percent = ((float)frameCount / (float)totalFrames) * 100f;
        text("delete: " + (nfc(percent, 2)) + "%", xPos, yPos);
        text(" _rate:" + nfc(frameRate, 4), xPos, 2 * yPos);

        xPos = 540;
        text("pr.ject: AFTXR", xPos, yPos);
        text("  count: " + frameCount, xPos, 2 * yPos);

        if (logCoords) {
            lookAt = cam.getLookAt();
            lookAtStrs = nfc(lookAt, 2);
            rotations = cam.getRotations();
            rotationStrings = nfc(rotations, 2);
            System.out.println("cam.lookAt(" + lookAtStrs[0] + ", " + lookAtStrs[1] + ","  + lookAtStrs[2] + ");");
            System.out.println("cam.setRotations(" + rotationStrings[0] + ", " + rotationStrings[1] + ","  + rotationStrings[2] + ");");
            System.out.println("cam.setDistance(" + cam.getDistance() + ");");

            shots[shotIndex].go(cam);
            rotationX = rotations[0];
            rotationY = rotations[1];
            rotationZ = rotations[2];
            logCoords = false;
        }

        if (hit) {
            textFont(monoBig);
            textSize(bigFontSize);
            fill(1, .01f, 0, .05f);
            text("AFTXR", 20, bigFontSize);
        }

        cam.endHUD();
        // camera(xpos, ypos, zpos, 0, 0, 0, -1, -1, 0);
    }

    @Override
    public void keyPressed() {
        if (keyCode == 65) {
            // 'a' Key
            logCoords = true;
            shotIndex++;
            if (shotIndex == shots.length) {
                shotIndex = 0;
            }
        }
    }
}