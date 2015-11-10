package ch.ethz.gis.velotemplate;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * Created by kentsay on 06/11/2015.
 */
public class FilterFragment extends Fragment {

    private SeekBar distanceFilter;
    private SeekBar altitudeFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.filter_view, container, false);

        distanceFilter = (SeekBar) getActivity().findViewById(R.id.distance);
        altitudeFilter = (SeekBar) getActivity().findViewById(R.id.altitude);

//        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int progressChanged = 0;
//
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
//                progressChanged = progress;
//            }
//
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // TODO Auto-generated method stub
//            }
//
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(getActivity(), "seek bar progress:" + progressChanged,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });

        return rootView;
    }

    private void filterAction() {
        Log.v("filter", "trigger button aciton");
    }
}
