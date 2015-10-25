package ch.ethz.jeromel.wmsmaptest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.view.Display;
import android.view.View;
import android.view.ViewGroupOverlay;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;


import java.util.List;

public class ShowMap extends AppCompatActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private MapView mapView;
    private static Projection projection;
    private static LatLngBounds mapBounds;
    private static int [] dimensions = new int[2];
    private Canvas canvas = new Canvas();
    private static Bitmap image;
    private LatLng  poslatlong;
    private WMSLoader wmsClient = new WMSLoader();

    private static Display display;

    // getter methods:
    public static LatLngBounds getBounds() {return mapBounds;}

    public static int[] getDimensions () {return dimensions;}

    public static GoogleMap getmMap() {return mMap;}

    public static  Display getDisplay() { return display; }

    public static Projection getProjection(){ return projection;}


    // setter methods
    public static void setImage(Bitmap nImage) {image = nImage;}
    public static void setmMap(GoogleMap nMap) {mMap = nMap;}
    public static void setProjection(Projection nProjection) {projection = nProjection;}
    public static void setMapBounds(LatLngBounds nMapBounds) {mapBounds = nMapBounds;}

    // code:


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        display = getWindowManager().getDefaultDisplay();

        // define the necessary size of the map and create the corresponding URL
        Point size = new Point();
        display.getSize(size);
        dimensions[0] = size.x; //width
        dimensions[1] = size.y; //height

        // get the position from the TextView from before
        double[] position = MainActivity.getPos();
        poslatlong = new LatLng(position[0],position[1]);


        // Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting GoogleMap object from the fragment
        fm.getMapAsync(this);

        //mMap.setMyLocationEnabled(true);


        // execute the thread to load the Google Map (which executes the load of the WMS, if this
        // thread is done -> onPostExecute)
        //loadGoogleMaps loadGoogleMapsThread = new loadGoogleMaps();
        //loadGoogleMapsThread.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);


                //"bbox=676000,241000,690000,255000&WIDTH=800&HEIGHT=800&Layers=Uebersichtsplan_2014";


    }

    @Override
    public void onMapReady(GoogleMap nMap) {
        setmMap(nMap);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poslatlong, 12));
        loadBasemap loadBasemapThread = new loadBasemap();
        loadBasemapThread.execute();
    }





    // This is done, when the botton is clicked
    public void doTheLoad(View view) {

        // when the basemap of Google Maps is loaded, we load the data from opendata zurich and
        // display them as an overlay
        //image = loadBasemap.execute();
        //Paint semitransparent = new Paint();
        //semitransparent.setAlpha(0x888);
        //canvas.drawBitmap(image, 0, 0, semitransparent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class loadBasemap extends AsyncTask<String, Void, Bitmap>{


        @Override
        protected Bitmap doInBackground(String... urls) {
            // get all the necessary objects:
            GoogleMap mMap = getmMap();
            Projection projection = mMap.getProjection();
            int[] dimensions = getDimensions();
            Display display = getDisplay();


            // Coordinate system of the display: origin upper-left, x right (width), y down (height)
            LatLng ur = projection.fromScreenLocation(new Point(dimensions[0],0)); //northeast
            LatLng ll = projection.fromScreenLocation(new Point(0,dimensions[1])); //southwest
            // set the new mapBounds
            ShowMap.setMapBounds(new LatLngBounds(ll,ur)); // southwest, northwest

            // resolve the LatLngBounds to doubles
            double[] bounds = new double[4];
            bounds[0] = ll.latitude;
            bounds[1] = ll.longitude;
            bounds[2] = ur.latitude;
            bounds[3] = ur.longitude;

            String wmsUrl = "http://www.gis.stadt-zuerich.ch/maps/services/wms/WMS-ZH-STZH-OGD/MapServer/WMSServer?" +
                    "VERSION=1.3.0&REQUEST=GetMap&CRS=EPSG:4326&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&BBOX=" +
                    Double.toString(bounds[0]) + "," + Double.toString(bounds[1]) + "," + Double.toString(bounds[2]) + ","
                    + Double.toString(bounds[3]) + "&WIDTH=" + dimensions[0] + "&HEIGHT=" + dimensions[1] + "&Layers=Uebersichtsplan";

            Log.d("WMSLoader:WMS URL", wmsUrl);
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
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            // get the current bounds of the view
            LatLngBounds mapBounds = getBounds();
            Log.d("mapBounds:", Double.toString(mapBounds.northeast.latitude));
            GroundOverlayOptions overlayOption = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .positionFromBounds(mapBounds);
            GoogleMap mMap = getmMap();
            mMap.addGroundOverlay(overlayOption);
        }
    }

}

