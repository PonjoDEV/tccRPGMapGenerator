package ponjoDEV.RPG.ImageProcessing;


public class TextureController {
    private String path = "C:\\Program Files\\RPGMapTextures";
    private String[] textureFolders = new String[]{"sada","pingas"};

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
}
