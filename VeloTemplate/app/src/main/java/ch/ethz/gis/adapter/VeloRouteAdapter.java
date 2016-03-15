package ch.ethz.gis.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import ch.ethz.gis.helper.VolleyHelper;
import ch.ethz.gis.R;
import ch.ethz.gis.databean.VeloRoute;

public class VeloRouteAdapter extends BaseAdapter {

    private final Context context;
    private final List<VeloRoute> routeList;

    public VeloRouteAdapter(Context context, List<VeloRoute> routeList) {
        this.context = context;
        this.routeList = routeList;
    }

    @Override
    public int getCount() {
        return routeList.size();
    }

    @Override
    public Object getItem(int position) {
        return routeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return routeList.indexOf(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.route_item, parent,false);
        }

        TextView routeName = (TextView)convertView.findViewById(R.id.route_name);
        TextView routeInfo = (TextView)convertView.findViewById(R.id.route_info);
        TextView routeDistance  = (TextView)convertView.findViewById(R.id.route_distance);
        ImageView routeSnapshot = (ImageView)convertView.findViewById(R.id.route_map);

        VeloRoute route = routeList.get(position);

        routeName.setText(route.getRoute_name());
        // TODO: Replace the special symbols with other icons
        String distance = route.getRoute_distance() + " km";
        SpannableString ss1=  new SpannableString(distance);
        ss1.setSpan(new RelativeSizeSpan(2f), 0, ss1.length() - 3, 0); // set size
        routeDistance.setText(ss1);

        routeInfo.setText("⇔︎ " + route.getRoute_distance() + " km\n");
        // Display elevation only when the value is valid
        if(!route.getElevation().equals("-1"))
            routeInfo.append("⇡︎ " + route.getElevation() + " m");
        showSnapshot(route.getSnapshot_url(), routeSnapshot);

        return convertView;
    }

    public VeloRoute getVeloRoute(int position)
    {
        return routeList.get(position);
    }

    private void showSnapshot(String url, final ImageView view){
        // Retrieves snapshots via Volley
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        view.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", error.getMessage());
                        view.setImageResource(R.drawable.logo_64);
                    }
                });
        VolleyHelper.getInstance(this.context).addToRequestQueue(request);
    }
}
