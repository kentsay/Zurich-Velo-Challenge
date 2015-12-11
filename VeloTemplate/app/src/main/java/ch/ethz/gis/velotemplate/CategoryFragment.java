package ch.ethz.gis.velotemplate;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


public class CategoryFragment extends Fragment implements View.OnClickListener{
    private Bundle args;
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.category_view, container, false);
        context = getActivity().getApplicationContext();
        args = new Bundle();

        ImageButton easy = (ImageButton)rootView.findViewById(R.id.button1);
        ImageButton medium = (ImageButton)rootView.findViewById(R.id.button2);
        ImageButton hard = (ImageButton)rootView.findViewById(R.id.button3);
        ImageButton random = (ImageButton)rootView.findViewById(R.id.button4);
        ImageButton favorite = (ImageButton)rootView.findViewById(R.id.button5);
        ImageButton rental = (ImageButton)rootView.findViewById(R.id.button6);

        easy.setOnClickListener(this);
        medium.setOnClickListener(this);
        hard.setOnClickListener(this);
        random.setOnClickListener(this);
        favorite.setOnClickListener(this);
        rental.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        Fragment fragment = null;
        switch (v.getId()) {
            case R.id.button1:
                fragment = new VeloRouteListFragment();
                args.putInt("distMax", 10);
                args.putInt("elevMax", 20);
                fragment.setArguments(args);
                break;
            case R.id.button2:
                fragment = new VeloRouteListFragment();
                args.putInt("elevMin", 30);
                fragment.setArguments(args);
                break;
            case R.id.button3:
                fragment = new VeloRouteListFragment();
                args.putInt("distMin", 30);
                fragment.setArguments(args);
                break;
            case R.id.button4:
                fragment = new VeloRouteListFragment();
                args.putBoolean("random", true);
                fragment.setArguments(args);
                break;
            case R.id.button5:
                fragment = new FavouriteFragment();
                break;
            case R.id.button6:
                // check if internet is available, if yes -> continue
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (cm.getActiveNetworkInfo() != null) {
                    fragment = new NearbyFragment();
                    Log.d("NetworkState", cm.getActiveNetworkInfo().toString());
                } else {
                    Toast.makeText(context, "No Internet connection! \n Please connect to the internet", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                break;
        }
        if(fragment != null) {
            this.getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
