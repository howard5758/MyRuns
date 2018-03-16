package com.example.ping_jungliu.myruns3;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ping-Jung Liu on 2018/1/12.
 */

public class HistoryFragment extends ListFragment {

    public static MyDbHelper DbHelper;
    public static ArrayList<DbEntry> entries;
    public static HistoryAdapter historyAdapter;

    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    DatabaseReference mDatabase;
    String mUserId;

    DbEntry entry;

    public static ArrayList<Long> ids;
    public static ArrayList<Long> old_ids;

    int login;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        login = LogInActivity.login;

        ids = new ArrayList<Long>();
        old_ids = new ArrayList<Long>();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if(savedInstanceState != null){
//            login = savedInstanceState.getInt("login");
            Log.d("helooooooo", "mehhh");
            login = 0;
            ids = (ArrayList<Long>) savedInstanceState.getSerializable("ids");
            old_ids = (ArrayList<Long>) savedInstanceState.getSerializable("old_ids");
        }
        else {
            SharedPreferences iddd = getActivity().getSharedPreferences("ids", 0);
            int size = iddd.getInt("size", 0);

            for (int i = 0;i<size;i++){
                ids.add(iddd.getLong(String.valueOf(i) + "ids", 0));
                old_ids.add(iddd.getLong(String.valueOf(i) + "old_ids", 0));
            }
        }


        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        }
        else{
            StartFragment.addListenerToAll();
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the image capture uri before the activity goes into background
//        outState.putInt("login", login);
        outState.putSerializable("ids", ids);
        outState.putSerializable("old_ids", old_ids);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedIntanceState){

        DbHelper = new MyDbHelper(getActivity());
        // get the whole table!
        entries = DbHelper.fetchEntries();

        // set up adapter
        historyAdapter = new HistoryAdapter(getActivity(), R.layout.list_history, entries);
        setListAdapter(historyAdapter);


        Log.d("masterr", String.valueOf(login));

        // if user login then get to main activity, get the data from firebase because they have been wiped
        if(login == 1){
            Log.d("masterr", "wut");
            mUserId = mFirebaseUser.getUid();
            mDatabase.child("users").child(mUserId).child("runs").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("masterr", "work!");
                    if (dataSnapshot.hasChildren()) {
                        entries = new ArrayList<DbEntry>();
                        for(DataSnapshot child:dataSnapshot.getChildren()){
                            entry = new DbEntry();
                            Log.d("masterr", String.valueOf(child.child("activityType").getValue()));
                            entry.setActivityType(String.valueOf(child.child("activityType").getValue()));
                            entry.setAvgSpeed(Double.parseDouble(String.valueOf(child.child("avgSpeed").getValue())));
                            entry.setCalories(Double.parseDouble(String.valueOf(child.child("calorie").getValue())));
                            entry.setClimb(Double.parseDouble(String.valueOf(child.child("climb").getValue())));
                            entry.setComment(String.valueOf(child.child("comment").getValue()));
                            entry.setDateTime(Long.parseLong(String.valueOf(child.child("dateTimeInMillis").getValue())));
                            entry.setDistance(Double.parseDouble(String.valueOf(child.child("distance").getValue())));
                            entry.setDuration(Double.parseDouble(String.valueOf(child.child("duration").getValue())));
                            entry.setHeartRate(Double.parseDouble(String.valueOf(child.child("heartrate").getValue())));
                            entry.setId(Long.parseLong(String.valueOf(child.child("id").getValue())));
                            int inputType = Integer.parseInt(String.valueOf(child.child("inputType").getValue()));
                            entry.setInputType(inputType);

                            ArrayList<LatLng> latLngList;
                            if(inputType != 0){
                                ArrayList<LatLng> locList = new ArrayList<LatLng>();
                                entry.setLocationList(locList);
                                for(DataSnapshot location:child.child("locationLatLngList").getChildren()){
                                    double latitude = Double.parseDouble(String.valueOf(location.child("latitude").getValue()));
                                    double longitude = Double.parseDouble(String.valueOf(location.child("longitude").getValue()));
                                    LatLng point =new LatLng(latitude, longitude);
                                    entry.addLocationList(point);
                                }
                            }
                            entries.add(entry);
                        }
                        new writeDB().execute();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDatabase.child("users").child(mUserId).child("runs").child("-100").setValue("temp");
            mDatabase.child("users").child(mUserId).child("runs").child("-100").removeValue();
        }




        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        super.onListItemClick(parent, v, position, id);

        // find the correct row id
        TextView tv = (TextView) v.findViewById(R.id.row_id);
        long rowid = Long.parseLong(tv.getText().toString());

        // initialize intent
        Intent intent;
        intent = new Intent(getActivity(), DisplayEntryActivity.class);
        DbEntry entry = DbHelper.fetchEntryByIndex(rowid);
        if(entry.getInputType() == 1 || entry.getInputType() == 2) {
            intent = new Intent(getActivity(), MapsActivity.class);
        }
        // put row id as extra message
        intent.putExtra("is_history", true);
        intent.putExtra("row_id", rowid);
        getActivity().startActivity(intent);
    }

    public class HistoryAdapter extends ArrayAdapter<DbEntry>{

        LayoutInflater inflater;

        public HistoryAdapter(Context context, int textViewResourceId, ArrayList<DbEntry> objects) {
            super(context, textViewResourceId, objects);
        }

        public View getView(int position, View view, ViewGroup parent) {

            // make sure view is never null
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_history, parent, false);
            }

            // get the current entry through position
            DbEntry entry = getItem(position);
            int input = entry.getInputType();
            String input_type = "";
            switch(input){
                case 0:
                    input_type = "Manual Entry";
                    break;
                case 1:
                    input_type = "GPS";
                    break;
                case 2:
                    input_type = "Automatic";
                    break;
            }

            // set up the text views
            String activity_type = entry.getActivityType();
            String date_time = formatDateTime(entry.getDateTime());
            String temp = input_type + ": " + activity_type + ", " + date_time;
            TextView firstLine = (TextView) view.findViewById(R.id.first_line);
            firstLine.setText(temp);

            // second line
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitPref = pref.getString("unit_prefernece", "Kilometers");
            Log.d("hihi", unitPref);
            String distance = goodDistance(entry.getDistance(),unitPref);
            String duration = goodDuration(entry.getDuration());
            temp = distance + ", " + duration;
            TextView secondLine = (TextView) view.findViewById(R.id.second_line);
            secondLine.setText(temp);

            // this part cannot be seen
            TextView idd = (TextView) view.findViewById(R.id.row_id);
            idd.setText(entry.getId() + "");

            Log.d("huh?", "huh");
            Log.d("comeon", "buo");

            return view;
        }

        // update history to new data
        @Override
        public void notifyDataSetChanged(){
            if(DbHelper != null) {

                Log.d("norifydatachange", "buuuu");
                ArrayList<DbEntry> new_entries = DbHelper.fetchEntries();
                historyAdapter = new HistoryAdapter(getActivity(), R.layout.list_history, new_entries);
                setListAdapter(historyAdapter);
            }
            super.notifyDataSetChanged();
        }

    }

    // some helpers
    public static String formatDateTime(long dateTime) {
        Date date = new Date(dateTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");

        return timeFormat.format(date) + " " + dateFormat.format(date);
    }
    public static String goodDuration(double duration) {
        int minutes = (int)(duration/60);
        int seconds = (int)(duration%60);
        if (minutes == 0 && seconds == 0) return "0secs";
        String temp = String.valueOf(minutes) + "min " + String.valueOf(seconds) + "secs";
        return temp;
    }

    public static String goodDistance(double distance, String unitPref) {
        if (unitPref.equals("Miles")) {
            distance /= 1.61; // converts from km to miles
        }
        String temp = String.format("%.2f", distance) + " " + unitPref;
        return temp;
    }

    private class writeDB extends AsyncTask<ArrayList<DbEntry>, Integer, String> {

        protected String doInBackground(ArrayList<DbEntry>... exerciseEntries) {
            Log.d("masterr", "saveee?");
            for(DbEntry entry:entries){
                old_ids.add(entry.getId());
                long id = DbHelper.insertEntry(entry);
                ids.add(id);
            }
            long id = 0;
            return ""+id;
        }

        protected void onPostExecute(String result) {
            HistoryFragment.historyAdapter.notifyDataSetChanged();
            StartFragment.addListenerToAll();

            SharedPreferences iddd = getActivity().getSharedPreferences("ids", 0);
            SharedPreferences.Editor editor = iddd.edit();
            editor.clear();

            int size = ids.size();
            for(int i = 0; i < size; i++){
                editor.putLong(String.valueOf(i) + "ids", ids.get(i));
                editor.putLong(String.valueOf(i) + "old_ids", old_ids.get(i));
            }
            editor.putInt("size", size);
            editor.commit();
        }
    }

    private void loadLogInView() {
        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}