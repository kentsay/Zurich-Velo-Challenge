package ch.ethz.gis.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.gis.databean.VeloRoute;
import ch.ethz.gis.helper.VeloDbHelper;
import ch.ethz.gis.adapter.VeloRouteAdapter;
import ch.ethz.gis.maps.RoutePreviewFragment;

public class VeloRouteListFragment extends ListFragment {

    public final static String ID_EXTRA = "ROUTE";
    private List<VeloRoute> routeList;
    private VeloDbHelper dbHelper;
    private VeloRouteAdapter veloAdapter;
    private Bundle args;
    private Context context;
    private int distMin;
    private int distMax;
    private int elevMin;
    private int elevMax;
    private boolean random;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
        context = getActivity().getApplicationContext();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_velo_home, null);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setDivider(null);
        getListView().setDividerHeight(0);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        Intent routeInfo = new Intent(getActivity(), RoutePreviewFragment.class);
        routeInfo.putExtra(ID_EXTRA, route);

        // check if internet is available, if yes -> continue
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null) {

            Log.d("NetworkState",cm.getActiveNetworkInfo().toString());
            startActivity(routeInfo);
        } else {
            Toast.makeText(context, "No Internet connection! \n Please connect to the internet", Toast.LENGTH_SHORT).show();

        }



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
