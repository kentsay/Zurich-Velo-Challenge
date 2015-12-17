package ch.ethz.gis.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutTeamFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.about_team, container, false);
        TextView issue   = (TextView) rootView.findViewById(R.id.issue);
        TextView contact = (TextView) rootView.findViewById(R.id.contact);
        issue.setText("Ken Tsay (tsayk@student.ethz.ch)\n" +
                      "Shuang Wu (wus@student.ethz.ch)\n" +
                      "Jerome Leibacher (jeromel@student.ethz.ch)");

        contact.setText("The source code of this application is available under the Creative Commons (CC) license:\n" +
                        "https://github.com/kentsay/Zurich-Velo-Challenge");
        return rootView;
    }
}
