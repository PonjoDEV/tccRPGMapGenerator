package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.Model.Prop;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class PropController {
    private static final double COLOR_TOLERANCE = 30.0; // Configurable tolerance threshold
    private RpgMapGeneratorController rpgController;

    // Constructor to initialize rpgController
    public PropController(RpgMapGeneratorController rpgController) {
        this.rpgController = rpgController;
    }

    public BufferedImage resizeImg(Prop prop) {
        // Null check for originalImage
        if (prop.getOriginalImage() == null) {
            throw new IllegalArgumentException("Prop originalImage is null. Cannot resize image for prop: " + prop.getName());
        }

        // Null check for rpgController
        if (rpgController == null) {
            throw new IllegalStateException("RpgMapGeneratorController is not initialized in PropController");
        }

        double heightRatio = (double) prop.getOriginalImage().getHeight() / prop.getHeight();
        double widthRatio = (double) prop.getOriginalImage().getWidth() / prop.getWidth();

        Vector<int[][]> rgbMat = rpgController.getMatrixRGB(prop.getOriginalImage());

        int[][] rOrig = rgbMat.elementAt(0);
        int[][] gOrig = rgbMat.elementAt(1);
        int[][] bOrig = rgbMat.elementAt(2);

        int[][] rRes = new int[prop.getHeight()][prop.getWidth()];
        int[][] gRes = new int[prop.getHeight()][prop.getWidth()];
        int[][] bRes = new int[prop.getHeight()][prop.getWidth()];

        for (int i = 0; i < prop.getHeight(); i++) {
            for (int j = 0; j < prop.getWidth(); j++) {
                int srcY = (int) Math.round(i * heightRatio);
                int srcX = (int) Math.round(j * widthRatio);

                // Ensure we don't go out of bounds
                srcY = Math.min(srcY, prop.getOriginalImage().getHeight() - 1);
                srcX = Math.min(srcX, prop.getOriginalImage().getWidth() - 1);

                rRes[i][j] = rOrig[srcY][srcX];
                gRes[i][j] = gOrig[srcY][srcX];
                bRes[i][j] = bOrig[srcY][srcX];
            }
        }

        // Criação da imagem com base nas matrizes RGB
        BufferedImage resizedImage = new BufferedImage(prop.getWidth(), prop.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < prop.getHeight(); i++) {
            for (int j = 0; j < prop.getWidth(); j++) {
                int r = Math.max(0, Math.min(255, rRes[i][j]));
                int g = Math.max(0, Math.min(255, gRes[i][j]));
                int b = Math.max(0, Math.min(255, bRes[i][j]));

                int rgb = (r << 16) | (g << 8) | b;
                resizedImage.setRGB(j, i, rgb);
            }
        }

        validateFlood(rRes, bRes, gRes, prop.getValidPixels());

        // TODO: SAVE THE NEW RESIZED IMAGE AND VALIDATE PIXELS
        return resizedImage;
    }

    private void validateFlood(int[][] rRes, int[][] gRes, int[][] bRes, int[][] valid) {
        int height = rRes.length;
        int width = rRes[0].length;

        // Get reference color from first pixel (0,0)
        Color referenceColor = new Color(rRes[0][0], gRes[0][0], bRes[0][0]);

        // Initialize comparison pool with reference color
        List<Color> comparisonPool = new ArrayList<>();
        comparisonPool.add(referenceColor);

        boolean[][] visited = new boolean[height][width];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, 0});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int y = current[0];
            int x = current[1];

            if (y < 0 || y >= height || x < 0 || x >= width || visited[y][x]) continue;

            Color currentColor = new Color(rRes[y][x], gRes[y][x], bRes[y][x]);

            // Check if current color matches any color in comparison pool
            boolean isMatch = false;
            for (Color poolColor : comparisonPool) {
                if (currentColor.equals(poolColor)) {
                    isMatch = true;
                    break;
                }
            }

            // If no exact match, check if it's close to reference color
            if (!isMatch) {
                double distance = calculateColorDistance(currentColor, referenceColor);
                if (distance <= COLOR_TOLERANCE) {
                    // Add this new similar color to comparison pool
                    comparisonPool.add(currentColor);
                    isMatch = true;
                }
            }

            if (isMatch) {
                visited[y][x] = true;
                valid[y][x] = 0;  // Mark as invalid

                // Add neighbors to stack
                stack.push(new int[]{y - 1, x});
                stack.push(new int[]{y + 1, x});
                stack.push(new int[]{y, x - 1});
                stack.push(new int[]{y, x + 1});
            }
        }
    }

    // Helper method to calculate Euclidean distance between two colors
    private double calculateColorDistance(Color color1, Color color2) {
        int deltaR = color1.getRed() - color2.getRed();
        int deltaG = color1.getGreen() - color2.getGreen();
        int deltaB = color1.getBlue() - color2.getBlue();

        return Math.sqrt(deltaR * deltaR + deltaG * deltaG + deltaB * deltaB);
    }
}
