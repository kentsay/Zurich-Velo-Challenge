package ch.ethz.gis.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import ch.ethz.gis.R;
import ch.ethz.gis.adapter.VeloDirectionAdapter;
import ch.ethz.gis.helper.GeoUtil;

public class VeloDirectionListFragment extends ListFragment {

    public final static String ID_EXTRA = "DIRECTION";
    private VeloDirectionAdapter veloDirectionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initialize();
        veloDirectionAdapter = new VeloDirectionAdapter(getActivity(), GeoUtil.getVeloDirectionList());
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
    }

    private void initialize() {

    }

}