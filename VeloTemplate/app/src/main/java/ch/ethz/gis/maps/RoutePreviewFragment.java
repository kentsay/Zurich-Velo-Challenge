package ch.ethz.gis.maps;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonLineString;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlLineString;
import com.google.maps.android.kml.KmlPlacemark;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.gis.helper.CoordinatesUtil;
import ch.ethz.gis.helper.GeoUtil;
import ch.ethz.gis.helper.SharedPreference;
import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.helper.VolleyHelper;
import ch.ethz.gis.velotemplate.R;
import ch.ethz.gis.velotemplate.VeloDirectionListFragment;
import ch.ethz.gis.velotemplate.VeloRoute;


public class RoutePreviewFragment extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final static String ID_EXTRA = "ROUTE";

    public GoogleMap mMap;
    public LinkedList<GroundOverlay> allOverlays = new LinkedList<>();
    private Projection projection;
    private LatLngBounds mapBounds;
    private int [] dimensions = new int[2];

    private LatLng beginLatLog;
    private LatLng routeStartPoint;
    private String kmlUrl = "";

    private SharedPreference sharedPreference;
    private VeloDbHelper dbHelper;
    private VeloRoute route;
    private MenuItem fav;
    private MenuItem unfav;
    private Context context;
    private GeoJsonLayer baseLayer, routingLayer, rentalLayer;
    private Location myLoc;
    private LatLng myLatLng;

    private GoogleApiClient mGoogleApiClient;
    private static final LocationRequest mLocationRequest = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(5000)
            .setSmallestDisplacement(5) // 5 meters
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private WebView myWebView;
    private SlidingUpPanelLayout slidingLayout;

    public int[] getDimensions () {return dimensions;}
    public Projection getProjection(){ return projection;}
    public void setProjection(Projection nProjection) {projection = nProjection;}
    public void setMapBounds(LatLngBounds nMapBounds) {mapBounds = nMapBounds;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        init();

        // TODO: call elevation api and plot the altitude profile in the webview
        //myWebView = (WebView) findViewById(R.id.map_elevation);
        //myWebView.loadUrl("https://www.google.com.tw/?gws_rd=ssl");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelCollapsed(View view) {
            }

            @Override
            public void onPanelExpanded(View view) {
            }

            @Override
            public void onPanelAnchored(View view) {
            }

            @Override
            public void onPanelHidden(View view) {
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap nMap) {
        mMap = nMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(beginLatLog, 8));
        Projection  tempProjection = mMap.getProjection();
        setProjection(tempProjection);
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        String[] wmsurlsTest = {kmlUrl};
        loadKML loadKMLThread = new loadKML();
        loadKMLThread.execute(wmsurlsTest);

        // Get initial location
        myLoc = GeoUtil.getCurrentLocation(mGoogleApiClient);
        myLatLng = GeoUtil.LocationToLatLng(myLoc);
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition camPos){
                setProjection(mMap.getProjection());
                loadBasemap loadBasemapThread = new loadBasemap();
                loadBasemapThread.execute();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        fav = menu.findItem(R.id.action_bar_favorite_add);
        unfav = menu.findItem(R.id.action_bar_favorite_remove);
        if (dbHelper.checkFavouriteRouteExists(route.getId())) {
            fav.setVisible(false);
            unfav.setVisible(true);
        } else {
            fav.setVisible(true);
            unfav.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bar_favorite_add:
                //reset action bar icon to show fav or unfav
                fav.setVisible(false);
                unfav.setVisible(true);
                Toast.makeText(context, "Added to My Favorite", Toast.LENGTH_SHORT).show();
                dbHelper.addFavouriteRoute(route);
                return true;
            case R.id.action_bar_favorite_remove:
                //reset action bar icon to show fav or unfav
                unfav.setVisible(false);
                fav.setVisible(true);
                Toast.makeText(context, "Removed from My Favorite", Toast.LENGTH_SHORT).show();
                dbHelper.deleteFromFavourite(route.getId());
                return true;
            case R.id.navigation:
                //open navigation menu
                openNavigation();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void init() {
        dbHelper = VeloDbHelper.getInstance(this);
        context = getApplicationContext();

        //init location for centre Zurich
        beginLatLog = new LatLng(47.375806, 8.528130);

        // read the route data from previous activity
        Intent i = getIntent();
        route  = (VeloRoute)i.getSerializableExtra(ID_EXTRA);
        kmlUrl = route.getKml_url();

        //Shared Preference setting
        sharedPreference = new SharedPreference(this);

        // GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Request the rental stations
        getRentalLocation(getString(R.string.rental_station_json));

        // define the necessary size of the map and create the corresponding URL
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dimensions[0] = size.x; //width
        dimensions[1] = size.y; //height

        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fm.getMapAsync(this);

        //init sliding layout and hide in the beginning tail user start navigation
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }


    public boolean openNavigation() {
        final String [] options = new String [] {"To the nearest rental station", "To velo route"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Start Navigation");

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    navToStation();
                } else if(which == 1) {
                    navToRoute();
                    /**
                     *  Some steps about the routing service
                     *  Pre-process:
                     *  1. Get the current location
                     *  2. Load the velo route and extract the staring point, or the nearest point from current location
                     *  3. Pass the current location and starting point into the WMS routing service
                     *
                     *  WMS process:
                     *  1. call REST API through Volley(done)
                     *  2. Parse the json object into several information:
                     *      - layer: JSONObject->routes->features[0]->geometry->paths[array]
                     *      - directions: JSONObject->directions->summary
                     *                    JSONObject->directions->features[array]
                     *  3. Convert layer into GsonLayer and display on baseMap
                     *  4. Convert directions data into adapter for display use
                     *
                     *  Post-process:
                     *  1. Create the Routing List Fragment and populates the Listview (Navigation Drawer) using a custom adapter
                     *  2. (not sure) When the user clicks on the Get Direction button on the bottom layout, a dialog box appears
                     *  3. (not sure) LocationListener will keep updating. When user is approaching some specific checkpoint, the
                     *      dialog box will appears and show the next direction
                     */
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    private boolean navToStation() {
        // iterate through all rental stations to find the closest one to the current location
        double distance = Math.pow(10,10);
        LatLng bestStation = new LatLng(0,0);
        for (GeoJsonFeature feature : rentalLayer.getFeatures()) {
            LatLng rentalLocation = ((GeoJsonPoint)feature.getGeometry()).getCoordinates();

            double tempdistance = GeoUtil.getDistance(myLatLng, rentalLocation);
            if (tempdistance < distance) {
                bestStation = new LatLng(rentalLocation.latitude,rentalLocation.longitude);
                distance = tempdistance;
            }
        }

        double[] locationSwiss = CoordinatesUtil.WGS84toLV03(myLatLng.latitude, myLatLng.longitude,0);
        double[] rentalStationSwiss = new double[2];
        rentalStationSwiss[0] = CoordinatesUtil.WGStoCHy(bestStation.latitude, bestStation.longitude);
        rentalStationSwiss[1] = CoordinatesUtil.WGStoCHx(bestStation.latitude, bestStation.longitude);
        volleyLoadRoute(locationSwiss[0], locationSwiss[1], rentalStationSwiss[0], rentalStationSwiss[1]);

        Location destLoc = GeoUtil.LatLngToLocation(bestStation);
        cameraLookFromTo(mMap, myLoc, destLoc);

        return true;
    }

    private void getRentalLocation(String url){
        // Request JSON file by Volley
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        rentalLayer = GeoUtil.addJsonFeature(mMap, response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "JSONObjectRequest error");
                    }
                });
        VolleyHelper.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    private void navToRoute() {
        //myWebView.setVisibility(View.GONE);
        double[] location = CoordinatesUtil.WGS84toLV03(myLatLng.latitude, myLatLng.longitude, 0);
        double[] destination = CoordinatesUtil.WGS84toLV03(routeStartPoint.latitude, routeStartPoint.longitude, 0);
        volleyLoadRoute(location[0], location[1], destination[0], destination[1]);

        Location destLoc = GeoUtil.LatLngToLocation(routeStartPoint);
        cameraLookFromTo(mMap, myLoc, destLoc);
    }

    private void showDirectionList() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.direction_box, new VeloDirectionListFragment()).commit();
    }

    /**
     * Call WMS routing service and display the route on map layer. The input parameters should all be in Swiss Coordinate format.
     * @param start_y
     * @param start_x
     * @param end_y
     * @param end_x
     */
    private void volleyLoadRoute(double start_y, double start_x, double end_y, double end_x) {
        String default_url = sharedPreference.getValue("route_url");
        int strStart = default_url.indexOf("stops=") + 6;
        String url = default_url.substring(0, strStart) + String.format("%f%%2C%f%%3B%f%%2C%f", start_y, start_x, end_y, end_x);
        Log.d("map", url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("map", response.toString());
                        if (response != null) {
                            try {
                                if(routingLayer != null)
                                    routingLayer.removeLayerFromMap();
                                routingLayer = GeoUtil.convert(mMap, response);
                                int totalLength = GeoUtil.extractSummary(response, "totalLength");
                                int totalTime   = GeoUtil.extractSummary(response, "totalTime");

                                //TODO: add summary in a better way instead of using toast
                                Toast toast = Toast.makeText(context, totalTime + " min (" + totalLength+ " km)", Toast.LENGTH_LONG);
                                toast.show();
                                showDirectionList();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            routingLayer.addLayerToMap();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("GeoJSONLoader", error.getMessage());
                    }
                });

        VolleyHelper.getInstance(this.context).addToRequestQueue(jsObjRequest);
    }

    public class loadBasemap extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            int[] dimensions = getDimensions();
            setMapBounds(getProjection().getVisibleRegion().latLngBounds);
            // resolve the LatLngBounds to doubles
            double[] bounds = new double[4];
            bounds[0] = mapBounds.southwest.latitude;
            bounds[1] = mapBounds.southwest.longitude;
            bounds[2] = mapBounds.northeast.latitude;
            bounds[3] = mapBounds.northeast.longitude;

            /**
             * check if the wanted layer is outside of the queryable layer -> download not needed layer bounding box of opendata Zurich
             * http://www.gis.stadt-zuerich.ch/maps/services/wms/WMS-ZH-STZH-OGD/MapServer/WMSServer?SERVICE=WMS&Request=GetCapabilities
             * <BoundingBox CRS="EPSG:4326" minx="47.310593" miny="8.427483" maxx="47.447433" maxy="8.633024"/>
             */
            if (bounds[0] > 47.310593 && bounds[1] > 8.427483 && bounds[2] < 47.447433 && bounds[3] < 8.633024) {
                // we are inside the bounds -> download layer
                String wmsUrl = sharedPreference.getValue("base_map_url") +
                        Double.toString(bounds[0]) + "," + Double.toString(bounds[1]) + "," + Double.toString(bounds[2]) + ","
                        + Double.toString(bounds[3]) + "&WIDTH=" + dimensions[0] + "&HEIGHT=" + dimensions[1] + "&Layers=Stadtplan";

                URL url = null;
                try {
                    url = new URL(wmsUrl);
                } catch (MalformedURLException e) {
                    Log.e("WMSLoader:Stream", e.getMessage());
                }
                InputStream input = null;
                try {
                    input = url.openStream();
                } catch (IOException e) {
                    Log.wtf("WMSLoader:Stream", "Error in WMSLoader: " + e.getMessage());
                }
                return BitmapFactory.decodeStream(input);
            } else {
                // bounds are outside the boundingBox
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                GroundOverlay newOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .positionFromBounds(mapBounds));
                allOverlays.push(newOverlay);
                //  if the total number of overlays is bigger than 3, the last is removed
                if (allOverlays.size()> 3) {
                    Log.d("allOverlay", "Array is bigger than 3!");
                    GroundOverlay remObject = allOverlays.getLast();
                    remObject.remove(); //removes overlay from the map
                    allOverlays.removeLast(); // removes the entry in the LinkedList
                }
            }
        }
    }

    public class loadKML extends AsyncTask<String, Void, KmlLayer> {

        @Override
        protected KmlLayer doInBackground(String... kmlurls) {

            /** Fix the looper problem
                The exception is thrown because you (or core Android code) has already called Looper.prepare()
                for the current executing thread.
                The following checks whether a Looper already exists for the current thread, if not, it creates one,
                thereby avoiding the RuntimeException.
             */
            if (Looper.myLooper() == null)
                Looper.prepare();

            String kmlUrl = kmlurls[0];
            URL url = null;
            KmlLayer kmlLayer = null;

            try {
                url = new URL(kmlUrl);
                Log.v("kml", kmlUrl);
            } catch (MalformedURLException e) {
                Log.e("KMLLoader:Stream", e.getMessage());
            }
            InputStream input = null;
            try {
                input = url.openStream();
                kmlLayer = new KmlLayer(mMap,input,context);
            } catch (IOException e) {
                Log.e("KMLLoader:StreamIO", "Error in KMLLoader: " + e.getMessage());
            } catch (XmlPullParserException e) {
                Log.e("KMSLoader:StreamXML", "Error setting the kml layer: " + e.getMessage());
            }

            return kmlLayer;
        }

        @Override
        protected void onPostExecute(KmlLayer kmlLayer) {
            JSONObject geoJson = new JSONObject();
            try {
                geoJson.put("type", "Feature");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            baseLayer = new GeoJsonLayer(mMap, geoJson);

            KmlContainer container = kmlLayer.getContainers().iterator().next();
            KmlPlacemark placemark = container.getPlacemarks().iterator().next();
            KmlLineString lineString = (KmlLineString) placemark.getGeometry();
            routeStartPoint = extractNearestPointFromRoute(GeoUtil.LocationToLatLng(myLoc), lineString);

            //Create LatLngBounds of the outer coordinates of the polygon
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : lineString.getGeometryObject()) {
                builder.include(latLng);
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 2));

            //Change route style by creating a new layer
            List<LatLng> coordinate = lineString.getGeometryObject();
            GeoJsonLineString line = new GeoJsonLineString(coordinate);
            GeoJsonFeature routeFeature = new GeoJsonFeature(line, null, null, null);
            routeFeature.setLineStringStyle(GeoUtil.getLineStringStyle(GeoUtil.LineType.BIKE_ROUTE));
            baseLayer.addFeature(routeFeature);
            baseLayer.addLayerToMap();
        }
    }

    private LatLng extractNearestPointFromRoute(LatLng currentlocation, KmlLineString lineString) {
        int len = lineString.getGeometryObject().size()-1;
        LatLng start = new LatLng(lineString.getGeometryObject().get(0).latitude, lineString.getGeometryObject().get(0).longitude);
        LatLng end   = new LatLng(lineString.getGeometryObject().get(len).latitude, lineString.getGeometryObject().get(len).longitude);

        if (GeoUtil.getDistance(currentlocation, start) > GeoUtil.getDistance(currentlocation, end))
            return end;
        else
            return start;
    }

    private void cameraLookFromTo(GoogleMap map, Location start, Location dest){
        CameraPosition camPos = new CameraPosition.Builder()
                .target(GeoUtil.LocationToLatLng(start))
                .zoom(14)
                .bearing(start.bearingTo(dest))
                .tilt(60)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    @Override
    public void onLocationChanged(Location location) {
        myLoc = location;
        myLatLng = GeoUtil.LocationToLatLng(myLoc);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng));
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d("GoogleApiClient", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.d("GoogleApiClient", "Connection suspended");
        mGoogleApiClient.connect();
    }
}