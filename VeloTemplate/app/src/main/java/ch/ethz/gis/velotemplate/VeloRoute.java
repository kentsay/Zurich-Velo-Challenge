package ch.ethz.gis.velotemplate;

import java.io.Serializable;

/**
 * Created by kentsay on 23/10/2015.
 */
public class VeloRoute implements Serializable {
    String route_name;
    String route_distance;
    String route_height;
    String snapshot_url;
    String elevation;
    String kml_url;

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public void setRoute_distance(String route_distance) {
        this.route_distance = route_distance;
    }

    public void setRoute_height(String route_height) {
        this.route_height = route_height;
    }

    public void setSnapshot_url(String snapshot_url) {
        this.snapshot_url = snapshot_url;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public void setKml_url(String kml_url) {
        this.kml_url = kml_url;
    }

    public String getRoute_name() {
        return route_name;
    }

    public String getRoute_distance() {
        return route_distance;
    }

    public String getRoute_height() {
        return route_height;
    }

    public String getSnapshot_url() {
        return snapshot_url;
    }

    public String getElevation() {
        return elevation;
    }

    public String getKml_url() {
        return kml_url;
    }
}
