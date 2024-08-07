package com.daveclay.processing;

import processing.core.PApplet;

public class FuzzySphere extends PApplet {

    public static void main(String[] args) {
        new FuzzySphere().runSketch();
    }

    /**
     * Noise Sphere
     * by David Pena.
     *
     * Uniform random distribution on the surface of a sphere.
     */

    int cuantos = 4000;
    Pelo[] lista ;
    float[] z = new float[cuantos];
    float[] phi = new float[cuantos];
    float[] largos = new float[cuantos];
    float radio;
    float rx = 0;
    float ry =0;

    public void settings() {
        size(1440, 860, P3D);
    }

    public void setup() {
        radio = height/3;
        lista = new Pelo[cuantos];
        for (int i=0; i<cuantos; i++) {
            lista[i] = new Pelo();
        }
        noiseDetail(3);
    }

    public void draw() {
        background(0);
        translate(width/2f, height/2f);

        float rxp = ((mouseX-(width/2f)) * 0.005f);
        float ryp = ((mouseY-(height/2f)) * 0.005f);
        rx = ((rx*0.9f) + (rxp*0.1f));
        ry = ((ry*0.9f) + (ryp*0.1f));
        rotateY(rx);
        rotateX(ry);
        fill(0);
        noStroke();
        sphere(radio);

        for (int i = 0;i < cuantos; i++) {
            lista[i].dibujar();
        }
    }


    class Pelo {

        float z = random(-radio, radio);
        float phi = random(TWO_PI);
        float largo = random(1.15f, 1.2f);
        float theta = asin(z/radio);

        void dibujar() {
            float off = (noise(millis() * 0.0005f, sin(phi))-0.5f) * 0.3f;
            float offb = (noise(millis() * 0.0007f, sin(z) * 0.01f)-0.5f) * 0.3f;

            float thetaff = theta+off;
            float phff = phi+offb;
            float x = radio * cos(theta) * cos(phi);
            float y = radio * cos(theta) * sin(phi);
            float z = radio * sin(theta);

            float xo = radio * cos(thetaff) * cos(phff);
            float yo = radio * cos(thetaff) * sin(phff);
            float zo = radio * sin(thetaff);

            float xb = xo * largo;
            float yb = yo * largo;
            float zb = zo * largo;

            beginShape(LINES);
            stroke(0);
            vertex(x, y, z);
            stroke(200, 150);
            vertex(xb, yb, zb);
            endShape();
        }
    }
}
