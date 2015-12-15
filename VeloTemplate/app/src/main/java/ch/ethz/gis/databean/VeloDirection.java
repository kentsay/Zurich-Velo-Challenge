package ch.ethz.gis.databean;


import java.text.DecimalFormat;

public class VeloDirection {

    private Double length;
    private Double time;
    private String text;
    private String directionType;

    public int getLength() {
        return length.intValue();
    }

    public void setLength(double length) {
        this.length = length;
    }

    public String getTime() {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(time);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDirectionType() {
        return directionType;
    }

    public void setDirectionType(String directionType) {
        this.directionType = directionType;
    }

}
