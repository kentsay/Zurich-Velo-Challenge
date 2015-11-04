package ch.ethz.gis.velotemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import ch.ethz.gis.helper.VeloDbHelper;

/**
 * Created by kentsay on 28/10/2015.
 */
public class RouteInfoActivity extends Activity {

    private VeloDbHelper dbHelper = VeloDbHelper.getInstance(this);
    private VeloRoute route;
    private MenuItem fav;
    private MenuItem unfav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.velo_detail_info_page);

        Intent i = getIntent();
        route        = (VeloRoute)i.getSerializableExtra(VeloHome.ID_EXTRA);
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
        if (dbHelper.checkFavoriteRouteExists(route.getId())) {
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
                dbHelper.addFavoriteRoute(route);
                return true;
            case R.id.action_bar_favorite_remove:
                //reset action bar icon to show fav or unfav
                unfav.setVisible(false);
                fav.setVisible(true);
                dbHelper.deleteFromFavorite(route.getId());
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
