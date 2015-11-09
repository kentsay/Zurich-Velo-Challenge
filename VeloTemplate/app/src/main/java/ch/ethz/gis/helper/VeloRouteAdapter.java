package ch.ethz.gis.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ch.ethz.gis.velotemplate.R;
import ch.ethz.gis.velotemplate.VeloRoute;

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
        TextView routeDistance = (TextView)convertView.findViewById(R.id.route_info);

        VeloRoute route = routeList.get(position);

        routeName.setText(route.getRoute_name());
        routeDistance.setText(route.getRoute_distance() + "km");

        return convertView;
    }

    public VeloRoute getVeloRoute(int position)
    {
        return routeList.get(position);
    }

}
