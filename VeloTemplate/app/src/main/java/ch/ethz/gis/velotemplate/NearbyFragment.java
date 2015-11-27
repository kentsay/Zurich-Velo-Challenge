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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.gis.helper.VolleyHelper;


public class NearbyFragment extends Fragment {

    //TODO: get current location and return near by rental station

    MapView mMapView;
    static final double[] zurich = {47.375806, 8.528130};

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
            public void onMapReady(final GoogleMap map) {
                map.setMyLocationEnabled(true);
                // Locate at Canton of Zurich
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zurich[0], zurich[1]), 10));
                // Move to user's location
                Location location = map.getMyLocation();
                if(location != null) {
                    LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12));
                }
                else
                    Log.d("location", "Cannot get current location");

                showStations(getString(R.string.rental_station_json), map);
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

    private void showStations(String url, final GoogleMap map){
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray features = null;
                        try {
                            features = response.getJSONArray("features");
                            for(int i = 0; i < features.length(); i++){
                                JSONObject station = features.getJSONObject(i);
                                JSONArray coordinate = station.getJSONObject("geometry").getJSONArray("coordinates");
                                Log.d("Volley", coordinate.toString());
                                LatLng latlng = new LatLng(coordinate.getDouble(1), coordinate.getDouble(0));
                                map.addMarker(new MarkerOptions().position(latlng));
                            }
                        } catch(JSONException e){
                            Log.d("Volley", "getJSONObject failed");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "JSONObjectRequest error");
                    }
                });
        VolleyHelper.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

}
