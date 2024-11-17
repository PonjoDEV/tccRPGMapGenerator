package ponjoDEV.RPG.Model;

public class Zone {
    int begY = Integer.MAX_VALUE, begX = Integer.MAX_VALUE, endY=0, endX=0, red, green, blue, tag;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;

    public int getBegY() {
        return begY;
    }

    public void setBegY(int begY) {
        this.begY = begY;
    }

    public int getBegX() {
        return begX;
    }

    public void setBegX(int begX) {
        this.begX = begX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setTypeByRGB(int red, int green, int blue){
        //TODO By each used color compare values and set Type name
        if (red == 255){
            if (green == 0){
                if (blue == 0){
                    setType("Construction");
                }else if(blue == 255){
                    setType("Roads");
                }
            }else{
                if (green == 255){
                    if (blue == 0){
                        setType("Desert/Sand");
                    }
                    if (blue == 255){

                    }
                }
            }
        }else{
            if (green == 0){
                if (blue ==0){
                    setType("Mountain");
                }else{
                    setType("Water");
                }
            }else{
                if (green == 255){
                    if (blue ==0){
                        setType("Grassland/Forest");
                    }
                }
            }

        }

    }
}
