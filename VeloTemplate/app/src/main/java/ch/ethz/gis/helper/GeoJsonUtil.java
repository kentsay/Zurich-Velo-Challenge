package ch.ethz.gis.helper;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class GeoJsonUtil {

    public static GeoJsonLayer covert(GoogleMap mMap, JSONObject json) {
        GeoJsonLayer layer;
        JSONObject geoJson = new JSONObject();

        /* testing data to add GeoJsonPoint back to GeoJsonLayer*/
        double[] coord = CoordinatesUtil.LV03toWGS84(680200.7672000006, 245139.80000000075, 0);
        GeoJsonPoint point = new GeoJsonPoint(new LatLng(coord[0], coord[1]));
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Ocean", "South Atlantic");
        GeoJsonFeature pointFeature = new GeoJsonFeature(point, "Origin", properties, null);
        GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
        pointStyle.setTitle(pointFeature.getProperty("Name"));
        pointFeature.setPointStyle(pointStyle);
        /* end of testing */

        try {
            geoJson.put("type", "Feature");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        layer = new GeoJsonLayer(mMap, geoJson);
        layer.addFeature(pointFeature);


        return layer;
    }
}
