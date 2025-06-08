package ponjoDEV.RPG.Model;

public class Zone {
    int begY = Integer.MAX_VALUE, begX = Integer.MAX_VALUE, endY=0, endX=0, tag, priority, size=0;
    String type;
    int [] initCoord, rgb = new int[3];

    public int[] getRgb() { return rgb; }

    public void setRgb(int[] rgb) { this.rgb = rgb; }

    public int[] getInitCoord() { return initCoord; }

    public void setInitCoord(int[] initCoord) {
        this.initCoord = initCoord;
    }

    public int getPriority() { return priority; }

    public void setPriority(int priority) { this.priority = priority; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {return size;}

    public void setSize(int size) {this.size = size;}

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

    public int getRed() { return rgb[0]; }

    public void setRed(int red) { this.rgb[0] = red; }

    public int getGreen() { return rgb[1]; }

    public void setGreen(int green) {
        this.rgb[1] = green;
    }

    public int getBlue() { return rgb[2]; }

    public void setBlue(int blue) { this.rgb[2] = blue; }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public void setTypeByRGB(int [] rgb) {

        /*Priority Set:
        Construction
        Roads
        Water
        Mountain
        Desert
        Grass
         */
        if (rgb[0] == 255) {
            if (rgb[1] == 0) {
                if (rgb[2] == 0) {
                    setType("Construction");
                    setPriority(6);
                } else if (rgb[2] == 255) {
                    setType("Roads");
                    setPriority(5);
                }
            } else {
                if (rgb[1] == 255) {
                    if (rgb[2] == 0) {
                        setType("Desert");
                        setPriority(2);
                    }
                    if (rgb[2] == 255) {

                    }
                }
            }
        } else {
            if (rgb[1] == 0) {
                if (rgb[2] == 0) {
                    setType("Mountain");
                    setPriority(3);
                } else {
                    setType("Water");
                    setPriority(4);
                }
            } else {
                if (rgb[1] == 255) {
                    if (rgb[2] == 0) {
                        setType("Grassland");
                        setPriority(1);
                    }
                }
            }
        }
    }


}