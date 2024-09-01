package ponjoDEV.RPG.ImageProcessing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RpgMapGeneratorController {

    //Mat being the original image, and zones being the new generated image with the zones of each kind of terrain/assets
    private int[][] matR, matG, matB, zoneR, zoneG, zoneB;

    //Factors that will determine how the image will be altered
    double chunkDensity, deviation, canvasMutation;

    //Used pixel colors of the original image, each color will represent a zone of the map with its own objects and characteristics
    private HashMap<String,Integer> pixelColors = new HashMap<>();


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

    public double getChunkDensity() {
        return chunkDensity;
    }

    public void setChunkDensity(double chunkDensity) {
        this.chunkDensity = chunkDensity;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public double getCanvasMutation() {
        return canvasMutation;
    }

    public void setCanvasMutation(double canvasMutation) {
        this.canvasMutation = canvasMutation;
    }

    public void fillUpCanvas(int [][] red, int [][] green, int [][] blue, double deviation, int chunks, double changeCanvas) {
        //Saving the most used colors from the canvas in case all around the analysed pixel are blank spots
        HashMap<String, Integer> canvasColors = new HashMap<>();
        registerColors(0, red.length, 0, red[0].length, red, blue, green, canvasColors);

        //Ordering the most used color on the whole canvas
        ArrayList<Map.Entry<String, Integer>> muCanvas;
        muCanvas = orderColorUsage(canvasColors);

        //TODO divide into smaller chunks/sectors so it looks less artificial, the more the better, it may also be usefull to do it using multi threads
        for (int n = 0; n < 1; n++) {

            //TODO erase after adjusting the smaller chunk division
            for (int i = 1; i < red.length - 1; i++) {
                for (int j = 1; j < red[0].length - 1; j++) {

                    int redPx = red[i][j];
                    int greenPx = green[i][j];
                    int bluePx = blue[i][j];


                    //TODO need to initialize, maybe as a bigger size screen
                        /*
                        zoneR[i][j] = red[i][j];
                        zoneG[i][j] = green[i][j];
                        zoneB[i][j] = blue[i][j];
                        */

                    //If it's a blank space, it should check its surroundings and adjusting according to it, plus a variation chance
                    if (redPx == 255 && greenPx == 255 && bluePx == 255) {
                        //If blank it MUST change its color, so changeCanvas chance set to 1
                        changePixel(red, green, blue, i, j, muCanvas, deviation, 1);
                    } else {
                        changePixel(red, green, blue, i, j, muCanvas, deviation, changeCanvas);
                    }
                }
            }
        }

    }

    private ArrayList<Map.Entry<String,Integer>> orderColorUsage (HashMap<String,Integer> usedColors){
        List<Map.Entry<String,Integer>> list = new ArrayList<>(usedColors.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
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
        //System.out.println(pixelColors);
    }

    private void changePixel(int[][] red, int[][] green, int[][] blue, int i, int j, ArrayList<Map.Entry<String, Integer>> muCanvas, double deviation, double changeCanvas) {
        if (Math.random()<=changeCanvas) {
            //Saving the used colors from around the pixel as a hashMap
            HashMap<String, Integer> aroundPixel = new HashMap<>();
            registerColors(i - 1, i + 2, j - 1, j + 2, red, blue, green, aroundPixel);

            //DONE order the aroundPixel HashMap by color usage
            ArrayList<Map.Entry<String, Integer>> mostUsedColor;
            mostUsedColor = orderColorUsage(aroundPixel);

            int[] rgb;
            int pickedColor = 0;
            //DONE select color by usage and deviation

            //DONE if all spaces around are blank it should use the most present value on the screen
            if (mostUsedColor.isEmpty()) {
                pickedColor = deviationColor(deviation, muCanvas.size());
                //System.out.println("Rounded by just blank spaces");
                rgb = stringToRGB(muCanvas.get(pickedColor).getKey());
                //System.out.println(Arrays.toString(rgb));

                //paint the blank space as the selected color
                red[i][j] = rgb[0];
                green[i][j] = rgb[1];
                blue[i][j] = rgb[2];

            } else {
                pickedColor = deviationColor(deviation, mostUsedColor.size());
                //System.out.println(Arrays.toString(rgb));
                rgb = stringToRGB(mostUsedColor.get(pickedColor).getKey());
                //System.out.println(Arrays.toString(rgb));

                //paint the blank space as the selected color
                red[i][j] = rgb[0];
                green[i][j] = rgb[1];
                blue[i][j] = rgb[2];
            }
        }
    }

    //Will iterate through a number of colors then pick one
    private int deviationColor(double deviation, int nColors) {
        int pick=0;
        for (int i = 1; i < nColors; i++) {
            if (Math.random() <= deviation) {
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

    public Vector<int[][]> getMatrixRGB(String path){
        BufferedImage img;
        int[][] rmat = null;
        int[][] gmat = null;
        int[][] bmat = null;
        try {
            img = ImageIO.read(new File(path));

            int[][] pixelData = new int[img.getHeight() * img.getWidth()][3];
            rmat = new int[img.getHeight()][img.getWidth()];
            gmat = new int[img.getHeight()][img.getWidth()];
            bmat = new int[img.getHeight()][img.getWidth()];

            int counter = 0;
            for(int i = 0; i < img.getHeight(); i++){
                for(int j = 0; j < img.getWidth(); j++){
                    rmat[i][j] = getPixelData(img, j, i)[0];
                    gmat[i][j] = getPixelData(img, j, i)[1];
                    bmat[i][j] = getPixelData(img, j, i)[2];

                    /*for(int k = 0; k < rgb.length; k++){
                        pixelData[counter][k] = rgb[k];
                    }*/
                    counter++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Vector<int[][]> rgb = new Vector<int[][]>();
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

    public void generateZones(String path) {

        Vector<int[][]> rgbMat = getMatrixRGB(path);
        setMatR(rgbMat.elementAt(0));
        setMatG(rgbMat.elementAt(1));
        setMatB(rgbMat.elementAt(2));

        setZoneR(getMatR());
        setZoneG(getMatG());
        setZoneB(getMatB());

        int chunks =(int) Math.pow(4,(this.getChunkDensity() -1));

        //If both changeCanvas and deviation are too high the results are not too good, SPECIALLY THE changeCanvas VALUE
        //deviation 0.8 and changeCanvas 0.2 wields good results
        this.fillUpCanvas(zoneR, zoneG, zoneB, getDeviation(), chunks, getCanvasMutation());
    }
}
