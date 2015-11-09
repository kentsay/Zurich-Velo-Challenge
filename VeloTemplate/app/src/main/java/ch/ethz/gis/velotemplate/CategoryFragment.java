package ch.ethz.gis.velotemplate;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class CategoryFragment extends Fragment implements View.OnClickListener{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_view, container, false);

        Button easy = (Button)rootView.findViewById(R.id.button1);
        Button medium = (Button)rootView.findViewById(R.id.button2);
        Button hard = (Button)rootView.findViewById(R.id.button3);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                ListFragment routeList = new VeloRouteListFragment();
                this.getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, routeList)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.button2:
                //TODO
                break;
            case R.id.button3:
                //TODO
                break;
            default:
                break;
        }
    }
}
