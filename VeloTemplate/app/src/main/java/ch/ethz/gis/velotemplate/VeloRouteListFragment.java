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
    private int distMin;
    private int distMax;
    private int elevMin;
    private int elevMax;
    private boolean random;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
        // if args is not null means calling the fragment from filter, else it's from default
        if (args != null) {
            if (random)
                veloAdapter = new VeloRouteAdapter(getActivity(), getRandomVeloRoutes());
            else
                veloAdapter = new VeloRouteAdapter(getActivity(), getVeloRoutes(distMin, elevMin, distMax, elevMax));
        }
        else
            veloAdapter = new VeloRouteAdapter(getActivity(), getAllVeloRoutes());
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

    private List<VeloRoute> getRandomVeloRoutes()
    {
        routeList = new ArrayList<VeloRoute>();
        routeList = dbHelper.getRandomVeloRoutes();
        return routeList;
    }

    private List<VeloRoute> getVeloRoutes(int distMin, int elevMin, int distMax, int elevMax)
    {
        routeList = new ArrayList<VeloRoute>();
        routeList = dbHelper.getVeloRoutesByArgument(distMin, elevMin, distMax, elevMax);
        return routeList;
    }

    private void initialize() {
        args = this.getArguments();
        if (args != null) {
                distMin = args.getInt("distMin", 0);
                elevMin = args.getInt("elevMin", 0);
                distMax = args.getInt("distMax", 1000);
                elevMax = args.getInt("elevMax", 1000);
                random = args.getBoolean("random", false);
        }
        dbHelper = VeloDbHelper.getInstance(getActivity());
    }

}
