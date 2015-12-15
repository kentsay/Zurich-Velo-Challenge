package ch.ethz.gis.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ch.ethz.gis.databean.VeloDirection;
import ch.ethz.gis.velotemplate.R;

public class VeloDirectionAdapter extends BaseAdapter {

    private final Context context;
    private final List<VeloDirection> directionList;

    public VeloDirectionAdapter(Context context, List<VeloDirection> directionList) {
        this.context = context;
        this.directionList = directionList;
    }

    @Override
    public int getCount() {
        return directionList.size();
    }

    @Override
    public Object getItem(int position) {
        return directionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return directionList.indexOf(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.direction_item, parent,false);
        }

        TextView direction_info    = (TextView)convertView.findViewById(R.id.direction_info);
        TextView direction_summary = (TextView)convertView.findViewById(R.id.direction_summary);
        //ImageView direction_icon   = (ImageView)convertView.findViewById(R.id.direction_indicator);

        VeloDirection direction = directionList.get(position);
        switch (direction.getText()) {
            case "Start at Location 1":
                direction_info.setText("Your Location");
                direction_summary.setText("");
                break;
            default:
                direction_summary.setText(direction.getLength() + " m (" + direction.getTime() + " min)");
                direction_info.setText(direction.getText());
        }
        if (direction.getText().substring(0, 6) == "Finish") {
            direction_info.setText("Your Destination");
            direction_summary.setText("");
        }

        return convertView;
    }

    public VeloDirection getVeloDirection(int position) {
        return directionList.get(position);
    }
}
