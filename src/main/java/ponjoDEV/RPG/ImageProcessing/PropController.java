package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.Model.Prop;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class PropController {
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

        int heightRatio = (int) prop.getOriginalImage().getHeight() / prop.getHeight();
        int widthRatio = (int) prop.getOriginalImage().getWidth() / prop.getWidth();

        Vector<int[][]> rgbMat = rpgController.getMatrixRGB(prop.getOriginalImage());

        int[][] rOrig = rgbMat.elementAt(0);
        int[][] gOrig = rgbMat.elementAt(1);
        int[][] bOrig = rgbMat.elementAt(2);

        int[][] rRes = new int[prop.getHeight()][prop.getWidth()];
        int[][] gRes = new int[prop.getHeight()][prop.getWidth()];
        int[][] bRes = new int[prop.getHeight()][prop.getWidth()];

        for (int i = 0; i < prop.getHeight(); i++) {
            for (int j = 0; j < prop.getWidth(); j++) {
                rRes[i][j] = rOrig[i * heightRatio][j * widthRatio];
                gRes[i][j] = gOrig[i * heightRatio][j * widthRatio];
                bRes[i][j] = bOrig[i * heightRatio][j * widthRatio];
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

        int targetR = rRes[0][0];
        int targetG = gRes[0][0];
        int targetB = bRes[0][0];

        boolean[][] visited = new boolean[height][width];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, 0});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int y = current[0];
            int x = current[1];

            if (y < 0 || y >= height || x < 0 || x >= width || visited[y][x]) continue;

            if (rRes[y][x] == targetR && gRes[y][x] == targetG && bRes[y][x] == targetB) {
                visited[y][x] = true;

                valid[y][x] = 0;  // marca como inválido

                // Vizinhos
                stack.push(new int[]{y - 1, x});
                stack.push(new int[]{y + 1, x});
                stack.push(new int[]{y, x - 1});
                stack.push(new int[]{y, x + 1});
            }
        }
    }
}