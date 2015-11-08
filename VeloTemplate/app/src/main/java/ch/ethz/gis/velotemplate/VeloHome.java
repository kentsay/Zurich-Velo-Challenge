package ch.ethz.gis.velotemplate;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.maps.RoutePreview;

public class VeloHome extends ListActivity {

    public final static String ID_EXTRA = "ch.ethz.gis.VeloTemplate._ID";
    public VeloRouteAdapter veloAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velo_home);

        veloAdapter= new VeloRouteAdapter();
        setListAdapter(veloAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        //Toast.makeText(VeloHome.this, route.route_name, Toast.LENGTH_SHORT).show();
        Intent routeInfo = new Intent(VeloHome.this, RoutePreview.class);
        routeInfo.putExtra(ID_EXTRA, route);
        startActivity(routeInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }

    public List<VeloRoute> getDataForListView()
    {
        List<VeloRoute> routeList = new ArrayList<VeloRoute>();
        VeloDbHelper dbHelper = VeloDbHelper.getInstance(this);
        routeList = dbHelper.getVeloRoutes();
        return routeList;
    }

    public class VeloRouteAdapter extends BaseAdapter {

        List<VeloRoute> routeList = getDataForListView();
        @Override
        public int getCount() {
            return routeList.size();
        }

        @Override
        public Object getItem(int position) {
            return routeList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return routeList.indexOf(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null)
            {
                LayoutInflater inflater = (LayoutInflater) VeloHome.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.route_item, parent,false);
            }

            TextView routeName = (TextView)convertView.findViewById(R.id.route_name);
            TextView routeDistance = (TextView)convertView.findViewById(R.id.route_info);
            ImageView routeSnapshot = (ImageView)convertView.findViewById(R.id.route_map);

            VeloRoute route = routeList.get(position);

            routeName.setText(route.route_name);
            routeDistance.setText(route.route_distance + "km");
            loadSnapshot loadSnapshotThread = new loadSnapshot(routeSnapshot);
            loadSnapshotThread.execute(route.snapshot_url);

            return convertView;
        }

        private VeloRoute getVeloRoute(int position)
        {
            return routeList.get(position);
        }

        private class loadSnapshot extends AsyncTask<String, Void, Bitmap> {
            ImageView bmImage;

            public loadSnapshot(ImageView bmImage) {
                this.bmImage = bmImage;
            }

            protected Bitmap doInBackground(String... urls) {
                String url = urls[0];
                Bitmap bitmap = null;
                try {
                    InputStream in = new URL(url).openStream();
                    bitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("loadSnapshot", e.getMessage());
                    e.printStackTrace();
                }
                return bitmap;
            }

            protected void onPostExecute(Bitmap result) {
                bmImage.setImageBitmap(result);
            }
        }
    }
}
