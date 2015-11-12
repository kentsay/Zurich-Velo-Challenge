package ch.ethz.gis.velotemplate;

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
    private TextView altitude;
    private SeekBar distanceFilter;
    private SeekBar altitudeFilter;
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
                args.putInt("distance", distance_settings);
                distance.setText("Route distance: " + distance_settings + " km");
            }
        });

        altitude.setText("Altitude diff: " + altitudeFilter.getProgress() + " hm");
        altitudeFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int altitude_settings = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // set arguments for ListFragment to call DbHelper and change text
                altitude_settings = progress;
                altitude.setText("Altitude diff: " + altitude_settings + " hm");
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                args.putInt("altitude", altitude_settings);
                altitude.setText("Altitude diff: " + altitude_settings + " hm");
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
        altitude = (TextView) view.findViewById(R.id.text_altitude);
        distanceFilter = (SeekBar) view.findViewById(R.id.distance);
        altitudeFilter = (SeekBar) view.findViewById(R.id.altitude);
        args.putInt("distance", distanceFilter.getProgress());
        args.putInt("altitude", altitudeFilter.getProgress());
        findButton = (Button) view.findViewById(R.id.find);
    }

}
