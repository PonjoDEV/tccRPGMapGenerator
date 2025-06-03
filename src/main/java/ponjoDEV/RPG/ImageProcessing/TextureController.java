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

    public String[] refreshFolders(String path) {
        //TODO NAVIGATE TO THE PATH DESTINATION AND GET THE AVAILABLE FOLDERS, THEN SAVE ONTO texturePacks
        String [] texturePacks = new String[]{path,"Harambe","Please","Forgive","Us"};

        return texturePacks;
    }
}
