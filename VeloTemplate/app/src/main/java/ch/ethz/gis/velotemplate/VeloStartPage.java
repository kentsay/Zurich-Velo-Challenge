package ch.ethz.gis.velotemplate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_general, menu);
        return true;
    }


}
