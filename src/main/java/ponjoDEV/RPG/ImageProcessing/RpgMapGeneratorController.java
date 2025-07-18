package ponjoDEV.RPG.ImageProcessing;

import ponjoDEV.RPG.Model.Prop;
import ponjoDEV.RPG.Model.Zone;
import ponjoDEV.RPG.View.RpgMapGeneratorView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RpgMapGeneratorController {
    private RpgMapGeneratorView view;

    private int[][] matR, matG, matB, matRCopy, matGCopy, matBCopy, drawn, spreaded, texR, texG, texB;
    private String path = "C:\\Program Files\\RPGMapGenerator";
    private  ArrayList<Zone> zones = new ArrayList<>();

    //deviation means how often the analysed pixel will differ from its surroundings
    //canvasMutation means the current pixel chance to change from its original value
    double surroundingWeight, mutationChance, propDensity;

    int zoneSpread;

    public RpgMapGeneratorController(RpgMapGeneratorView rpgMapGeneratorView) {
    }

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

    public int[][] getTexB() { return texB; }

    public void setTexB(int[][] texB) { this.texB = texB; }

    public int[][] getTexG() { return texG; }

    public void setTexG(int[][] texG) { this.texG = texG; }

    public int[][] getTexR() { return texR; }

    public void setTexR(int[][] texR) { this.texR = texR; }

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


    public ArrayList<Zone> generateZones(BufferedImage canvas) {
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
            zones.clear();
            zones = fillUpZones(matRCopy, matGCopy, matBCopy);

            return zones;
        }
        return null;
    }

    public ArrayList<Zone> fillUpZones(int [][] red, int [][] green, int [][] blue) {
        //Saving the most used colors from the canvas
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

            //Ordering each zone by priority, consttruction is above all for it to create full rooms instead of another zone invading it
            zones.sort(Comparator.comparingInt(Zone::getPriority));
            spreadDrawnZones(matRCopy, matGCopy, matBCopy, zones, getZoneSpread(), getMutationChance());

            /*for (Zone zone : zones) {
                System.out.println("Zone " + zone.getTag() + "\nZona minima Y: " + zone.getBegY() + " Zona minima X: " + zone.getBegX() + "\nZona maxima Y: " + zone.getEndY() + " Zona maxima X: " + zone.getEndX() + "\nTipo " + zone.getType());
                System.out.println("Y inicial: " + zone.getInitCoord()[0] + " X inicial " + zone.getInitCoord()[1]+"\n Zone Size :"+zone.getSize());
            }
             */

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

        registerDrawnZones(red, green, blue, zones);

        return zones;

    }

    private void registerDrawnZones(int[][] red, int[][] green, int[][] blue, ArrayList<Zone> zones) {
        int tag = 1;
        int [] rgb = new int [3];

        int[][] temp = new int[red.length][red[0].length];

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
                    zone.setPropHeightMap(temp);
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

            zone.setMinY(Math.min(zone.getMinY(), y));
            zone.setMaxY(Math.max(zone.getMaxY(), y));
            zone.setMinX(Math.min(zone.getMinX(), x));
            zone.setMaxX(Math.max(zone.getMaxX(), x));

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
                        case "Mountain":
                            spreadMountain(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                            break;
                        case "Grassland":
                            spreadForest(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                            break;
                        case "Desert":
                            spreadDesert(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                            break;
                        case "Water":
                            spreadWater(i, j, red, green, blue, zone, spreadLimit,(double) startingSpread*0.09*0.7, mutationChance);
                            break;
                        case "Roads":
                            spreadRoads(i, j, red, green, blue, zone, spreadLimit, (double) startingSpread, mutationChance);
                            break;
                        case "Construction":
                            spreadConstruction(red, green, blue, zone, spreadLimit, mutationChance);
                            break;
                        default:
                            System.out.println("Invalid Zone Type");
                            break;
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
        for (int i = zone.getMinY(); i < zone.getMaxY(); i++) {
            for (int j = zone.getMinX(); j < zone.getMaxX(); j++) {
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
            int x = randomPick[i] % red.length;
            int y = randomPick[i] / red.length;

            int [] rgb = new int [3];
            rgb = getValuesRGB(x, y, red,green,blue);

            if (pixelIsBlank(rgb)) {//If blank it MUST change its color, so mutationChance chance set to 1
                singlePixelChange(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), 1.0);
            }//else { singlePixelChange(red, green, blue, x, y, mostUsedGlobalColors, getSurroundingWeight(), getMutationChance()); }

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



    public Vector<int[][]> texturizeMap(ArrayList<Zone>zones, String texturePack, double mutationChance, double propDensity) {
        //Linking each texture to a zone so it can get the files by zoneType and then copy the texture into the zone
        int count =0;

        int[][] texR = new int[matR.length][matR[0].length];
        int [][] texG = new int[matR.length][matR[0].length];
        int [][] texB = new int[matR.length][matR[0].length];

        //TEXTURE
        for (Zone zone : zones){

            count++;
            System.out.println(count+"°");
            System.out.println(zone.getType());

            String subPath = path+"\\RPGMapTextures\\"+texturePack+"\\Textures\\"+zone.getType();
            int file = fileChooser(subPath, mutationChance);

            textureFill(zone, subPath, mutationChance, file, texR, texG, texB);
            zone.setPopulated(false);
            resetMatrix(zone.getPropHeightMap());

        }

        Vector<int[][]> rgb = new Vector<>();
        rgb.add(texR);
        rgb.add(texG);
        rgb.add(texB);


        return rgb;
    }


    public void textureFill(Zone zone, String subPath, double mutationChance, int fileNumber, int[][] texR, int[][] texG, int[][] texB) {
        try {
            // Convert the path to a Path object
            Path directory = Paths.get(subPath);

            // Check if the directory exists
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.out.println("Directory not found: " + subPath);
                return;
            }

            // List all files in the directory
            List<Path> files = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(Collectors.toList());

            // Check if we have enough files and the fileNumber is valid
            if (files.isEmpty()) {
                System.out.println("No files found in directory: " + subPath);
                return;
            }

            if (fileNumber < 0 || fileNumber >= files.size()) {
                System.out.println("Invalid file number: " + fileNumber + " (available files: " + files.size() + ")");
                return;
            }

            // Get the selected file and load as BufferedImage
            Path selectedFile = files.get(fileNumber);
            System.out.println("Selected texture file: " + selectedFile.getFileName().toString());
            BufferedImage img = ImageIO.read(selectedFile.toFile());

            if (img == null) {
                System.err.println("Failed to load image: " + selectedFile);
                return;
            }

            // Calculate zone dimensions
            int zoneWidth = zone.getMaxX() - zone.getMinX() + 1;
            int zoneHeight = zone.getMaxY() - zone.getMinY() + 1;

            // Get texture dimensions
            int textureWidth = img.getWidth();
            int textureHeight = img.getHeight();


            // Create temporary arrays to store texture portion (zone-sized)
            int[][] tempR = new int[zoneHeight][zoneWidth];
            int[][] tempG = new int[zoneHeight][zoneWidth];
            int[][] tempB = new int[zoneHeight][zoneWidth];

            if (zone.getType().equals("Construction")) {
                // For construction zones, resize texture to fit the zone
                for (int y = zone.getMinY(); y <= zone.getMaxY(); y++) {
                    for (int x = zone.getMinX(); x <= zone.getMaxX(); x++) {
                        // Check bounds
                        if (y >= 0 && y < drawn.length && x >= 0 && x < drawn[0].length) {
                            // Only apply texture where the zone exists
                            if (drawn[y][x] == zone.getTag() || spreaded[y][x] == zone.getTag()) {
                                // Calculate relative position within the zone (0 to zoneWidth/Height)
                                int relativeX = x - zone.getMinX();
                                int relativeY = y - zone.getMinY();

                                // Map zone coordinates to texture coordinates (scaling)
                                int textureX = (relativeX * textureWidth) / zoneWidth;
                                int textureY = (relativeY * textureHeight) / zoneHeight;

                                // Ensure we don't go out of texture bounds
                                textureX = Math.min(textureX, textureWidth - 1);
                                textureY = Math.min(textureY, textureHeight - 1);

                                // Get RGB values directly from the texture image
                                int rgb = img.getRGB(textureX, textureY);
                                int r = (rgb >> 16) & 0xFF;
                                int g = (rgb >> 8) & 0xFF;
                                int b = rgb & 0xFF;

                                // Apply mutation if specified
                                if (Math.random() < mutationChance) {
                                    r = Math.max(0, Math.min(255, r + (int)(Math.random() * 20 - 10)));
                                    g = Math.max(0, Math.min(255, g + (int)(Math.random() * 20 - 10)));
                                    b = Math.max(0, Math.min(255, b + (int)(Math.random() * 20 - 10)));
                                }

                                texR[y][x] = r;
                                texG[y][x] = g;
                                texB[y][x] = b;
                            }
                        }
                    }
                }
            }else {


                // Calculate safe starting bounds for random texture selection
                int maxStartX = Math.max(0, textureWidth - zoneWidth);
                int maxStartY = Math.max(0, textureHeight - zoneHeight);

                // Generate random starting point
                Random random = new Random();
                int startX = maxStartX > 0 ? random.nextInt(maxStartX + 1) : 0;
                int startY = maxStartY > 0 ? random.nextInt(maxStartY + 1) : 0;

                // Copy texture portion to temporary arrays
                for (int y = 0; y < zoneHeight; y++) {
                    for (int x = 0; x < zoneWidth; x++) {
                        // Calculate texture coordinates with wrapping if needed
                        int texX = (startX + x) % textureWidth;
                        int texY = (startY + y) % textureHeight;

                        // Get RGB values from texture
                        int rgb = img.getRGB(texX, texY);
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;

                        // Apply mutation chance for variation
                        if (random.nextDouble() < mutationChance) {
                            red = Math.max(0, Math.min(255, red + random.nextInt(21) - 10));
                            green = Math.max(0, Math.min(255, green + random.nextInt(21) - 10));
                            blue = Math.max(0, Math.min(255, blue + random.nextInt(21) - 10));
                        }

                        tempR[y][x] = red;
                        tempG[y][x] = green;
                        tempB[y][x] = blue;
                    }
                }

                // Apply texture to zone areas in the main matrices
                for (int y = zone.getMinY(); y <= zone.getMaxY(); y++) {
                    for (int x = zone.getMinX(); x <= zone.getMaxX(); x++) {
                        // Check bounds
                        if (y >= 0 && y < drawn.length && x >= 0 && x < drawn[0].length) {
                            // Only apply texture where the zone exists
                            if (drawn[y][x] == zone.getTag() || spreaded[y][x] == zone.getTag()) {
                                // Calculate relative position within the zone
                                int relativeY = y - zone.getMinY();
                                int relativeX = x - zone.getMinX();

                                // Apply texture from temporary arrays
                                if (relativeY < zoneHeight && relativeX < zoneWidth) {
                                    texR[y][x] = tempR[relativeY][relativeX];
                                    texG[y][x] = tempG[relativeY][relativeX];
                                    texB[y][x] = tempB[relativeY][relativeX];
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error accessing directory or loading image: " + e.getMessage());
        }
    }

    public Vector<int[][]> addProps(ArrayList<Zone>zones, int[][] texR, int[][] texG, int[][] texB, String texturePack, double surroundingWeight, double propDensity) {
        //Linking each texture to a zone so it can get the files by zoneType and then copy the texture into the zone
        int count =0;

        //TODO UNDERSTAND WHY I NEED THIS HERE TO WORK
        int[][] propR = new int[matR.length][matR[0].length];
        int [][] propG = new int[matR.length][matR[0].length];
        int [][] propB = new int[matR.length][matR[0].length];

        for (int i = 0; i < propR.length ; i++) {
            for (int j = 0; j < propR[0].length ; j++) {
                propR[i][j] = texR[i][j];
                propG[i][j] = texG[i][j];
                propB[i][j] = texB[i][j];
            }
        }

        //PROPS

        for (Zone zone: zones){

            PropController propController = new PropController(this);

            String subPath = path+"\\RPGMapTextures\\"+texturePack+"\\Props\\"+zone.getType();
            propFill(zone, subPath, propR, propG, propB, propDensity, propController, surroundingWeight);

            System.out.println(subPath);
        }
        //*/


        Vector<int[][]> rgb = new Vector<>();
        rgb.add(propR);
        rgb.add(propG);
        rgb.add(propB);


        return rgb;
    }

    private void propFill(Zone zone, String subPath, int[][] propR, int[][] propG, int[][] propB, double propDensity, PropController propController, double surroundingWeight) {
        try {// Convert the path to a Path object
            Path directory = Paths.get(subPath);

            // Check if the directory exists
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.out.println("Directory not found: " + subPath);
                return;
            }

            // List all files in the directory
            List<Path> files = Files.list(directory)
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(Collectors.toList());

            // Check if we have enough files and the fileNumber is valid
            if (files.isEmpty()) {
                System.out.println("No files found in directory: " + subPath);
                return;
            }

            List<Prop> props = new ArrayList<>();
            // Get the files from the folders and load as BufferedImage on a list
            for (Path selectedFile : files) {
                Prop prop = new Prop(selectedFile);
                prop.setResizedImage(propController.resizeImg(prop));
                props.add(prop);
            }

            for (int i = 0; i < propDensity; i++) {
                int selectedProp = (int) (Math.random() * props.size());
                int[] xy;

                do {
                    xy = pickPropLocation(zone);
                }while (zone.isPopulated() && Math.random() < surroundingWeight && zone.getPropHeightMap()[xy[0]][xy[1]] == 0);


                addPropToLocation(props.get(selectedProp), xy[1], xy[0], propR, propG, propB, zone);

            }

        }
        catch (IOException e){
            System.out.println("you stupdi ahh got error: "+e.getMessage());
        }
    }

    private int[] pickPropLocation(Zone zone) {
        int [] xy = new int[2];

        //Making sure the prop will hae the first pixel added to the zone correctly
        // TODO GET X AND Y AS THE CENTER OF THE PROP INSTEAD OF JUST THE FIRST PIXEL to be applied, withou getting out
        //  of bounds when effectively copying the prop image
        do {
            xy[0] = zone.getMinY() + (int) (Math.random() * (zone.getMaxY() - zone.getMinY() + 1));
            xy[1] = zone.getMinX() + (int) (Math.random() * (zone.getMaxX() - zone.getMinX() + 1));
            //y-= (int) props.get(selectedProp).getHeight()/2;
            //x-= (int) props.get(selectedProp).getWidth()/2;

        }//while (x<0 || y<0 || y>= texR.length|| x>=texR[0].length ||drawn[y][x]!=zone.getTag());
        while (drawn[xy[0]][xy[1]]!=zone.getTag());

        return xy;
    }

    private void addPropToLocation(Prop prop, int x, int y, int[][] texR, int[][] texG, int[][] texB, Zone zone) {
        Vector<int[][]> rgbMat = getMatrixRGB(prop.getResizedImage());

        int[][] rOrig = rgbMat.elementAt(0);
        int[][] gOrig = rgbMat.elementAt(1);
        int[][] bOrig = rgbMat.elementAt(2);

        // Calculate bounds to prevent going outside texture boundaries
        int maxY = Math.min(y + prop.getHeight(), texR.length);
        int maxX = Math.min(x + prop.getWidth(), texR[0].length);

        int height = 0; // Local prop coordinate

        for (int i = y; i < maxY; i++) {
            int width = 0; // Reset width for each row - Local prop coordinate

            for (int j = x; j < maxX; j++) {
                // Check bounds for prop arrays before accessing
                if (height < prop.getValidPixels().length && width < prop.getValidPixels()[0].length) {
                    if (prop.getValidPixels()[height][width] == 1) {
                        // Use local coordinates (height, width) for prop arrays
                        // Use global coordinates (i, j) for texture arrays
                        if (zone.getPropHeightMap()[i][j] < prop.getHeight()) {
                            texR[i][j] = rOrig[height][width];
                            texG[i][j] = gOrig[height][width];
                            texB[i][j] = bOrig[height][width];
                        }
                        zone.getPropHeightMap()[i][j] = prop.getHeight();
                    }
                }
                width++; // Increment local width coordinate
            }
            height++; // Increment local height coordinate
        }

        System.out.println(prop.getName()+" has this height: "+prop.getPropHeight());

        //TODO IS THIS CORRECT ? CHECK LATER
        zone.setPopulated(true);

        //TODO AFTER APPLYING THE COLORS OF THE PROP AND ITS HEIGHT (IF BIGGER THAN ALREADY EXISTS) NEEDS TO APPLY A "NEGATIVE WAKE" FROM THE PROP AREA THAT
        // SLOWLY TURNS TO ZERO, EXAMPLE, IF THE PROP IS 1 PIXEL AND ITS HEIGHT IS 5, ALL PIXELS AROUNRD IT WILL BE -4, AND AROUND THOSE WILL BE -3 ETC
        // UNTIL IT REACHES 0, THAT WAY 2 PROPS WITH DIFFERENT HEIGHT MAY CAUSE THE IMPRESSION OF DEPTH ON THE IMAGE


        //TODO USE THE zone.getPropHeightMap() attribute to "paint" a height map in and around ex: 1px image, that px will carry the
        // prop.getPropHeight() attribute, and around it will have its (value-1)*-1 and increasing till gets 0
        // ex: prop height =5, then this pixel carries 5. and the first row around it carry -4, then -3, then -2, -1,

        propagateHeightWake(y, x, zone.getPropHeightMap());

    }

    private void propagateHeightWake(int y, int x, int[][] propHeightMap) {
        int propHeight = propHeightMap[y][x];

        if (propHeight <= 0) {
            return;
        }

        // Process from innermost to outermost layer
        for (int layer = 1; layer < propHeight; layer++) {
            int wakeHeight = layer - propHeight; // Creates: -4, -3, -2, -1

            for (int dy = -layer; dy <= layer; dy++) {
                for (int dx = -layer; dx <= layer; dx++) {
                    int newY = y + dy;
                    int newX = x + dx;

                    if (newY >= 0 && newY < propHeightMap.length &&
                            newX >= 0 && newX < propHeightMap[0].length) {

                        // Only set wake if current value is 0 (empty space)
                        if (propHeightMap[newY][newX] == 0) {
                            propHeightMap[newY][newX] = wakeHeight;
                        }
                    }
                }
            }
        }
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

    public static Zone getZoneByTag(int tag, ArrayList<Zone> zones) {
        for (Zone zone : zones) {
            if (tag == zone.getTag()) {
                return zone;
            }
        }
        System.out.println("No such zone found, zone ID:"+ tag);
        return null;
    }

    public int fileChooser(String folderPath, double mutationChance) {
        try {
            Path folder = Paths.get(folderPath);
            if (!Files.exists(folder) || !Files.isDirectory(folder)) {
                return -1; // Invalid folder
            }

            // Get all image files and sort them for consistent numbering
            List<Path> imageFiles = Files.list(folder)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".png") || fileName.endsWith(".jpg") ||
                                fileName.endsWith(".jpeg") || fileName.endsWith(".bmp");
                    })
                    .sorted()
                    .collect(Collectors.toList());

            if (imageFiles.isEmpty()) {
                return -1; // No image files found
            }

            // Choose a random file index
            Random random = new Random();
            int selectedIndex = random.nextInt(imageFiles.size());

            // Apply mutation chance - if mutation triggers, select a different random file
            if (random.nextInt(100) < mutationChance && imageFiles.size() > 1) {
                int mutatedIndex;
                do {
                    mutatedIndex = random.nextInt(imageFiles.size());
                } while (mutatedIndex == selectedIndex);
                selectedIndex = mutatedIndex;
            }

            return selectedIndex;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
