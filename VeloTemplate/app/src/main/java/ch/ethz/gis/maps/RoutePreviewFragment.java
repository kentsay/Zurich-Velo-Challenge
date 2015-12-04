package ch.ethz.gis.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.maps.android.geojson.GeoJsonFeature;
import com.google.maps.android.geojson.GeoJsonLayer;
import com.google.maps.android.geojson.GeoJsonPoint;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlLineString;
import com.google.maps.android.kml.KmlPlacemark;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import ch.ethz.gis.helper.CoordinatesUtil;

import ch.ethz.gis.helper.GeoJsonUtil;

import ch.ethz.gis.helper.SharedPreference;
import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.helper.VolleyHelper;
import ch.ethz.gis.velotemplate.NearbyFragment;
import ch.ethz.gis.velotemplate.R;
import ch.ethz.gis.velotemplate.VeloRoute;


public class RoutePreviewFragment extends AppCompatActivity implements OnMapReadyCallback {

    public final static String ID_EXTRA = "ROUTE";

    public GoogleMap mMap;
    public LinkedList<GroundOverlay> allOverlays = new LinkedList<>();
    private static Projection projection;
    private static LatLngBounds mapBounds;
    private static int [] dimensions = new int[2];

    //init location for centre Zurich
    private LatLng  poslatlong = new LatLng(47.375806, 8.528130);
    private String kmlUrl = "";

    private SharedPreference sharedPreference;
    private VeloDbHelper dbHelper = VeloDbHelper.getInstance(this);
    private VeloRoute route;
    private MenuItem fav;
    private MenuItem unfav;
    private Context context;
    private GeoJsonLayer RoutingLayer;

    public static int[] getDimensions () {return dimensions;}
    public static Projection getProjection(){ return projection;}
    public static void setProjection(Projection nProjection) {projection = nProjection;}
    public static void setMapBounds(LatLngBounds nMapBounds) {mapBounds = nMapBounds;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        context = getApplicationContext();
        Intent i = getIntent();

        // read the route data from previous activity
        route  = (VeloRoute)i.getSerializableExtra(ID_EXTRA);
        kmlUrl = route.getKml_url();


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Display display = getWindowManager().getDefaultDisplay();
        // define the necessary size of the map and create the corresponding URL
        Point size = new Point();
        display.getSize(size);
        dimensions[0] = size.x; //width
        dimensions[1] = size.y; //height

        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting GoogleMap object from the fragment
        fm.getMapAsync(this);

        //Shared Preference setting
        sharedPreference = new SharedPreference(this);
    }

    @Override
    public void onMapReady(GoogleMap nMap) {
        mMap = nMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poslatlong, 8));
        Projection  tempProjection = mMap.getProjection();
        setProjection(tempProjection);
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setTrafficEnabled(true);
        String[] wmsurlsTest = {kmlUrl};
        loadKML loadKMLThread = new loadKML();
        loadKMLThread.execute(wmsurlsTest);
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition camPos){
                Projection newProjection = mMap.getProjection();
                loadBasemap loadBasemapThread = new loadBasemap();
                loadBasemapThread.execute();
                setProjection(newProjection);
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
                Toast.makeText(context, "Added to My Favourite", Toast.LENGTH_SHORT).show();
                dbHelper.addFavouriteRoute(route);
                return true;
            case R.id.action_bar_favorite_remove:
                //reset action bar icon to show fav or unfav
                unfav.setVisible(false);
                fav.setVisible(true);
                Toast.makeText(context, "Removed from My Favourite", Toast.LENGTH_SHORT).show();
                dbHelper.deleteFromFavourite(route.getId());
                return true;
            case R.id.navigation:
                // if the navigation is clicked
                openNavigation();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean openNavigation() {

        // This function does open the PopUp Menu to select different routing services
        // e.g. routing to the next pumping station, routing along the route, etc.
        final String [] options = new String [] {"To the next rental station", "Start navigation on route"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, options);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Routing");

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // switch statement for the entries. Just take care of the entries, if you changed them above
                if (which == 0) {
                    // To the closest rental station
                    navToStation();
                } else if(which == 1) {
                        // Navigation on the route
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

                    //different testing data
                    volleyLoadRoute(680200.7672000006, 245139.80000000075, 681035.2903000005, 245954.14919999987);
                }

            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        return true;

    }

    public boolean navToStation() {
        // TODO: Find closest rental station and navigate to it
        NearbyFragment nearbyFragment = new NearbyFragment();
        // request the rental stations
        nearbyFragment.getRentalLocation(getString(R.string.rental_station_json));
        // query the current location
        Location mylocation = mMap.getMyLocation();
        GeoJsonLayer mLayer = nearbyFragment.mLayer;
        // iterate through all rental stations to find the closest one to the current location
        double distance = Math.pow(10,10);
        LatLng bestStation;
        for (GeoJsonFeature feature : mLayer.getFeatures()) {
            // get the latitude/longitude of the rental station
            LatLng rentalLocation = ((GeoJsonPoint)feature.getGeometry()).getCoordinates();
            // calculate the distance
            double tempdistance = Math.sqrt((Math.pow(mylocation.getLatitude()-rentalLocation.latitude,2)+ Math.pow(mylocation.getLongitude()-rentalLocation.longitude,2)));
            Log.d("Dist Loc-RentalStation", Double.toString(distance));
            // check distance
            if (tempdistance < distance) {
                // distance to this rental station is shorter...
                bestStation = new LatLng(rentalLocation.latitude,rentalLocation.longitude);
                distance = tempdistance;
            }
        }

        // Transform the coordinates to swiss coordinate System CH1903
        CoordinatesUtil coordinatesUtil = new CoordinatesUtil();
        double[] locationSwiss = coordinatesUtil.WGS84toLV03(mylocation.getLatitude(),mylocation.getLongitude(),mylocation.getAltitude());
        double [] rentalStationSwiss = coordinatesUtil.WGS84toLV03(bestStation.latitude,bestStation.longitude,bestStation.)
        // query the routing service to navigate from current location to the closest rental station


        return true;
    }

    public GeoJsonLayer testConverter(JSONObject json) throws JSONException {
        //TODO: json cannot convert into the right format of GeoJson
        GeoJsonLayer test = GeoJsonUtil.covert(mMap, json);
        return test;
    }

    public void volleyLoadRoute(double start_x, double start_y, double end_x, double end_y) {
        String url = sharedPreference.getValue("route_url");
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("map", response.toString());
                        if (response != null) {
                            try {
                                RoutingLayer = testConverter(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // Add the layer onto the map
                            RoutingLayer.addLayerToMap();
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

        private LatLngBounds mapBounds;

        @Override
        protected Bitmap doInBackground(String... urls) {
            // get all the necessary objects:
            Projection projection = getProjection();
            int[] dimensions = getDimensions();

            // Coordinate system of the display: origin upper-left, x right (width), y down (height)
            LatLng ur = projection.fromScreenLocation(new Point(dimensions[0],0)); //northeast
            LatLng ll = projection.fromScreenLocation(new Point(0,dimensions[1])); //southwest
            // set the new mapBounds
            RoutePreviewFragment.setMapBounds(new LatLngBounds(ll, ur)); // southwest, northwest
            this.mapBounds = new LatLngBounds(ll,ur);
            // resolve the LatLngBounds to doubles
            double[] bounds = new double[4];
            bounds[0] = ll.latitude;
            bounds[1] = ll.longitude;
            bounds[2] = ur.latitude;
            bounds[3] = ur.longitude;

            /**
             * check if the wanted layer is outside of the queryable layer -> download not needed layer bounding box of opendata Zurich
             * http://www.gis.stadt-zuerich.ch/maps/services/wms/WMS-ZH-STZH-OGD/MapServer/WMSServer?SERVICE=WMS&Request=GetCapabilities
             * <BoundingBox CRS="EPSG:4326" minx="47.310593" miny="8.427483" maxx="47.447433" maxy="8.633024"/>
             */
            if (bounds[0] > 47.310593 && bounds[1] > 8.427483 && bounds[2] < 47.447433 && bounds[3] < 8.633024) {
                // we are inside the bounds -> download layer
                String wmsUrl = sharedPreference.getValue("base_map_url") +
                        Double.toString(bounds[0]) + "," + Double.toString(bounds[1]) + "," + Double.toString(bounds[2]) + ","
                        + Double.toString(bounds[3]) + "&WIDTH=" + dimensions[0] + "&HEIGHT=" + dimensions[1] + "&Layers=Uebersichtsplan";

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
                        .positionFromBounds(this.mapBounds));
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
            // Iterate through the KML file to find its extend (bounds)
            KmlContainer container = kmlLayer.getContainers().iterator().next();
            //Retrieve the first placemark in the nested container
            KmlPlacemark placemark = container.getPlacemarks().iterator().next();
            //Retrieve a polygon object in a placemark
            KmlLineString lineString = (KmlLineString) placemark.getGeometry();
            //Create LatLngBounds of the outer coordinates of the polygon
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : lineString.getGeometryObject()) {
                builder.include(latLng);
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 2));

            // add the layer to the map
            try {
                kmlLayer.addLayerToMap();
            } catch (IOException e) {
                Log.e("KMLLoader:AddLayerIO" , e.getMessage());
            } catch(XmlPullParserException e) {
                Log.e("KMLLoader:AddLayerXML" , e.getMessage());
            }
        }
    }
}