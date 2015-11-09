package ch.ethz.gis.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
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
        TextView routeInfo = (TextView)convertView.findViewById(R.id.route_info);
        ImageView routeSnapshot = (ImageView)convertView.findViewById(R.id.route_map);

        VeloRoute route = routeList.get(position);

        routeName.setText(route.getRoute_name());
        // TODO: Replace the special symbols with other icons
        routeInfo.setText("⇔︎ " + route.getRoute_distance() + " km\n");
        // Display elevation only when the value is valid
        if(!route.getElevation().equals("-1"))
            routeInfo.append("⇡︎ " + route.getElevation() + " m");
        loadSnapshot loadSnapshotThread = new loadSnapshot(routeSnapshot);
        loadSnapshotThread.execute(route.getSnapshot_url());

        return convertView;
    }

    public VeloRoute getVeloRoute(int position)
    {
        return routeList.get(position);
    }
    private class loadSnapshot extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public loadSnapshot(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("loadSnapshot", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
