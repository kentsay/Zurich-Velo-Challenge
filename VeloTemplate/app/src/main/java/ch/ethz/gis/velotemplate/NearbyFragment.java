package ch.ethz.gis.velotemplate;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.geojson.GeoJsonPointStyle;

import org.xmlpull.v1.XmlPullParser;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import ch.ethz.gis.helper.VolleyHelper;
import ch.ethz.gis.helper.XMLRequest;


public class NearbyFragment extends Fragment {

    //TODO: get current location and return near by rental station

    MapView mMapView;
    GoogleMap mMap;
    GeoJsonLayer mLayer;
    private static final double[] zurich = {47.375806, 8.528130};
    private static final double latlng_thres = 1e-2;
    private static final int TAG_NB_BIKE = 1;
    private static final int TAG_X = 2;
    private static final int TAG_Y = 3;
    private static final int TAG_NAME = 4;

    public static NearbyFragment newInstance(String param1, String param2) {
        NearbyFragment fragment = new NearbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nearby, container, false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mMap = map;
                mMap.setMyLocationEnabled(true);
                // Locate at Canton of Zurich
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zurich[0], zurich[1]), 10));
                // Move to user's location
                Location location = mMap.getMyLocation();
                if (location != null) {
                    LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12));
                } else
                    Log.d("location", "Cannot get current location");

                getRentalLocation(getString(R.string.rental_station_json));
                getRentalCurrentInfo(getString(R.string.rental_info_xml));
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void getRentalLocation(String url){
        // Request JSON file by Volley
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mLayer = new GeoJsonLayer(mMap, response);
                        for (GeoJsonFeature feature : mLayer.getFeatures()) {
                            if (feature.hasProperty("Name")) {
                                GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
                                pointStyle.setTitle(feature.getProperty("Name"));
                                feature.setPointStyle(pointStyle);
                            }
                        }
                        mLayer.addLayerToMap();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "JSONObjectRequest error");
                    }
                });
        VolleyHelper.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    private void getRentalCurrentInfo(String url){
        // Since the datasets do not have common station id/name,
        // we need to match stations of 2 datasets according to their coordinates...

        // Request XML file by Volley
        XMLRequest xmlRequest = new XMLRequest(Request.Method.GET, url, new Response.Listener<XmlPullParser>(){
            @Override
            public void onResponse(XmlPullParser response) {
                try {
                    int eventType = response.getEventType();
                    ArrayList<String> stationName = new ArrayList<String>();    // For debugging
                    ArrayList<String> numBike = new ArrayList<String>();
                    ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
                    String tag;
                    int tagType = 0;
                    double x = 0;
                    double y = 0;
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                tag = response.getName();
                                if(tag.equals("nb_bike")) tagType = TAG_NB_BIKE;
                                else if(tag.equals("x"))  tagType = TAG_X;
                                else if(tag.equals("y"))  tagType = TAG_Y;
                                else if(tag.equals("station_name"))  tagType = TAG_NAME;
                                break;
                            case XmlPullParser.TEXT:
                                String text = response.getText();
                                if(tagType == TAG_NB_BIKE)
                                    numBike.add(text);
                                else if(tagType == TAG_X)
                                    x = Double.parseDouble(text);
                                else if(tagType == TAG_Y){
                                    y = Double.parseDouble(text);
                                    coordinates.add(new LatLng(y, x));
                                }
                                else if(tagType == TAG_NAME)
                                    stationName.add(text);
                                break;
                            case XmlPullParser.END_TAG:
                                tag = response.getName();
                                if(tag.equals("nb_bike") || tag.equals("x") || tag.equals("y") || tag.equals("station_name"))
                                    tagType = 0;
                                break;
                        }
                        eventType = response.next();
                    }
                    for (GeoJsonFeature feature : mLayer.getFeatures()) {
                        // For each station in dataset 1, find the corresponding one in dataset 2
                        LatLng point1 = ((GeoJsonPoint)feature.getGeometry()).getCoordinates();
                        float min_dist = Float.MAX_VALUE;
                        int idx = 0;
                        for(int i = 0; i < coordinates.size(); i++) {
                            LatLng point2 = coordinates.get(i);
                            float[] dist = new float[1];
                            Location.distanceBetween(point1.latitude, point1.longitude, point2.latitude, point2.longitude, dist);
                            if(dist[0] < min_dist) {
                                min_dist = dist[0];
                                idx = i;
                            }
                        }
                        GeoJsonPointStyle pointStyle = feature.getPointStyle();
                        pointStyle.setSnippet("Bikes: " + numBike.get(idx));
                        feature.setPointStyle(pointStyle);

                        // Distance error between 2 datasets
                        Log.d("Nearby", stationName.get(idx) + "\tDist error:\t" + Float.toString(min_dist) + "m");
                    }
                }catch(IOException e){
                    Log.d("XML", "IOException");
                }catch(XmlPullParserException e){
                    Log.d("XML", "XmlPullParserException");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "XMLRequest error");
            }
        });
        VolleyHelper.getInstance(getActivity().getApplicationContext()).addToRequestQueue(xmlRequest);
    }
}
