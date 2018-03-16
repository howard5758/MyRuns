package com.example.ping_jungliu.myruns3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ping-Jung Liu on 2018/1/31.
 */

// display the details of a database entry
public class DisplayEntryActivity extends AppCompatActivity {

    long row_id;
    public MyDbHelper DbHelper;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_entry);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // get the row id from intent
        row_id = getIntent().getLongExtra("row_id", -1);
        DbHelper = new MyDbHelper(this);
        // get the correct entry from database
        DbEntry entry = DbHelper.fetchEntryByIndex(row_id);

        // update all the EditTexts
        EditText input = (EditText) findViewById(R.id.inputt);
        int input_int = entry.getInputType();
        String input_str = "";
        switch (input_int){
            case 0:
                input_str = "Manual Input";
                break;
            case 1:
                input_str = "GPS";
                break;
            case 2:
                input_str = "Automatic";
                break;
        }
        input.setText(input_str);

        EditText activity = (EditText) findViewById(R.id.activityy);
        activity.setText(entry.getActivityType());

        EditText datetime = (EditText) findViewById(R.id.datetimee);
        datetime.setText(HistoryFragment.formatDateTime(entry.getDateTime()));

        EditText duration = (EditText) findViewById(R.id.durationn);
        duration.setText(HistoryFragment.goodDuration(entry.getDuration()));

        EditText distance = (EditText) findViewById(R.id.distancee);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString("unit_prefernece", "Kilometers");
        String distancee = HistoryFragment.goodDistance(entry.getDistance(),unitPref);
        distance.setText(distancee);

        EditText calories = (EditText) findViewById(R.id.caloriess);
        calories.setText(entry.getCalories()+" cals");

        EditText heartrate = (EditText) findViewById(R.id.heartratee);
        heartrate.setText(entry.getHeartRate()+" bpm");
    }

    // define the delete button in menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, 0, 0, "DELETE").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    // use a thread to delete a database entry
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Thread thread = new Thread(){
            public void run(){
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            DbHelper.removeEntry(row_id);
                            HistoryFragment.historyAdapter.notifyDataSetChanged();
                            mUserId = mFirebaseUser.getUid();

                            if (HistoryFragment.ids.contains(row_id)) {
                                Log.d("masterr", "well");
                                int idx = HistoryFragment.ids.indexOf(row_id);
                                mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(HistoryFragment.old_ids.get(idx))).removeValue();
                            }
                            else {
                                Log.d("masterr", "weird");
                                mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(row_id)).removeValue();
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("no", "no");
                }
            }
        };
        thread.start();
        finish();
        return true;
    }


}
