package ch.ethz.gis.velotemplate;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import ch.ethz.gis.adapter.VeloDirectionAdapter;
import ch.ethz.gis.databean.VeloDirection;
import ch.ethz.gis.maps.RoutePreviewFragment;

public class VeloDirectionListFragment extends ListFragment {

    public final static String ID_EXTRA = "DIRECTION";
    private List<VeloDirection> directionList;
    private VeloDirectionAdapter veloDirectionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
        //veloDirectionAdapter = new VeloRouteAdapter(getActivity(), getAllVeloRoutes());
        setListAdapter(veloDirectionAdapter);
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
        //TODO: detail of direction
//        VeloDirection route = veloDirectionAdapter.getVeloRoute(position);
//        Intent routeInfo = new Intent(getActivity(), RoutePreviewFragment.class);
//        routeInfo.putExtra(ID_EXTRA, route);
//        startActivity(routeInfo);
    }

    private void initialize() {

    }

}
