package ch.ethz.jeromel.wmsmaptest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private static double[] position = new double[2]; //position (Latitude, Longitude)

    public static double[] getPos () {
        // returns the position entered
        return position;
    }


    // This method gets started if the button ShowMap is clicked
    public void ProcessAndShowMap(View view) {
        // extract the entered values from the TextViews
        TextView lat = (TextView)findViewById(R.id.StartActivity_lat_value);
        position[0] = Double.parseDouble(lat.getText().toString());
        TextView lon = (TextView)findViewById(R.id.StartActivity_long_value);
        position[1] = Double.parseDouble(lon.getText().toString());

        // start the next Activity
        Intent intent = new Intent(this,ShowMap.class);
        startActivity(intent);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // temporary setting the coordinates to Zurich
        TextView lat = (TextView)findViewById(R.id.StartActivity_lat_value);
        TextView lon = (TextView)findViewById(R.id.StartActivity_long_value);

        // set the position to Zurich City
        lat.setText("47.375806");;
        lon.setText("8.528130");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
