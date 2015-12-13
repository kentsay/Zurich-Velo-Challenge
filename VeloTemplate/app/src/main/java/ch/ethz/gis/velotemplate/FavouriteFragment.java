package ch.ethz.gis.velotemplate;

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

public class FavouriteFragment extends ListFragment {

    public final static String ID_EXTRA = "ROUTE";
    public VeloRouteAdapter veloAdapter;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRouteListAdapter();
        context = getActivity().getApplicationContext();
    }

    // retrieval favourite route when resume
    @Override
    public void onResume() {
        super.onResume();
        setRouteListAdapter();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        Intent routeInfo = new Intent(getActivity(), RoutePreviewFragment.class);
        routeInfo.putExtra(ID_EXTRA, route);
        // check if internet is available, if yes -> continue
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() != null) {

            Log.d("NetworkState", cm.getActiveNetworkInfo().toString());
            startActivity(routeInfo);
        } else {
            Toast.makeText(context, "No Internet connection! \n Please connect to the internet", Toast.LENGTH_SHORT).show();
        }
    }

    private List<VeloRoute> getDataForFavouriteList()
    {
        List<VeloRoute> routeList = new ArrayList<VeloRoute>();
        VeloDbHelper dbHelper = VeloDbHelper.getInstance(getActivity());
        routeList = dbHelper.getAllFavouriteRoutes();
        return routeList;
    }

    // get favourite route and set list adapter
    private void setRouteListAdapter() {
        veloAdapter= new VeloRouteAdapter(getActivity(), getDataForFavouriteList());
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
}
