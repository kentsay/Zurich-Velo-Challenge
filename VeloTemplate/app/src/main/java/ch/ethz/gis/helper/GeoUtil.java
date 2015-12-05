package ch.ethz.gis.helper;


import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineString;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GeoUtil {

    public static GeoJsonLayer convert(GoogleMap mMap, JSONObject json) throws JSONException {
        GeoJsonLayer layer;
        JSONObject geoJson = new JSONObject();

        //init layer
        geoJson.put("type", "Feature");
        layer = new GeoJsonLayer(mMap, geoJson);

        List<LatLng> coordinate = new ArrayList<>();

        //extract data point from json
        JSONArray paths = json.getJSONObject("routes").
                               getJSONArray("features").
                               getJSONObject(0).
                               getJSONObject("geometry").
                               optJSONArray("paths");

        JSONArray outerArray = paths.getJSONArray(0);

        for(int i = 0; i < outerArray.length(); i++) {
            JSONArray innerArray = outerArray.getJSONArray(i);
            double[] point = new double[innerArray.length()];

            for(int j = 0; j < innerArray.length(); j++)
                point[j] = innerArray.getDouble(j);

            double[] coord = CoordinatesUtil.LV03toWGS84(point[0], point[1], point[2]);
            coordinate.add(new LatLng(coord[0], coord[1]));
        }

        //customise the linestring style for a GeoJsonFeature
        GeoJsonLineString line = new GeoJsonLineString(coordinate);
        GeoJsonFeature routeFeature = new GeoJsonFeature(line, null, null, null);
        GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
        lineStringStyle.setColor(Color.RED);
        routeFeature.setLineStringStyle(lineStringStyle);

        layer.addFeature(routeFeature);
        return layer;
    }

    public static GeoJsonLayer addJsonFeature(GoogleMap mMap, JSONObject response) {
        GeoJsonLayer mLayer = new GeoJsonLayer(mMap, response);
        for (GeoJsonFeature feature : mLayer.getFeatures()) {
            if (feature.hasProperty("Name")) {
                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
                pointStyle.setTitle(feature.getProperty("Name"));
                feature.setPointStyle(pointStyle);
            }
        }
        mLayer.addLayerToMap();
        return mLayer;
    }

    public static Location getCurrentLocation(GoogleMap mMap) {
        Location location = mMap.getMyLocation();
        return location;
    }

}
