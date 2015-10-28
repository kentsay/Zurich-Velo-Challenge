package ch.ethz.gis.velotemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by kentsay on 23/10/2015.
 */
public class VeloStartPage extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.velo_start_page);

        Button easy = (Button)findViewById(R.id.button1);
        Button medium = (Button)findViewById(R.id.button2);
        Button hard = (Button)findViewById(R.id.button3);

        easy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent simple = new Intent(VeloStartPage.this,VeloHome.class);
                startActivity(simple);
            }
        });

        medium.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent base = new Intent(VeloStartPage.this,VeloHome.class);
                startActivity(base);
            }
        });

        hard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent base = new Intent(VeloStartPage.this,VeloHome.class);
                startActivity(base);
            }
        });
    }

}