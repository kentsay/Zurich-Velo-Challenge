package ch.ethz.jeromel.wmsmaptest;

/**
 * Created by Jerome on 16.10.15.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class WMSLoader {

    public static Bitmap loadMap(String urlString) {

        URL url = null;

        try {

            url = new URL(urlString);


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
}


