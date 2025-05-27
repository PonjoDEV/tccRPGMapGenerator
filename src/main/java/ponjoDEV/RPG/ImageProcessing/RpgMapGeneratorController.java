package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.Model.Zone;
import ponjoDEV.RPG.View.RpgMapGeneratorView;

import java.awt.image.BufferedImage;
import java.util.*;

public class RpgMapGeneratorController {
    private RpgMapGeneratorView view;
    public RpgMapGeneratorController(RpgMapGeneratorView view){ this.view = view; }

    private int[][] matR, matG, matB, matRCopy, matGCopy, matBCopy, drawn, spreaded;

    //deviation means how often the analysed pixel will differ from its surroundings
    //canvasMutation means the current pixel chance to change from its original value
    double surroundingWeight, mutationChance, propDensity;

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

    public int[][] getMatRCopy() {
        return matRCopy;
    }

    public void setMatRCopy(int[][] matRCopy) { this.matRCopy = matRCopy; }

    public int[][] getMatGCopy() { return matGCopy; }

    public void setMatGCopy(int[][] matGCopy) {
        this.matGCopy = matGCopy;
    }

    public int[][] getMatBCopy() {
        return matBCopy;
    }

    public void setMatBCopy(int[][] matBCopy) {
        this.matBCopy = matBCopy;
    }

    public double getSurroundingWeight() {
        return surroundingWeight;
    }

    public void setSurroundingWeight(double surroundingWeight) {
        this.surroundingWeight = surroundingWeight;
    }

    public double getPropDensity() { return propDensity; }

    public void setPropDensity(double propDensity) { this.propDensity = propDensity; }

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

        //Creating an array of Zones to save the drawn zones information
        ArrayList<Zone> zones = new ArrayList<>();
        registerDrawnZones(red, green, blue, zones);

        randomFill(red, green, blue, zones, mostUsedGlobalColors);

        int iteration = 0;

        //while(iteration <= this.getZoneSpread() || whitePxLeft(red,green,blue)) {
        while(iteration <= this.getZoneSpread()*5) {
            // First, copy the current state of the original matrices to the working copies
            originalMatToCopy();

            zones.sort(Comparator.comparingInt(Zone::getPriority));
            spreadDrawnZones(matRCopy, matGCopy, matBCopy, zones, getZoneSpread(), getMutationChance());

            for (Zone zone : zones) {
                System.out.println("Zone " + zone.getTag() + "\nZona minima Y: " + zone.getBegY() + " Zona minima X: " + zone.getBegX() + "\nZona maxima Y: " + zone.getEndY() + " Zona maxima X: " + zone.getEndX() + "\nTipo " + zone.getType());
                System.out.println("Y inicial: " + zone.getInitCoord()[0] + " X inicial " + zone.getInitCoord()[1]+"\n Zone Size :"+zone.getSize());
            }

            // After spreading, copy the results from the working copies back to the original matrices
            // This ensures that each iteration builds upon the previous one
            copyMattoOriginalMat();

            // Reset the tracking matrices for the next iteration
            resetMatrix(drawn);
            resetMatrix(spreaded);

            // Clear and re-register zones based on the updated matrices
            zones.clear();
            registerDrawnZones(red, green, blue, zones);

            System.out.println(iteration+"° iteração");
            iteration++;

            if (!whitePxLeft(red,green,blue)){
                break;
            }
        }

        System.out.println(mostUsedGlobalColors);
    }

    private boolean whitePxLeft(int[][] red, int[][] green, int[][] blue) {

        for (int i = 0; i < red.length; i++) {
            for (int j = 0; j < red[0].length; j++) {
                int [] rgb = new int[3];
                rgb[0]=red[i][j];
                rgb[1]=green[i][j];
                rgb[2]=blue[i][j];
                if (pixelIsBlank(rgb)){
                    return true;
                }
            }
        }
        return false;
    }

    private void resetMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    private void replicateInto(int[][] origin, int[][] destination) {
        if (origin.length != destination.length || origin[0].length != destination[0].length )
            for (int i = 0; i < origin.length; i++) {
                for (int j = 0; j < origin[0].length; j++) {
                    destination[i][j] = origin[i][j];
                }
            }
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

    private void spreadDrawnZones(int[][] red, int[][] green, int[][] blue, ArrayList<Zone> zones, double startingSpread, double mutationChance) {
        int spreadLimit = (int) startingSpread;
        startingSpread*=0.09;

        for (int i = 0; i < red.length - 1; i++) {
            for (int j = 0; j < red[0].length - 1; j++) {
                if (!notVisited(i, j, drawn) && notVisited(i,j,spreaded)) {
                    Zone zone = getZoneByTag(drawn[i][j], zones);

                    // Fix for the null return issue
                    if (zone == null) {
                        continue; // Skip this iteration if zone is null
                    }

                    String type = zone.getType();

                    switch (type) {
                        case "Mountain" -> spreadMountain(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                        case "Grassland/Forest" -> spreadForest(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                        case "Desert/Sand" -> spreadDesert(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                        case "Water" -> spreadWater(i, j, red, green, blue, zone, spreadLimit,(double) startingSpread*0.09*0.7, mutationChance);
                        case "Roads" -> spreadRoads(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                        case "Construction" -> spreadConstruction(red, green, blue, zone, spreadLimit, mutationChance);
                        default -> System.out.println("Invalid Zone Type");
                    }
                }
            }
        }
    }


    //TODO FOR EVERY SPREAD METHOD THE OVERALL EXPANSION CHANCE RESULTS FROM THE INITIAL EXPANSION SHALL GROW WEAKER, EVEN IF FOR MUTATION PURPOSES IT SOMETIMES REGROWS,
    // MATHEMATICALLY IT SHOULD HAVE A TENDENCY TO SHRINK UNTIL ITS OVER, SO THE FORMULAS SHOULD RESULT IN 0, THAT BEING SAID:
    // if (rand > spread) { SPREAD DECREASE RATE SHOULD BE BIGGER THAN ANY GROwTH MUTATIONS INSIDE OR OUTSIDE OF THE CONDITION

    private void spreadMountain(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int spreadLimit, double spreadRate, double mutationChance) {
        generalSpread(i,j,red,green,blue, zone, spreadLimit, spreadRate, mutationChance, 16);
    }

    private void spreadForest(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int spreadLimit, double spreadRate, double mutationChance) {
        generalSpread(i,j,red,green,blue, zone, spreadLimit, spreadRate, mutationChance, 16);
    }

    private void spreadDesert(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int spreadLimit, double spreadRate, double mutationChance) {
        generalSpread(i,j,red,green,blue, zone, spreadLimit, spreadRate, mutationChance, 16);
    }

    private void spreadWater(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int spreadLimit, double spreadRate, double mutationChance) {
        //spreadLimit controls how many iterations, while spreadRate controls the chance to reduce the spread rate
        if (spreadLimit<=0) return;

        if (spreaded[i][j] != 0) return; //Blocking going through SPREADED other zones

        //TODO What is the behavior of water when encounters another object? It can either be blocked, or by time goes on and pierce through it,
        // If its not a construction it should be able to continue going through
        if (drawn[i][j] != zone.getTag() && drawn[i][j] != 0){
            //Blocking going through other road or construction zones
            //if (it encounters a road or construction){ waterSpread stops}
            return;
        }

        double spread = spreadRate;
        double rand = Math.random();

        red[i][j] = zone.getRed();
        green[i][j] = zone.getGreen();
        blue[i][j] = zone.getBlue();
        spreaded[i][j] = zone.getTag();

        if (rand > spread) {
            //TODO tweak with this spread condition for a bit
            //Water mutation should start at least 80, so if mutation is lower than 0.8, it must be set to it
            if ((Math.random() < 0.8+ (mutationChance*0.2)-0.61 ) && spreadLimit == 1) spreadLimit++;

            spreadLimit--;
            spread-=(spread/16);
        }

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x <= j+1; x++) {
                if (y >= 0 && y < red.length && x >= 0 && x < red[0].length) {
                    spreadWater(y, x, red, green, blue, zone, spreadLimit, spread, mutationChance);
                }
            }
        }
    }

    private void spreadConstruction(int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double mutationChance) {
        //TODO add a chance for other construction shapes

        //Square shaped constructions
        for (int i = zone.getBegY(); i < zone.getEndY(); i++) {
            for (int j = zone.getBegX(); j < zone.getEndX(); j++) {
                red[i][j] = zone.getRed();
                green[i][j] = zone.getGreen();
                blue[i][j] = zone.getBlue();
            }
        }
    }

    private void spreadRoads(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int spreadLimit, double startingSpread, double mutationChance) {

        if (spreadLimit<=0) return;

        if (spreaded[i][j] != 0) return;    //Blocking going through other SPREAD zones

        if (drawn[i][j] != zone.getTag() && drawn[i][j] != 0){ //Blocking going through other DRAWN zones
            return;
        }

        double spread = startingSpread;
        double rand = Math.random();

        red[i][j] = zone.getRed();
        green[i][j] = zone.getGreen();
        blue[i][j] = zone.getBlue();
        spreaded[i][j] = zone.getTag();

        //Way to reduce gradually the spreading limit
        if (rand > spread) {

            spreadLimit--;
            spread-=(spread/4);
        }

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x <= j+1; x++) {
                if (y >= 0 && y < red.length && x >= 0 && x < red[0].length) {
                    spreadRoads(y, x, red, green, blue, zone, spreadLimit, spread, mutationChance);
                }
            }
        }
    }

    private void generalSpread(int i, int j, int[][] red, int[][] green, int[][] blue, Zone zone, int maxSpread, double spreadRate, double mutationChance, int reductionFactor) {
        //TODO SPREAD MORE FOR EACH TIME IT HAPPENS
        if (maxSpread<=0) return;

        if (spreaded[i][j] != 0) return;    //Blocking going through other SPREAD zones

        if (drawn[i][j] != zone.getTag() && drawn[i][j] != 0){ //Blocking going through other DRAWN zones
            return;
        }

        double spread = spreadRate;
        double rand = Math.random();

        red[i][j] = zone.getRed();
        green[i][j] = zone.getGreen();
        blue[i][j] = zone.getBlue();
        spreaded[i][j] = zone.getTag();

        //Way to reduce gradually the spreading
        if (rand > spread) {
            //IN HERE THERE MUST BE A TWEAK TO THE MaxSpread

            maxSpread--;
            spread-=(spread/reductionFactor);
        }

        for (int y = i-1; y <= i+1; y++) {
            for (int x = j-1; x <= j+1; x++) {
                if (y >= 0 && y < red.length && x >= 0 && x < red[0].length) {
                    generalSpread(y, x, red, green, blue, zone, maxSpread, spread, mutationChance, 2);
                }
            }
        }
    }

    private void randomFill(int[][] red, int[][] green, int[][] blue, ArrayList<Zone> zones, ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors) {
        // Vector which will be used to random select pixels on the scrren to be filled
        int pixelCount = (red.length * red[0].length);

        int[] randomPick = new int[pixelCount];
        for (int i = 0; i < pixelCount; i++) {
            randomPick[i] = i;
        }
        shuffleVector(randomPick);

        int randomDotAmount = (int) (10*getMutationChance());
        for (int i = 0; i < randomDotAmount; i++) {
            // THIS ONE IS OK System.out.println("X equivale a:"+x+"\nY equivale a:"+y);
            int x = randomPick[i] % red.length;
            int y = randomPick[i] / red.length;

            int [] rgb = new int [3];
            rgb = getValuesRGB(x, y, red,green,blue);

            if (pixelIsBlank(rgb)) {
                //If blank it MUST change its color, so mutationChance chance set to 1
                singlePixelChange(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), 1.0);
            } else {
                //singlePixelChange(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), getMutationChance());
            }

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

    private void singlePixelChange(int[][] red, int[][] green, int[][] blue, int i, int j, ArrayList<Map.Entry<String, Integer>> mostUsedGlobalColors, double surrondWeight, double mutationChance) {
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

        Zone zone = new Zone();
        zone.setTypeByRGB(rgb);
        spreaded[i][j] = zone.getTag();
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

            setMatRCopy(rgbMat.elementAt(0));
            setMatGCopy(rgbMat.elementAt(1));
            setMatBCopy(rgbMat.elementAt(2));

            drawn = new int[matR.length][matR[0].length];
            spreaded = new int[matR.length][matR[0].length];

            //Initializing the visited vector with 0's
            for (int i = 0; i < matR.length; i++) {
                for (int j = 0; j < matR[0].length; j++) {
                    drawn[i][j] =0;
                    spreaded[i][j] =0;
                }
            }

            fillUpZones(matR, matG, matB);
        }
    }

    private void originalMatToCopy(){
        for (int i = 0; i < matR.length; i++) {
            for (int j = 0; j < matR[0].length; j++) {
                matRCopy[i][j] = matR[i][j];
                matGCopy[i][j] = matG[i][j];
                matBCopy[i][j] = matB[i][j];
            }
        }
    }

    private void copyMattoOriginalMat() {
        for (int i = 0; i < matR.length; i++) {
            for (int j = 0; j < matR[0].length; j++) {
                matR[i][j] = matRCopy[i][j];
                matG[i][j] = matGCopy[i][j] ;
                matB[i][j] = matBCopy[i][j];
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

    private boolean pixelIsBlank(int[] rgb) {
        return rgb[0] == 255 && rgb[1] == 255 && rgb[2] == 255;
    }

    private boolean equalColors(int redPX, int redPx, int greenPX, int greenPx, int bluePX, int bluePx) {
        return redPX == redPx && greenPx == greenPX && bluePx == bluePX;
    }

    //Method ok
    private boolean notVisited(int i, int j, int[][] sampleMatrix) {
        return sampleMatrix[i][j] == 0;
    }

    private int[] getValuesRGB(int i, int j, int[][] red, int[][] green, int[][] blue) {
        int[] rgb = new int[3];

        rgb[0] = red[i][j];
        rgb[1] = green[i][j];
        rgb[2] = blue[i][j];

        return rgb;
    }

    private boolean finished (int[][] red, int[][] green, int[][] blue){
        for (int i = 0; i < red.length; i++) {
            for (int j = 0; j < red[0].length; j++) {
                int[] rgb = new int[3];

                rgb[0] = red[i][j];
                rgb[1] = green[i][j];
                rgb[2] = blue[i][j];

                if (pixelIsBlank(rgb)) return false;
            }
        }
        return true;
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
