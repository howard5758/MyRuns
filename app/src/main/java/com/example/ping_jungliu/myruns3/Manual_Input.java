package com.example.ping_jungliu.myruns3;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Ping-Jung Liu on 2018/1/13.
 */

public class Manual_Input extends ListActivity {

    public Calendar date_time;
    public DbEntry entry;
    public MyDbHelper DbHelper;
    public Intent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manual_input);
        date_time = Calendar.getInstance();
        intent = getIntent();
        String activity_type = intent.getStringExtra("activity_type");

        entry = new DbEntry();
        DbHelper = new MyDbHelper(this);
        entry.setInputType(0);
        entry.setActivityType(activity_type);
        entry.setDateTime(date_time.getTimeInMillis());
        entry.setDuration(0);
        entry.setDistance(0);
        entry.setCalories(0);
        entry.setHeartRate(0);
        entry.setComment("huh");

        if (savedInstanceState != null){
            entry = (DbEntry) savedInstanceState.getSerializable("entry");
        }

        String[] MANUAL_OPTIONS = new String[]{"Date", "Time", "Duration", "Distance",
                "Calories", "Heart Rate", "Comment"};
        ArrayAdapter<String> manualAdapter = new ArrayAdapter<String>(this, R.layout.list_manual,
                MANUAL_OPTIONS);
        setListAdapter(manualAdapter);
        ListView manual_list = getListView();
        OnItemClickListener itemListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (position == 0) {
                    DialogFragment date_input = MyAlertDialogFragment.newInstance(0);
                    date_input.show(getFragmentManager(), "");
                }
                else if (position == 1) {
                    DialogFragment time_input = MyAlertDialogFragment.newInstance(1);
                    time_input.show(getFragmentManager(), "");
                }
                else if (position == 2) {
                    DialogFragment duration_input = MyAlertDialogFragment.newInstance(2);
                    duration_input.show(getFragmentManager(), "Duration");
                }
                else if(position == 3) {
                    DialogFragment distance_input = MyAlertDialogFragment.newInstance(3);
                    distance_input.show(getFragmentManager(), "Distance");
                }
                else if(position == 4) {
                    DialogFragment calories_input = MyAlertDialogFragment.newInstance(4);
                    calories_input.show(getFragmentManager(), "Calories");
                }
                else if(position == 5) {
                    DialogFragment heartrate_input = MyAlertDialogFragment.newInstance(5);
                    heartrate_input.show(getFragmentManager(), "Heart Rate");
                }
                else if(position == 6) {
                    DialogFragment comment_input = MyAlertDialogFragment.newInstance(6);
                    comment_input.show(getFragmentManager(), "Heart Rate");
                }
            }
        };
        manual_list.setOnItemClickListener(itemListener);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("entry", entry);
    }

    public void onClick_save(View view){
        new writeDB().execute();
        finish();
    }
    public void onClick_cancel(View view){
        Toast toast = Toast
                .makeText(this, "Entry discarded.", Toast.LENGTH_SHORT);
        toast.show();
        finish();
    }

    private class writeDB extends AsyncTask<DbEntry, Integer, String> {

        protected String doInBackground(DbEntry... exerciseEntries) {
            long id = DbHelper.insertEntry(entry);
            Log.d("save!", "yeah");
            return ""+id;
        }

        protected void onPostExecute(String result) {
            HistoryFragment.historyAdapter.notifyDataSetChanged();
            Log.d("mah", "he");
            Toast.makeText(getApplicationContext(), "Entry #"+result+" saved.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
