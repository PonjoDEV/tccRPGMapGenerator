package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.View.RpgMapGeneratorView;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RpgMapGeneratorController {
    private RpgMapGeneratorView view;
    public RpgMapGeneratorController(RpgMapGeneratorView view){
        this.view = view;
    }

    //Mat being the original image, and zones being the new generated image with the zones of each kind of terrain/assets
    private int[][] matR, matG, matB, zoneR, zoneG, zoneB;

    //deviation means how often the analysed pixel will differ from its surroundings
    //TODO Still needs to find a better way to compare the current pixel to its surroundings
    //canvasMutation means the current pixel chance to change from its original value
    double surroundingWeight, mutationChance, perlinNoise, propDensity;

    int zoneSpread;

    public int getZoneSpread() {
        return zoneSpread;
    }

    public void setZoneSpread(int zoneSpread) {
        this.zoneSpread = zoneSpread;
    }



    public int[][] getMatR() {
        return matR;
    }

    public void setMatR(int[][] matR) {
        this.matR = matR;
    }

    public int[][] getMatG() {
        return matG;
    }

    public void setMatG(int[][] matG) {
        this.matG = matG;
    }

    public int[][] getMatB() {
        return matB;
    }

    public void setMatB(int[][] matB) {
        this.matB = matB;
    }

    public int[][] getZoneR() {
        return zoneR;
    }

    public void setZoneR(int[][] zoneR) {
        this.zoneR = zoneR;
    }

    public int[][] getZoneG() {
        return zoneG;
    }

    public void setZoneG(int[][] zoneG) {
        this.zoneG = zoneG;
    }

    public int[][] getZoneB() {
        return zoneB;
    }

    public void setZoneB(int[][] zoneB) {
        this.zoneB = zoneB;
    }

    public double getSurroundingWeight() {
        return surroundingWeight;
    }

    public void setSurroundingWeight(double surroundingWeight) {
        this.surroundingWeight = surroundingWeight;
    }

    public double getPropDensity() { return propDensity; }

    public void setPropDensity(double propDensity) { this.propDensity = propDensity; }

    public double getSPerlinNoise() {
        return perlinNoise;
    }

    public void setPerlinNoise(double perlinNoise) {
        this.perlinNoise = perlinNoise;
    }

    public double getMutationChance() {
        return mutationChance;
    }

    public void setMutationChance(double mutationChance) {
        this.mutationChance = mutationChance;
    }

    public void fillUpZones(int [][] red, int [][] green, int [][] blue, double surroundingWeight, double mutationChance) {
        //Saving the most used colors from the canvas in case all around the analysed pixel are blank spots
        HashMap<String, Integer> canvasColors = new HashMap<>();
        registerColors(0, red.length, 0, red[0].length, red, blue, green, canvasColors);

        /*
        // Call the view to generate the drop-down menus
        view.createColorDropdowns(canvasColors);
        //*/

        //Ordering the most used color on the whole canvas
        ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors;
        mostUsedGlobalColors = orderColorUsage(canvasColors);

        /* NORMAL PIXEL SELECTION ORDER (LOOKS UNNATURAL)
        for (int i = 0; i < red.length - 1; i++) {
            for (int j = 0; j < red[0].length - 1; j++) {

                int redPx = red[i][j];
                int greenPx = green[i][j];
                int bluePx = blue[i][j];

                //If it's a blank space, it should check its surroundings and adjusting according to it, plus a variation chance
                if (redPx == 255 && greenPx == 255 && bluePx == 255) {
                    //If blank it MUST change its color, so mutationChance chance set to 1
                    changePixel(red, green, blue, i, j, mostUsedGlobalColors, surroundingWeight, 1.0);
                } else {
                    changePixel(red, green, blue, i, j, mostUsedGlobalColors, surroundingWeight, mutationChance);
                }
            }
        }
        */

        /* RANDOM PIXEL SELECTION ORDER*/

        // Vector wich will be used to random select pixels on the scrren to be filled
        int pixelCount = (red.length * red[0].length);
        int[] randomPick = new int[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            randomPick[i] = i;
        }
        shuffleVector(randomPick);

        for (int i = 0; i < pixelCount - 1; i++) {
            int x = randomPick[i] % red.length;
            int y = randomPick[i] / red.length;

            int redPx = red[x][y];
            int greenPx = green[x][y];
            int bluePx = blue[x][y];

            //If it's a blank space, it should check its surroundings and adjusting according to it, plus a variation chance
            if (redPx == 255 && greenPx == 255 && bluePx == 255) {
                //If blank it MUST change its color, so mutationChance chance set to 1
                changePixel(red, green, blue, x, y, mostUsedGlobalColors, surroundingWeight, 1.0);
            } else {
                changePixel(red, green, blue, x, y, mostUsedGlobalColors, surroundingWeight, mutationChance);
            }
        }
        //*/

        /* PERLIN NOISE


            // Instantiate the Perlin noise generator
            PerlinNoise perlin = new PerlinNoise();

            // Create a Perlin noise-based grid for pixel selection
            double[][] noiseGrid = new double[red.length][red[0].length];
            for (int i = 0; i < red.length; i++) {
                for (int j = 0; j < red[0].length; j++) {
                    noiseGrid[i][j] = perlin.noise(i * getSPerlinNoise(), j * getSPerlinNoise()); // Scale down for smoother gradients
                }
            }

            // Process each pixel based on the noise values
            for (int i = 0; i < red.length; i++) {
                for (int j = 0; j < red[0].length; j++) {
                    if (noiseGrid[i][j] > 0.5) { // Use a threshold to determine if it’s a zone to apply color
                        int redPx = red[i][j];
                        int greenPx = green[i][j];
                        int bluePx = blue[i][j];

                        double mutate = mutationChance;
                        if (redPx == 255 && greenPx == 255 && bluePx == 255) {
                            mutate = 1.0;
                        }
                        changePixel(red, green, blue, i, j, mostUsedGlobalColors, surroundingWeight, mutate);
                    }
                }
            }
        //*/
    }

    private ArrayList<Map.Entry<String,Integer>> orderColorUsage (HashMap<String,Integer> usedColors){
        List<Map.Entry<String,Integer>> list = new ArrayList<>(usedColors.entrySet());
        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return new ArrayList<>(list);
    }

    private void registerColors (int begY, int endY, int begX, int endX, int [][] red, int [][] blue, int [][] green, HashMap<String,Integer> pixelColors ) {

        int colorOcurrences = 1;   //number of color occurrences
        String rgbString;

        //If the RGB Value hasn't been registered yet, we add it to the pixelColors Hash with a new ID corresponding to the times of occurrences
        for (int i = begY; i < endY; i++) {
            for (int j = begX; j < endX; j++) {
                if (i < 0 || j < 0 || i >= red.length || j >= red[0].length) {
                    continue;
                }
                if (red[i][j] != 255 || green[i][j] != 255 || blue[i][j] != 255) {
                    rgbString = String.valueOf(red[i][j]) + "," + String.valueOf(green[i][j]) + "," + String.valueOf(blue[i][j]);
                    if (!pixelColors.containsKey(rgbString)) {
                        pixelColors.put(rgbString, 1);
                        //if the value already exists, we add +1 to the counter
                    } else {
                        colorOcurrences = pixelColors.get(rgbString) + 1;
                        pixelColors.put(rgbString, colorOcurrences);
                    }
                }
            }
        }
    }

    private void changePixel(int[][] red, int[][] green, int[][] blue, int i, int j, ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors, double surrondWeight, double mutationChance) {
        if ((Math.random() > mutationChance)) {
            return;
        }
        //Saving the used colors from around the pixel as a hashMap
        HashMap<String, Integer> aroundPixel = new HashMap<>();
        registerColors(i - 1, i + 2, j - 1, j + 2, red, blue, green, aroundPixel);

        //Ordering the aroundPixel HashMap by color usage
        ArrayList<Map.Entry<String, Integer>> mostUsedColor;
        mostUsedColor = orderColorUsage(aroundPixel);

        int[] rgb;
        int pickedColor = 0;
        //Selecting color by usage and surroundWeight

        //If all spaces around the analysed are blank it should use the most present value on the screen
        if (mostUsedColor.isEmpty()) {
            pickedColor = colorSelection(surrondWeight, mostUsedGlobalColors.size());
            rgb = stringToRGB(mostUsedGlobalColors.get(pickedColor).getKey());
        } else {
            pickedColor = colorSelection(surrondWeight, mostUsedColor.size());
            rgb = stringToRGB(mostUsedColor.get(pickedColor).getKey());
        }

        red[i][j] = rgb[0];
        green[i][j] = rgb[1];
        blue[i][j] = rgb[2];
    }

    //Will iterate through a number of colors then pick one
    private int colorSelection(double surroundWeight, int nColors) {
        int pick=0;
        for (int i = 1; i < nColors; i++) {
            if (Math.random() > surroundWeight) {
                pick = i;
            } else {
                break;
            }
        }
        return pick;
    }

    private int[] stringToRGB (String colorValue){
        int [] rgb = new int[3];
        String [] rgbCode = colorValue.split(",");

        for (int i = 0; i < 3; i++) {
            rgb[i] = Integer.parseInt(rgbCode[i]);
        }
        return rgb;
    }

    public Vector<int[][]> getMatrixRGB(BufferedImage img) {
        int[][] rmat = new int[img.getHeight()][img.getWidth()];
        int[][] gmat = new int[img.getHeight()][img.getWidth()];
        int[][] bmat = new int[img.getHeight()][img.getWidth()];

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int[] rgb = getPixelData(img, j, i);
                rmat[i][j] = rgb[0];
                gmat[i][j] = rgb[1];
                bmat[i][j] = rgb[2];
            }
        }

        Vector<int[][]> rgb = new Vector<>();
        rgb.add(rmat);
        rgb.add(gmat);
        rgb.add(bmat);
        return rgb;
    }

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[] {
                (argb >> 16) & 0xff, //red
                (argb >>  8) & 0xff, //green
                (argb      ) & 0xff  //blue
        };

        return rgb;
    }

    public void generateZones(BufferedImage canvas) {
        // Check if the canvas image is available
        if (canvas == null) {
            System.out.println("Canvas image is null. Cannot generate zones.");
        } else {
            Vector<int[][]> rgbMat = getMatrixRGB(canvas);
            setMatR(rgbMat.elementAt(0));
            setMatG(rgbMat.elementAt(1));
            setMatB(rgbMat.elementAt(2));

            copyMatToZone();

            // If both changeCanvas and deviation are too high the results are not good,
            // Especially with high changeCanvas values
            // Recommended values: deviation 0.8 and changeCanvas 0.2
            fillUpZones(zoneR, zoneG, zoneB, getSurroundingWeight(), getMutationChance());
        }
    }

    private void copyMatToZone(){
        int height = matR.length;
        int width = matR[0].length;

        zoneR = new int[height][width];
        zoneG = new int[height][width];
        zoneB = new int[height][width];

        for (int i = 0; i < matR.length; i++) {
            for (int j = 0; j < matR[0].length; j++) {
                zoneR[i][j] = matR[i][j];
                zoneG[i][j] = matG[i][j];
                zoneB[i][j] = matB[i][j];
            }
        }
    }

    private void shuffleVector(int[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}
