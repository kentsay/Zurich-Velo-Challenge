package ch.ethz.gis.velotemplate;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.helper.VeloRouteAdapter;
import ch.ethz.gis.maps.RoutePreviewFragment;

public class VeloRouteListFragment extends ListFragment {

    public final static String ID_EXTRA = "ROUTE";
    private List<VeloRoute> routeList;
    private VeloDbHelper dbHelper;
    private VeloRouteAdapter veloAdapter;
    private Bundle args;
    private int distance;
    private int altitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
        // if args is not null means calling the fragment from filter, else it's from default
        if (args != null)
            veloAdapter= new VeloRouteAdapter(getActivity(), getVeloRoutes(distance, altitude));
        else
            veloAdapter= new VeloRouteAdapter(getActivity(), getAllVeloRoutes());
        setListAdapter(veloAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        Intent routeInfo = new Intent(getActivity(), RoutePreviewFragment.class);
        routeInfo.putExtra(ID_EXTRA, route);
        startActivity(routeInfo);
    }

    private List<VeloRoute> getAllVeloRoutes()
    {
        routeList = new ArrayList<VeloRoute>();
        routeList = dbHelper.getVeloRoutes();
        return routeList;
    }

    private List<VeloRoute> getVeloRoutes(int distance, int altitude)
    {
        routeList = new ArrayList<VeloRoute>();
        routeList = dbHelper.getVeloRoutesByArgument(distance, altitude);
        return routeList;
    }

    private void initialize() {
        args = this.getArguments();
        if (args != null) {
            if (args.containsKey("distance"))
                distance = args.getInt("distance", 1000);
            if (args.containsKey("altitude"))
                altitude = args.getInt("altitude", 1000);
        }
        dbHelper = VeloDbHelper.getInstance(getActivity());
    }

}
