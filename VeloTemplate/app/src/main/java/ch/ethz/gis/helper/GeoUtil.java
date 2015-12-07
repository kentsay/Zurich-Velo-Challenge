package ch.ethz.gis.helper;


import android.graphics.Color;
import android.location.Location;

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

import ch.ethz.gis.velotemplate.R;

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

            //double[] coord = CoordinatesUtil.LV03toWGS84(point[0], point[1], point[2]);
            //coordinate.add(new LatLng(coord[0], coord[1]));
            coordinate.add(new LatLng(point[1], point[0]));
        }

        //customise the linestring style for a GeoJsonFeature
        GeoJsonLineString line = new GeoJsonLineString(coordinate);
        GeoJsonFeature routeFeature = new GeoJsonFeature(line, null, null, null);
        GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
        lineStringStyle.setColor(Color.MAGENTA);
        lineStringStyle.setWidth(20);
        routeFeature.setLineStringStyle(lineStringStyle);
        layer.addFeature(routeFeature);

        // Show the 2nd routing instruction on the map

        JSONArray features = json.getJSONArray("directions").
                getJSONObject(0).
                getJSONArray("features");

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            String cg = feature.getString("compressedGeometry");
            String instruction = feature.getJSONObject("attributes").getString("text");

            GeoJsonLineString cgline = createPathFromCompressedGeometry(cg);
//            GeoJsonFeature cgFeature = new GeoJsonFeature(cgline, null, null, null);
//            GeoJsonLineStringStyle cglineStringStyle = new GeoJsonLineStringStyle();
//            cglineStringStyle.setColor(Color.RED);
//            cgFeature.setLineStringStyle(cglineStringStyle);
//            layer.addFeature(cgFeature);

            GeoJsonPoint endPoint = new GeoJsonPoint(cgline.getCoordinates().get(0));
            GeoJsonFeature epFeature = new GeoJsonFeature(endPoint, null, null, null);
            GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
            pointStyle.setTitle(instruction);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_flag);
            pointStyle.setIcon(icon);
            epFeature.setPointStyle(pointStyle);
            layer.addFeature(epFeature);
        }

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
}
