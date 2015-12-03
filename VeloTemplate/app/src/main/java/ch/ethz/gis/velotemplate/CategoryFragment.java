package ch.ethz.gis.velotemplate;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


public class CategoryFragment extends Fragment implements View.OnClickListener{
    private Bundle args;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_view, container, false);

        args = new Bundle();

        ImageButton easy = (ImageButton)rootView.findViewById(R.id.button1);
        ImageButton medium = (ImageButton)rootView.findViewById(R.id.button2);
        ImageButton hard = (ImageButton)rootView.findViewById(R.id.button3);
        ImageButton random = (ImageButton)rootView.findViewById(R.id.button4);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);
        random.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        ListFragment routeList = new VeloRouteListFragment();
        switch (v.getId()) {
            case R.id.button1:
                args.putInt("distMax", 10);
                args.putInt("elevMax", 20);
                break;
            case R.id.button2:
                args.putInt("elevMin", 30);
                break;
            case R.id.button3:
                args.putInt("distMin", 30);
                break;
            case R.id.button4:
                args.putBoolean("random", true);
                break;
            default:
                break;
        }
        routeList.setArguments(args);
        this.getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, routeList)
                .addToBackStack(null)
                .commit();
    }
}
