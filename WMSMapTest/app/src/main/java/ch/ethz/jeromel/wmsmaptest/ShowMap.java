package ch.ethz.jeromel.wmsmaptest;

import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

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




public class ShowMap extends AppCompatActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public LinkedList<GroundOverlay> allOverlays = new LinkedList<>();
    private static Projection projection;
    private static LatLngBounds mapBounds;
    private static int [] dimensions = new int[2];
    private LatLng  poslatlong;

    // getter methods:
    public static LatLngBounds getBounds() {return mapBounds;}
    public static int[] getDimensions () {return dimensions;}
    public static Projection getProjection(){ return projection;}

    // setter methods
    public static void setProjection(Projection nProjection) {projection = nProjection;}
    public static void setMapBounds(LatLngBounds nMapBounds) {mapBounds = nMapBounds;}

    // code:


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        Display display = getWindowManager().getDefaultDisplay();
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
        mMap = nMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poslatlong, 15));
        Projection  tempProjection = mMap.getProjection();
        setProjection(tempProjection);
        mMap.setOnCameraChangeListener(getCameraChangeListener());
        //Log.d("ID of mMap", String.valueOf(mMap.hashCode()));
        loadBasemap loadBasemapThread = new loadBasemap();
        loadBasemapThread.execute();
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition camPos){
                Projection newProjection = mMap.getProjection();
                // mMap.clear();
                loadBasemap loadBasemapThread = new loadBasemap();
                loadBasemapThread.execute();
                setProjection(newProjection);
            }
        };
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
            Projection projection = getProjection();
            int[] dimensions = getDimensions();

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
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            // get the current bounds of the view
            LatLngBounds mapBounds = getBounds();
            // create new overlay
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

