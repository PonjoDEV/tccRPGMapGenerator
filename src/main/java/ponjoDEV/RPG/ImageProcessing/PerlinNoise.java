package ponjoDEV.RPG.ImageProcessing;

import java.util.Random;

public class PerlinNoise {private int[] p; // Permutation array

    public PerlinNoise() {
        p = new int[512];
        Random rand = new Random();
        int[] permutation = new int[256];
        for (int i = 0; i < 256; i++) permutation[i] = i;
        for (int i = 0; i < 256; i++) {
            int j = rand.nextInt(256 - i) + i;
            int tmp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = tmp;
        }
        System.arraycopy(permutation, 0, p, 0, 256);
        System.arraycopy(permutation, 0, p, 256, 256);
    }

    public double noise(double x, double y) {
        // Implement basic Perlin noise based on input coordinates
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);

        int a = p[X] + Y;
        int b = p[X + 1] + Y;

        return lerp(v, lerp(u, grad(p[a], x, y), grad(p[b], x - 1, y)),
                lerp(u, grad(p[a + 1], x, y - 1), grad(p[b + 1], x - 1, y - 1)));
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }
    private double grad(int hash, double x, double y) { return ((hash & 1) == 0 ? x : -x) + ((hash & 2) == 0 ? y : -y); }
}
