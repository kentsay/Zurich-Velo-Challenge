package ch.ethz.gis.velotemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by kentsay on 28/10/2015.
 */
public class RouteInfoActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.velo_detail_info_page);

        Intent i = getIntent();
        VeloRoute route        = (VeloRoute)i.getSerializableExtra(VeloHome.ID_EXTRA);
        TextView name          = (TextView) findViewById(R.id.route_info_name);
        TextView distance      = (TextView) findViewById(R.id.route_info_distance);
        TextView elevation     = (TextView) findViewById(R.id.route_info_elevation);
        TextView snapeshot_url = (TextView) findViewById(R.id.route_info_snapshot_url);
        TextView kml_url       = (TextView) findViewById(R.id.route_info_kml_url);

        name.setText("Name: " + route.getRoute_name());
        distance.setText("Distance: " + route.getRoute_distance());
        elevation.setText("Elevation: " + route.getElevation());
        snapeshot_url.setText("Snapshot url: " + route.getSnapshot_url());
        kml_url.setText("KML url: " + route.getKml_url());
    }
}
