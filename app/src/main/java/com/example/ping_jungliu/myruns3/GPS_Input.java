package com.example.ping_jungliu.myruns3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Ping-Jung Liu on 2018/1/13.
 */

public class GPS_Input extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_input);
    }

    // buttons
    public void onClick_save(View view){
        finish();
    }
    public void onClick_cancel(View view) {
        Toast toast = Toast
                .makeText(this, "Entry discarded.", Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }
}
