package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.Model.Zone;
import ponjoDEV.RPG.View.RpgMapGeneratorView;

import java.awt.image.BufferedImage;
import java.util.*;

public class RpgMapGeneratorController {
    private RpgMapGeneratorView view;
    public RpgMapGeneratorController(RpgMapGeneratorView view){
        this.view = view;
    }

    //Mat being the original image, and zones being the new generated image with the zones of each kind of terrain/assets
    private int[][] matR, matG, matB, zoneR, zoneG, zoneB, drawn, spreaded;

    //deviation means how often the analysed pixel will differ from its surroundings
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

    public void setMatR(int[][] matR) { this.matR = matR; }

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

    public int[][] getZoneG() { return zoneG; }

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

    public void fillUpZones(int [][] red, int [][] green, int [][] blue) {
        //Saving the most used colors from the canvas in case all around the analysed pixel are blank spots
        HashMap<String, Integer> canvasColors = new HashMap<>();
        registerColors(0, red.length, 0, red[0].length, red, blue, green, canvasColors);

        //Ordering the most used color on the whole canvas
        ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors;
        mostUsedGlobalColors = orderColorUsage(canvasColors);

        //Creating a array of Zones to save the drawn zones information
        ArrayList<Zone> zones = new ArrayList<>();
        registerDrawnZones(red, green, blue, zones);

        boolean thereIsWhite = true;

        while(thereIsWhite) {

            zones.sort(Comparator.comparingInt(Zone::getPriority));
            thereIsWhite = spreadDrawnZones(red, green, blue, zones);

            for (Zone zone : zones) {
                System.out.println("Zone " + zone.getTag() + "\nZona minima Y: " + zone.getBegY() + " Zona minima X: " + zone.getBegX() + "\nZona maxima Y: " + zone.getEndY() + " Zona maxima X: " + zone.getEndX() + "\nTipo " + zone.getType());
                System.out.println("Y inicial: " + zone.getInitCoord()[0] + " X inicial " + zone.getInitCoord()[1]+"\n Zone Size :"+zone.getSize());
            }

            zones.clear();

            //TODO registerSpreadedZones(red, green, blue, zones);

            copyZoneToMat();
            randomFill(red, green, blue, mostUsedGlobalColors);

            //TODO SMOOTH EDGES
        }

        System.out.println(mostUsedGlobalColors);
    }

    private void registerDrawnZones(int[][] red, int[][] green, int[][] blue, ArrayList<Zone> zones) {
        int tag = 1;
        int [] rgb = new int [3];

        for (int i = 0; i < red.length; i++) {
            for (int j = 0; j < red[0].length; j++) {

                rgb[0] = red[i][j];
                rgb[1] = green[i][j];
                rgb[2] = blue[i][j];

                if (!pixelIsBlank(rgb) && notVisited(i, j, drawn)) {
                    Zone zone = new Zone();
                    zone.setInitCoord(new int[]{i, j});
                    zone.setTag(tag);

                    zone.setRed(rgb[0]);
                    zone.setGreen(rgb[1]);
                    zone.setBlue(rgb[2]);

                    zone.setTypeByRGB(rgb);
                    zones.add(zone);

                    getZoneDimensions(i, j, red, green, blue, rgb, tag, zone);
                    tag++;
                }
            }
        }
    }

    private void getZoneDimensions(int i, int j, int[][] red, int[][] green, int[][] blue, int [] rgb, int tag, Zone zone) {

        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{i, j});

        while (!stack.isEmpty()) {
            int[] current = stack.pop();
            int y = current[0];
            int x = current[1];

            if (y < 0 || y >= red.length || x < 0 || x >= red[0].length || drawn[y][x] == tag) {
                continue;
            }

            if (!equalColors(red[y][x], rgb[0], green[y][x], rgb[1], blue[y][x], rgb[2])) {
                continue;
            }

            drawn[y][x] = tag;
            zone.setSize(zone.getSize()+1);

            zone.setBegY(Math.min(zone.getBegY(), y));
            zone.setEndY(Math.max(zone.getEndY(), y));
            zone.setBegX(Math.min(zone.getBegX(), x));
            zone.setEndX(Math.max(zone.getEndX(), x));

            stack.push(new int[]{y + 1, x});
            stack.push(new int[]{y - 1, x});
            stack.push(new int[]{y, x + 1});
            stack.push(new int[]{y, x - 1});
        }
    }

    private boolean spreadDrawnZones(int[][] red, int[][] green, int[][] blue, ArrayList<Zone> zones) {
        int maxSpread = getZoneSpread();

        boolean thereIsWhite = false;

        //TODO SPREAD THE ZONES
        for (int i = 0; i < red.length - 1; i++) {
            for (int j = 0; j < red[0].length - 1; j++) {
                if (!notVisited(i, j, drawn) && notVisited(i,j,spreaded)) {

                    int[] rgb = new int[3];
                    rgb[0] = red[i][j];
                    rgb[1] = green[i][j];
                    rgb[2] = blue [i][j];

                    if (pixelIsBlank(rgb)){ thereIsWhite = true;}

                    Zone zone = getZoneByTag(drawn[i][j], zones);

                    String type = zone.getType();

                    switch (type) {
                        case "Mountain" -> spreadMountain(i, j, red, green, blue, zone, maxSpread,0.9*getMutationChance());
                        case "Grassland/Forest" -> spreadForest(i, j, red, green, blue, zone, maxSpread, 0.9*getMutationChance());
                        case "Desert/Sand" -> spreadDesert(i, j, red, green, blue, zone, maxSpread, 0.9*getMutationChance());
                        case "Water" -> spreadWater(i, j, red, green, blue, zone, maxSpread,0.9+(getMutationChance())*0.09*0.7);
                        case "Roads" -> spreadRoads(i, j, red, green, blue, zone, maxSpread, 0.9*getMutationChance());
                        case "Construction" -> spreadConstruction(red, green, blue, zone, maxSpread);
                        default -> System.out.println("Default case");
                    }
                }
            }
        }



        /*
        for (int i = 0; i < zones.size(); i++) {
            Zone zone = new Zone();
            zone = zones.get(i);

            String type = zone.getType();

            System.out.println("Tipo de zona "+zone.getType());

            switch (type) {
                case "Mountain" -> spreadMountain(zone.getInitCoord()[0], zone.getInitCoord()[1], red, green, blue, zone, maxSpread);
                case "Grassland/Forest" -> spreadForest(zone.getInitCoord()[0], zone.getInitCoord()[1], red, green, blue, zone, maxSpread);
                case "Desert/Sand" -> spreadDesert(zone.getInitCoord()[0], zone.getInitCoord()[1],red, green, blue, zone, maxSpread);
                case "Water" -> spreadWater(zone.getInitCoord()[0], zone.getInitCoord()[1], red, green, blue, zone, maxSpread);
                case "Roads" -> spreadRoads(zone.getInitCoord()[0], zone.getInitCoord()[1], red, green, blue, zone, maxSpread);
                case "Construction" -> spreadConstruction(red, green, blue, zone, maxSpread);
                default -> System.out.println("Default case");
            }
        }
         */

        return thereIsWhite;
    }

    private void spreadMountain(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        generalSpread(i,j,red,green,blue, zone, maxSpread, startingMutation);
    }

    private void spreadForest(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        generalSpread(i,j,red,green,blue, zone, maxSpread, startingMutation);
    }

    private void spreadDesert(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        generalSpread(i,j,red,green,blue, zone, maxSpread, startingMutation);
    }

    private void spreadWater(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        //TODO
        if (maxSpread<=0) return;

        if (spreaded[i][j] != 0) return; //Blocking going through SPREADED other zones

        //TODO What is the behavior of water when encounters another object? It can either be blocked, or by time goes on pierce through it
        if (drawn[i][j] != zone.getTag() && drawn[i][j] != 0){ //Blocking going through DRAWN other zones
            return;
        }

        double mutation = startingMutation;
        double rand = Math.random();

        red[i][j] = zone.getRed();
        green[i][j] = zone.getGreen();
        blue[i][j] = zone.getBlue();
        spreaded[i][j] = zone.getTag();

        if (rand > mutation) {
            maxSpread--;
            mutation-=(mutation/16);
        }

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x <= j+1; x++) {
                if (y >= 0 && y < red.length && x >= 0 && x < red[0].length) {
                    spreadWater(y, x, red, green, blue, zone, maxSpread, mutation);
                }
            }
        }
    }

    private void spreadConstruction(int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread) {
        //TODO add a chance for other construction shapes

        for (int i = zone.getBegY(); i < zone.getEndY(); i++) {
            for (int j = zone.getBegX(); j < zone.getEndX(); j++) {
                red[i][j] = zone.getRed();
                green[i][j] = zone.getGreen();
                blue[i][j] = zone.getBlue();
            }
        }


    }

    private void spreadRoads(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        //TODO implement on the Zone class atributes to save the biggest and smallest continuous coloured vectors in x a y axis to get the lenght of the
        generalSpread(i,j,red,green,blue, zone, maxSpread, startingMutation/2);
        /*
        for (int i = zone.getBegY(); i < zone.getEndY(); i++) {

            for (int j = zone.getBegX(); j < zone.getEndX(); j++) {

                red[i][j] = zone.getRed();
                green[i][j] = zone.getGreen();
                blue[i][j] = zone.getBlue();
            }
        }
         */

    }

    private void generalSpread(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double startingMutation) {
        if (maxSpread<=0) return;

        if (spreaded[i][j] != 0) return;    //Blocking going through other SPREAD zones

        if (drawn[i][j] != zone.getTag() && drawn[i][j] != 0){ //Blocking going through other DRAWN zones
            return;
        }

        double mutation = startingMutation;
        double rand = Math.random();

        red[i][j] = zone.getRed();
        green[i][j] = zone.getGreen();
        blue[i][j] = zone.getBlue();
        spreaded[i][j] = zone.getTag();

        if (rand > mutation) {
            maxSpread--;
            mutation-=(mutation/16);
        }

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x <= j+1; x++) {
                if (y >= 0 && y < red.length && x >= 0 && x < red[0].length) {
                    generalSpread(y, x, red, green, blue, zone, maxSpread, mutation);
                }
            }
        }
    }


    private void spreadZone(int i, int j, int[][] red, int[][] green, int[][] blue, int maxSpread, int redPx, int greenPx, int bluePx, int mark) {
        maxSpread-=1;
        //visited[i][j] = mark;

        //System.out.println("Red "+red[i][j]+" Green "+green[i][j]+" Blue "+blue[i][j]+" ########### red " +redPx+ " green "+greenPx+"  blue "+ bluePx);
        red[i][j] = redPx;
        green[i][j] = greenPx;
        blue[i][j] = bluePx;

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x < j+1; x++) {
                if (x>=0 && x<red[0].length && y>=0 && y<red.length ){
                    if (!(y==i && x==j)) {
                        if (notVisited(y, x, drawn)) {
                            if (notVisited(x,y, drawn) && maxSpread>0) {
                                spreadZone(i, j, red, green, blue, maxSpread, redPx, greenPx, bluePx, mark);
                            }
                        }
                    }
                }
            }
        }
    }

    private void finalSpreading(int[][] red, int[][] green, int[][] blue, ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors) {
        for (int i = 0; i < red.length - 1; i++) {
            for (int j = 0; j < red[0].length - 1; j++) {

                int [] rgb = new int [3];
                rgb = getValuesRGB(i, j, red,green,blue);

                if (pixelIsBlank(rgb)){
                    //If blank it MUST change its color, so mutationChance chance set to 1
                    changePixel(red, green, blue, i, j, mostUsedGlobalColors, getSurroundingWeight(), 1.0);
                }else {
                    changePixel(red, green, blue, i, j, mostUsedGlobalColors, getSurroundingWeight(), getMutationChance());
                }
            }
        }
    }

    private void randomFill(int[][] red, int[][] green, int[][] blue, ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors) {

        // Vector wich will be used to random select pixels on the scrren to be filled
        int pixelCount = (red.length * red[0].length);
        int[] randomPick = new int[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            randomPick[i] = i;
        }
        shuffleVector(randomPick);

        for (int i = 0; i < pixelCount; i++) {
            int x = randomPick[i] % red.length;
            int y = randomPick[i] / red.length;

            int [] rgb = new int [3];
            rgb = getValuesRGB(x, y, red,green,blue);

            if (pixelIsBlank(rgb)) {
                //If blank it MUST change its color, so mutationChance chance set to 1
                changePixel(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), 1.0);
            } /* else {
                changePixel(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), getMutationChance());
            }*/

        }
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

    //Cleaning the zones from small noisy areas after having them painted
    private void eraseSmallZones (int [][] red, int [][] green, int [][] blue){

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

            drawn = new int[matR.length][matR[0].length];
            spreaded = new int[matR.length][matR[0].length];

            //Initializing the visited vector with 0's
            for (int i = 0; i < matR.length; i++) {
                for (int j = 0; j < matR[0].length; j++) {
                    drawn[i][j] =0;
                }
            }

            fillUpZones(zoneR, zoneG, zoneB);
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

    private void copyZoneToMat() {
        for (int i = 0; i < matR.length; i++) {
            for (int j = 0; j < matR[0].length; j++) {
                matR[i][j] = zoneR[i][j];
                matG[i][j] = zoneG[i][j] ;
                matB[i][j] = zoneB[i][j];
            }
        }
    }



    private void shuffleVector(int[] ar){
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

    /*
    private boolean allFilledAround(int i, int j, int[][] red,int[][] green, int[][] blue) {
        for (int y = i-1; y < y+1; y++) {
            for (int x = j-1; x < j+1; x++) {
                if (y<0 || y> red.length )
            }
        }
    }
     */

    private boolean pixelIsBlank(int[] rgb) {
        return rgb[0] == 255 && rgb[1] == 255 && rgb[2] == 255;
    }

    private boolean equalColors(int redPX, int redPx, int greenPX, int greenPx, int bluePX, int bluePx) {
        return redPX == redPx && greenPx == greenPX && bluePx == bluePX;
    }

    private boolean notVisited(int i, int j, int[][] visited) {
        return visited[i][j] == 0;
    }

    private int[] getValuesRGB(int i, int j, int[][] red, int[][] green, int[][] blue) {
        int[] rgb = new int[3];

        rgb[0] = red[i][j];
        rgb[1] = green[i][j];
        rgb[2] = blue[i][j];

        return rgb;
    }

    public static Zone getZoneByTag(int tag, ArrayList<Zone> zones) {
        for (Zone zone : zones) {
            if (tag == zone.getTag()) {
                return zone;
            }
        }
        System.out.println("No such zone found, zone ID:"+ tag);
        return null;
    }

}