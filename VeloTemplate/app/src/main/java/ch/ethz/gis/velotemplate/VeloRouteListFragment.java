package ch.ethz.gis.velotemplate;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.helper.VeloRouteAdapter;
import ch.ethz.gis.maps.RoutePreview;

public class VeloRouteListFragment extends ListActivity {

    public final static String ID_EXTRA = "ch.ethz.gis.VeloTemplate._ID";
    public VeloRouteAdapter veloAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_velo_home);

        veloAdapter= new VeloRouteAdapter(this, getDataForListView());
        setListAdapter(veloAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        Intent routeInfo = new Intent(VeloRouteListFragment.this, RoutePreview.class);
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

}
