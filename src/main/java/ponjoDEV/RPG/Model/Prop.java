package ponjoDEV.RPG.Model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents a prop with parsed filename information and size data.
 * Filename format: name_sizexsize.format (e.g., rock_64x64.PNG)
 */
public class Prop {
    private String name;
    private int width;
    private int height;
    private String format;
    private BufferedImage originalImage;
    private BufferedImage resizedImage;
    private int propHeight;
    private int[][] validPixels;

    public Prop(Path file) throws IOException {
        BufferedImage img = ImageIO.read(file.toFile());
        if (img == null) {
            throw new IOException("Failed to read image from file: " + file.toString() + ". The file may not be a valid image format.");
        }
        this.setOriginalImage(img);

        this.setName(file.getFileName().toString());
        parseFilename(this.getName());

        int[][] validation = new int[height][width];

        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                validation[i][j] =1;
            }
        }

        this.validPixels = validation;

    }

    public int getPropHeight() { return propHeight; }
    public void setPropHeight(int propHeight) { this.propHeight = propHeight; }

    public int[][] getValidPixels() { return validPixels; }
    public void setValidPixels(int[][] validPixels) { this.validPixels = validPixels; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public BufferedImage getOriginalImage() { return originalImage; }
    public void setOriginalImage(BufferedImage originalImage) { this.originalImage = originalImage; }

    public BufferedImage getResizedImage() { return resizedImage; }
    public void setResizedImage(BufferedImage resizedImage) { this.resizedImage = resizedImage; }


    private void parseFilename(String filename) {
        try {
            // Remove file extension
            String nameWithoutExtension = filename;
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                this.format = filename.substring(lastDotIndex + 1);
                nameWithoutExtension = filename.substring(0, lastDotIndex);
            }

            // Split by underscore
            String[] parts = nameWithoutExtension.split("_");

            if (parts.length >= 3) {
                // Get the prop Height
                this.setPropHeight(Integer.parseInt(parts[1]));

                // Look for the size part (contains 'x')
                String sizeStr = null;
                int sizeIndex = -1;

                // Check from the end backwards to find the size part
                for (int i = parts.length - 1; i >= 2; i--) {
                    if (parts[i].contains("x")) {
                        sizeStr = parts[i];
                        sizeIndex = i;
                        break;
                    }
                }

                if (sizeStr != null) {
                    // Build name from parts before the size
                    StringBuilder nameBuilder = new StringBuilder();

                    nameBuilder.append(parts[0]);

                    this.name = nameBuilder.toString();

                    // Parse size (e.g., "128x128")
                    String[] sizeParts = sizeStr.split("x");
                    if (sizeParts.length == 2) {
                        this.width = Integer.parseInt(sizeParts[0]);
                        this.height = Integer.parseInt(sizeParts[1]);
                    } else {
                        // Default size if parsing fails
                        this.width = 32;
                        this.height = 32;
                    }
                } else {
                    // No size found, use defaults
                    this.name = parts[0];
                    this.width = 32;
                    this.height = 32;
                }
            } else {
                // Default values if parsing fails
                this.name = "unknown";
                this.width = 32;
                this.height = 32;
                this.propHeight = 1;
            }
        } catch (Exception e) {
            System.err.println("Error parsing prop filename: " + filename + " - " + e.getMessage());
            // Set default values
            this.name = "unknown";
            this.width = 32;
            this.height = 32;
            this.propHeight = 1;
        }
    }


    @Override
    public String toString() {
        return String.format("Prop{name='%s', size=%dx%d, format='%s'}",
                name, width, height, format);
    }


}