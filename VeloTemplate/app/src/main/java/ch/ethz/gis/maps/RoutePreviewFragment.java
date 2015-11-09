package ch.ethz.gis.maps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlLineString;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.velotemplate.R;
import ch.ethz.gis.velotemplate.VeloRouteListFragment;
import ch.ethz.gis.velotemplate.VeloRoute;


public class RoutePreviewFragment extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public LinkedList<GroundOverlay> allOverlays = new LinkedList<>();
    private static Projection projection;
    private static LatLngBounds mapBounds;
    private static int [] dimensions = new int[2];
    //init location for centre Zurich
    private LatLng  poslatlong = new LatLng(47.375806, 8.528130);
    private String kmlUrl = "";

    public static LatLngBounds getBounds() {return mapBounds;}
    public static int[] getDimensions () {return dimensions;}
    public static Projection getProjection(){ return projection;}

    public static void setProjection(Projection nProjection) {projection = nProjection;}
    public static void setMapBounds(LatLngBounds nMapBounds) {mapBounds = nMapBounds;}

    private VeloDbHelper dbHelper = VeloDbHelper.getInstance(this);
    private VeloRoute route;
    private MenuItem fav;
    private MenuItem unfav;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        context = getApplicationContext();
        Intent i = getIntent();

        // read the route data from previous activity
        route        = (VeloRoute)i.getSerializableExtra(VeloRouteListFragment.ID_EXTRA);
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
    }

    @Override
    public void onMapReady(GoogleMap nMap) {
        mMap = nMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poslatlong, 8));
        Projection  tempProjection = mMap.getProjection();
        setProjection(tempProjection);
        mMap.setOnCameraChangeListener(getCameraChangeListener());
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
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public class loadBasemap extends AsyncTask<String, Void, Bitmap>{
        // create mapBounds for each thread
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

            // check if the wanted layer is outside of the queryable layer -> download not needed
            // layer bounding box of opendata Zurich:
            // http://www.gis.stadt-zuerich.ch/maps/services/wms/WMS-ZH-STZH-OGD/MapServer/WMSServer?SERVICE=WMS&Request=GetCapabilities
            // <BoundingBox CRS="EPSG:4326" minx="47.310593" miny="8.427483" maxx="47.447433" maxy="8.633024"/>
            if (bounds[0] > 47.310593 && bounds[1] > 8.427483 && bounds[2] < 47.447433 && bounds[3] < 8.633024) {
                // we are inside the bounds -> download layer
                String wmsUrl = "http://www.gis.stadt-zuerich.ch/maps/services/wms/WMS-ZH-STZH-OGD/MapServer/WMSServer?" +
                        "VERSION=1.3.0&REQUEST=GetMap&CRS=EPSG:4326&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=TRUE&BBOX=" +
                        Double.toString(bounds[0]) + "," + Double.toString(bounds[1]) + "," + Double.toString(bounds[2]) + ","
                        + Double.toString(bounds[3]) + "&WIDTH=" + dimensions[0] + "&HEIGHT=" + dimensions[1] + "&Layers=Uebersichtsplan";

                Log.d("WMS URL", wmsUrl);
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
                    Log.wtf("WMSLoader:Stream", "****************** Error in WMSLoader: " + e.getMessage());
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
                // bitmap is ready!
                // create new overlay
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

            //TODO: fix the looper problem
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
                Log.v("Debug", "##### starting to open stream");
                input = url.openStream();
                Log.v("Debug", "##### after open stream");
                //TODO: HERE!!!
                kmlLayer = new KmlLayer(mMap,input,context);
            } catch (IOException e) {
                Log.e("KMLLoader:StreamIO", "****************** Error in KMLLoader: " + e.getMessage());
            } catch (XmlPullParserException e) {
                Log.e("KMSLoader:StreamXML", "Error setting the kml layer: " + e.getMessage());
            }

            return kmlLayer;
        }

        @Override
        protected void onPostExecute(KmlLayer kmlLayer) {
            Log.v("KML", "before it crash");
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