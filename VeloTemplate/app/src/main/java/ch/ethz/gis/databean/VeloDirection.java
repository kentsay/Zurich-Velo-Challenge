package ch.ethz.gis.databean;


public class VeloDirection {

    private double length;
    private double time;
    private String text;
    private String directionType;

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getTime() {
        return time;
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
