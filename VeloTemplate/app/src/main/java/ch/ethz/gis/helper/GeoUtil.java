package ch.ethz.gis.helper;


import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineString;
import com.google.maps.android.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.databean.VeloDirection;
import ch.ethz.gis.R;

public class GeoUtil {
    public enum LineType {BIKE_ROUTE, NAVIGATION};
    private static GeoJsonLineStringStyle bikeRouteStyle;
    private static GeoJsonLineStringStyle navStyle;
    private static List<VeloDirection> veloDirectionList;
    private static List<LatLng> coordinateList;

    public static GeoJsonLineStringStyle getLineStringStyle(LineType lineType){
        switch(lineType){
            case BIKE_ROUTE:
                if(bikeRouteStyle == null){
                    bikeRouteStyle = new GeoJsonLineStringStyle();
                    bikeRouteStyle.setColor(Color.rgb(0, 146, 255));
                    bikeRouteStyle.setWidth(10);
                }
                return bikeRouteStyle;
            case NAVIGATION:
                if(navStyle == null){
                    navStyle = new GeoJsonLineStringStyle();
                    navStyle.setColor(Color.rgb(150, 52, 132));
                    navStyle.setWidth(10);
                }
                return navStyle;
            default:
                return new GeoJsonLineStringStyle();
        }
    }

    public static void setVeloDirection(JSONObject json) throws JSONException {
        if (json != null) {
            VeloDirection direction = new VeloDirection();
            direction.setLength(json.getDouble("length"));
            direction.setTime(json.getDouble("time"));
            direction.setText(json.getString("text"));
            direction.setDirectionType(json.getString("maneuverType"));
            veloDirectionList.add(direction);
        }
    }

    public static List<VeloDirection> getVeloDirectionList() {
            return veloDirectionList;
    }

    public static List<LatLng> getCoordinateList() {
        return coordinateList;
    }

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

            coordinate.add(new LatLng(point[1], point[0]));
        }

        //customise the linestring style for a GeoJsonFeature
        GeoJsonLineString line = new GeoJsonLineString(coordinate);
        GeoJsonFeature routeFeature = new GeoJsonFeature(line, null, null, null);
        routeFeature.setLineStringStyle(getLineStringStyle(LineType.NAVIGATION));
        layer.addFeature(routeFeature);

        JSONArray features = json.getJSONArray("directions").
                getJSONObject(0).
                getJSONArray("features");

        //reset veloDirectionList, nav_coordinate
        veloDirectionList = new ArrayList<>();
        coordinateList = new ArrayList<>();

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            setVeloDirection(feature.optJSONObject("attributes"));

            String cg = feature.getString("compressedGeometry");
            String instruction = feature.getJSONObject("attributes").getString("text");

            GeoJsonLineString cgline = createPathFromCompressedGeometry(cg);
            GeoJsonPoint endPoint = new GeoJsonPoint(cgline.getCoordinates().get(0));
            coordinateList.add(endPoint.getCoordinates());
            GeoJsonFeature epFeature = new GeoJsonFeature(endPoint, null, null, null);
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setTitle(instruction);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_red_pin);
            pointStyle.setIcon(icon);
            epFeature.setPointStyle(pointStyle);
            layer.addFeature(epFeature);
        }

        return layer;
    }

    public static int extractSummary(JSONObject json, String key) throws JSONException {
        int value = 0;
        if (key.equals("totalLength")) {
            value = json.getJSONArray("directions").
                    getJSONObject(0).
                    getJSONObject("summary").getInt("totalLength");
            value = value / 1000;
        } else if (key.equals("totalTime")) {
            Double time = json.getJSONArray("directions").
                    getJSONObject(0).
                    getJSONObject("summary").getDouble("totalTime");
            value = time.intValue();
        }
        return value;
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

    public static LatLng LocationToLatLng(Location location) {
        if(location != null)
            return new LatLng(location.getLatitude(), location.getLongitude());
        else
            return null;
    }

    public static Location LatLngToLocation(LatLng latLng) {
        if(latLng != null) {
            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            return location;
        }
        else
            return null;
    }

    public static Location getCurrentLocation(GoogleApiClient mGoogleApiClient) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient); //mMap.getMyLocation();
        if(location == null){
            // To prevent the emulator from crashing...
            Log.d("GeoUtil", "Location service not available");
            location = new Location("");
            location.setLatitude(47.408);
            location.setLongitude(8.507);
            location.setBearing(90);
        }
        return location;
    }

    public static GeoJsonLineString createPathFromCompressedGeometry(String cgString) {
        // Modified from https://www.arcgis.com/home/item.html?id=feb080c524f84afebd49725d083b56ae
        // Decompress 'CompressedGeometry' string to GeoJsonLingString
        // (In our application, the Z and M value are ignored.)

        List<LatLng> coordinate = new ArrayList<>();
        int flags = 0;
        int[] nIndex_XY = { 0 };
        int[] nIndex_Z = { 0 };
        int[] nIndex_M = { 0 };
        int dMultBy_XY = 0;
        int dMultBy_Z = 0;
        int dMultBy_M = 0;

        int firstElement = extractInt(cgString, nIndex_XY);
        if (firstElement == 0) {// 10.0+ format
            int version = extractInt(cgString, nIndex_XY);
            if (version != 1)
                throw new IllegalArgumentException(
                        "Compressed geometry: Unexpected version.");

            flags = extractInt(cgString, nIndex_XY);
            if ((0xfffffffc & flags) != 0)
                throw new IllegalArgumentException(
                        "Compressed geometry: Invalid flags.");

            dMultBy_XY = extractInt(cgString, nIndex_XY);
        } else
            dMultBy_XY = firstElement;

        int nLength = cgString.length();
        if (flags != 0) {
            nLength = cgString.indexOf('|');
            if ((flags & 1) == 1) {
                nIndex_Z[0] = nLength + 1;
                dMultBy_Z = extractInt(cgString, nIndex_Z);
            }
            if ((flags & 2) == 2) {
                nIndex_M[0] = cgString.indexOf('|', nIndex_Z[0]) + 1;
                dMultBy_M = extractInt(cgString, nIndex_M);
            }
        }
        int nLastDiffX = 0;
        int nLastDiffY = 0;
        int nLastDiffZ = 0;
        int nLastDiffM = 0;

        while (nIndex_XY[0] < nLength) {
            // X
            int nDiffX = extractInt(cgString, nIndex_XY);
            int nX = nDiffX + nLastDiffX;
            nLastDiffX = nX;
            double dX = (double) nX / dMultBy_XY;

            // Y
            int nDiffY = extractInt(cgString, nIndex_XY);
            int nY = nDiffY + nLastDiffY;
            nLastDiffY = nY;
            double dY = (double) nY / dMultBy_XY;

            coordinate.add(new LatLng(dY, dX));

            if ((flags & 1) == 1) {// has Zs
                int nDiffZ = extractInt(cgString, nIndex_Z);
                int nZ = nDiffZ + nLastDiffZ;
                nLastDiffZ = nZ;
                double dZ = (double) nZ / dMultBy_Z;
            }
            if ((flags & 2) == 2) {// has Ms
                int nDiffM = extractInt(cgString, nIndex_M);
                int nM = nDiffM + nLastDiffM;
                nLastDiffM = nM;
                double dM = (double) nM / dMultBy_M;
            }
        }
        return (new GeoJsonLineString(coordinate));
    }

    private static int extractInt(String cgString, int[] index) {
        /**
         * Read one integer from compressed geometry string by using passed
         * position Returns extracted integer, and re-writes nStartPos for the
         * next integer
         */
        int i = index[0] + 1;
        while (i < cgString.length() && cgString.charAt(i) != '-'
                && cgString.charAt(i) != '+' && cgString.charAt(i) != '|')
            i++;

        String sr32 = cgString.substring(index[0], i);
        index[0] = i;
        return Integer.parseInt(sr32.replace("+", ""), 32);
    }

    public static double getDistance(LatLng start, LatLng end) {
        double distance = Math.sqrt((Math.pow(start.latitude - end.latitude,2)+ Math.pow(start.longitude - end.longitude,2)));
        return distance;
    }
}
