package ponjoDEV.RPG.ImageProcessing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TextureController {
    private String path = "C:\\Program Files\\RPGMapGenerator\\RPGMapTextures";
    private String[] textureFolders = new String[]{"Elfic","Dwarfic"};

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getTextureFolders() {
        return textureFolders;
    }

    public void setTextureFolders(String[] textureFolders) {
        this.textureFolders = textureFolders;
    }

    public String[] refreshFolders(String path) {
        try {
            Path dirPath = Paths.get(path);

            // Check if the path exists and is a directory
            if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
                return new String[0]; // Return empty array if path doesn't exist or isn't a directory
            }

            // Get all directories in the specified path
            List<String> folders = Files.list(dirPath).filter(Files::isDirectory).map(p -> p.getFileName().toString()).sorted().collect(Collectors.toList());

            // Update the textureFolders field
            this.textureFolders = folders.toArray(new String[0]);

            return this.textureFolders;

        } catch (IOException e) {
            // Handle any IO exceptions (permission issues, etc.)
            System.err.println("Error reading directories from path: " + path);
            e.printStackTrace();
            return new String[0]; // Return empty array on error
        }
    }
}
