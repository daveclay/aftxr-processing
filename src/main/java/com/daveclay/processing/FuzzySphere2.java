package com.daveclay.processing;

import processing.core.PApplet;

import java.awt.*;

public class FuzzySphere2 extends PApplet {

    public static void main(String[] args) {
        new FuzzySphere2().runSketch();
    }

    /**
     * Noise Sphere
     * by David Pena.
     *
     * Uniform random distribution on the surface of a sphere.
     */

    int count = 8000;
    Pelo[] lines;
    float radius;
    float rx = 0;
    float ry = 0;
    float hue = 0f;
    float saturation = 0f;
    float rotation = 0f;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        radius = height / 5f;
        lines = new Pelo[count];
        for (int i = 0; i< count; i++) {
            lines[i] = new Pelo(i);
        }
        noiseDetail(5);
    }

    public void draw() {
        background(0);

        float orbitRadius= mouseX / 2f + 50;
        float ypos = mouseY / 3f;
        float xpos = cos(radians(rotation)) * orbitRadius;
        float zpos = sin(radians(rotation)) * orbitRadius;

        translate(width/2f, height/2f);

        camera(xpos, ypos, zpos, 0, 0, 0, 0, -1, 0);

        rotation += .1f;

        // rotates base sphere by mouse movement
        float rxp = ((mouseX-(width/2f)) * 0.005f);
        float ryp = ((mouseY-(height/2f)) * 0.005f);
        rx = ((rx*0.9f) + (rxp*0.1f));
        ry = ((ry*0.9f) + (ryp*0.1f));
        rotateY(rx);
        rotateX(ry);

        for (int i = 0; i < count; i++) {
            lines[i].update();
        }

    }

    class Pelo {
        int index;

        public Pelo(int index) {
            this.index = index;
        }

        float z = random(-(radius * 2), radius * 2);

        // spread on the sphere/shape (evenly is TWO_PI)
        float phi = random(TWO_PI);

        // distance/spread
        float largo = random(2.15f, 2.4f);

        // asin for a sphere, atan for underwear, cos for a crown/iris, sin for a torus
        float theta = cos(z/ radius);

        void update() {
            // wiggle amounts
            float v1 = (mouseX * .001f) * 0.3f; // orig: 0.3f
            float v2 = (mouseY * .001f) * 0.7f; // orig: 0.5f

            float thetaJitter = 0.005f; // original 0.0005f, anxiety: 0.005f
            float positionJitter = 0.0007f; // original 0.0007f
            float wiggleDampeningFactory = 10f; // original 0.01f; 10 is like hair

            float offsetForTheta = (noise(millis() * thetaJitter, sin(phi)) - v2) * v1;
            float offsetForPhi = (noise(millis() * positionJitter, sin(z) * wiggleDampeningFactory) - v2) * v1;

            float thetaOffset = theta + offsetForTheta;
            float phiOffset = phi + offsetForPhi;

            float x = radius * cos(theta) * cos(phi);
            float y = radius * cos(theta) * sin(phi);
            float z = radius * sin(theta);

            float xo = radius * cos(thetaOffset) * cos(phiOffset);
            float yo = radius * cos(thetaOffset) * sin(phiOffset);
            float zo = radius * sin(thetaOffset);

            float xb = xo * largo;
            float yb = yo * largo;
            float zb = zo * largo;

            beginShape(TRIANGLE_STRIP);

            hue = .0f;
            saturation = 0; //index % 2 == 0 ? 0f : 1f; // lerp(.05f, .16f, thetaOffset);
            // saturation = lerp(.18f, .2f, thetaOffset);

            int color = Color.HSBtoRGB(hue, saturation, 1);
            fill(color, 50);
            //stroke(color, 80);
            noStroke();
            vertex(x, y, z);
            // stroke(color, 20);
            vertex(xb, yb, zb);
            // creating weird shapes:
            float width = 100;
            vertex(xb + width, yb + width, zb + width);
            endShape();
        }
    }
}
