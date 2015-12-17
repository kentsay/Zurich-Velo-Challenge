package ch.ethz.gis.fragment;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class FilterFragment extends Fragment {

    private TextView distance;
    private TextView elevation;
    private SeekBar distanceFilter;
    private SeekBar elevationFilter;
    private Button findButton;
    private Bundle args;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.filter_view, container, false);
        initialize(rootView);

        distance.setText("Route distance: " + distanceFilter.getProgress() + " km");
        distanceFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int distance_settings = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance_settings = progress;
                distance.setText("Route distance: " + distance_settings + " km");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // set arguments for ListFragment to call DbHelper and change text
                args.putInt("distMax", distance_settings);
                distance.setText("Route distance: " + distance_settings + " km");
            }
        });

        elevation.setText("Route elevation: " + elevationFilter.getProgress() + " m");
        elevationFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int elevation_settings = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // set arguments for ListFragment to call DbHelper and change text
                elevation_settings = progress;
                elevation.setText("Route elevation: " + elevation_settings + " m");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                args.putInt("elevMax", elevation_settings);
                elevation.setText("Route elevation: " + elevation_settings + " m");
            }
        });

        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListFragment routeList = new VeloRouteListFragment();
                routeList.setArguments(args);
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, routeList)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return rootView;
    }

    //initialize elements in this fragment
    private void initialize(View view) {
        args = new Bundle();
        distance = (TextView) view.findViewById(R.id.text_distance);
        elevation = (TextView) view.findViewById(R.id.text_elevation);
        distanceFilter = (SeekBar) view.findViewById(R.id.distance);
        elevationFilter = (SeekBar) view.findViewById(R.id.elevation);
        args.putInt("distMax", distanceFilter.getProgress());
        args.putInt("elevMax", elevationFilter.getProgress());
        findButton = (Button) view.findViewById(R.id.find);
    }

}
