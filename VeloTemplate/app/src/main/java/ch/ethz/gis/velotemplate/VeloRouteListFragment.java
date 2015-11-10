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
    public VeloRouteAdapter veloAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        veloAdapter= new VeloRouteAdapter(getActivity(), getDataForListView());
        setListAdapter(veloAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        VeloRoute route = veloAdapter.getVeloRoute(position);
        Intent routeInfo = new Intent(getActivity(), RoutePreviewFragment.class);
        routeInfo.putExtra(ID_EXTRA, route);
        startActivity(routeInfo);
    }

    public List<VeloRoute> getDataForListView()
    {
        List<VeloRoute> routeList = new ArrayList<VeloRoute>();
        VeloDbHelper dbHelper = VeloDbHelper.getInstance(getActivity());
        routeList = dbHelper.getVeloRoutes();
        return routeList;
    }

}
