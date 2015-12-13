package ch.ethz.gis.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import ch.ethz.gis.velotemplate.R;

/**
 * Created by hoisee on 12/11/15.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static String TAG = "Geofence";
    private Handler handler;

    public GeofenceTransitionsIntentService(){
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        final GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Location services error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Send notification and log the transition details.
            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String instruction = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
                        Toast.makeText(getApplicationContext(), instruction, Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Exit a geofence", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
