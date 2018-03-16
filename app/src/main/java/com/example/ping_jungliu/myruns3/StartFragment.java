package com.example.ping_jungliu.myruns3;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Ping-Jung Liu on 2018/1/12.
 */

public class StartFragment extends Fragment {

    private Spinner spinner_input, spinner_activity;

    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseUser mFirebaseUser;

    private static DatabaseReference mDatabase;
    private static String mUserId;

    static MyDbHelper helper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new MyDbHelper(getActivity());

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser == null) {
            // Not logged in, launch the Log In activity
            loadLogInView();
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedIntanceState){

        // initialize buttons and spinners
        View view = inflater.inflate(R.layout.start_fragment, container, false);
        Button button_start = (Button) view.findViewById(R.id.start);
        Button button_sync = (Button) view.findViewById(R.id.sync);
        spinner_input = (Spinner) view.findViewById(R.id.spinner_input);
        spinner_activity = (Spinner) view.findViewById(R.id.spinner_activity);



        // cases of button inputs
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg) {
                Intent intent;
                String input_type = spinner_input.getSelectedItem().toString();
                String activity_type = spinner_activity.getSelectedItem().toString();
                // GPS
                if(input_type.equals("GPS")){
//                    intent = new Intent(getActivity(), GPS_Input.class);
                    intent = new Intent(getActivity(), MapsActivity.class);
                }
                // Automatic
                else if(input_type.equals("Automatic")){
                    intent = new Intent(getActivity(), MapsActivity.class);
                }
                // Manual
                else{
                    intent = new Intent(getActivity(), Manual_Input.class);
                }
                intent.putExtra("input_type", input_type);
                intent.putExtra("activity_type", activity_type);
                intent.putExtra("is_history", false);
                Log.d("hey", activity_type);
                getActivity().startActivity(intent);
            }
        });

        // sync to firebase
        button_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg) {
                if (mFirebaseUser == null) {
                    // Not logged in, launch the Log In activity
                    loadLogInView();
                } else {
                    Log.d("master", "comeon");

                    mUserId = mFirebaseUser.getUid();
                    //helper = new MyDbHelper(getActivity());
                    ArrayList<DbEntry> entries = helper.fetchEntries();


                    for(DbEntry entry: entries){
                        final long id = entry.getId();

                        if (!HistoryFragment.ids.contains(id)) {

                            int inputType = entry.getInputType();
                            String activityType = entry.getActivityType();
                            double calories = entry.getCalories();
                            String comment = entry.getComment();
                            double distance = entry.getDistance();
                            double duration = entry.getDuration();
                            double heartrate = entry.getHeartRate();
                            long datetime = entry.getDateTime();

                            double avgSpeed;
                            double climb;
                            double curSpeed;
                            ArrayList<LatLng> LocationList;

                            if (inputType == 0) {
                                avgSpeed = -1;
                                climb = -1;
                                curSpeed = -1;
                                LocationList = null;
                            } else {
                                avgSpeed = entry.getAvgSpeed();
                                climb = entry.getClimb();
                                curSpeed = MapsActivity.speedd;
                                LocationList = entry.getLocationList();
                            }


                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("activityType").setValue(activityType);
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("avgSpeed").setValue(String.valueOf(avgSpeed));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("calorie").setValue(String.valueOf(calories));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("climb").setValue(String.valueOf(climb));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("comment").setValue(comment);
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("curSpeed").setValue(String.valueOf(curSpeed));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("dateTimeInMillis").setValue(String.valueOf(datetime));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("distance").setValue(String.valueOf(distance));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("duration").setValue(String.valueOf(duration));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("heartrate").setValue(String.valueOf(heartrate));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("id").setValue(String.valueOf(id));
                            mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("inputType").setValue(String.valueOf(inputType));

                            int i = 0;
                            if (inputType != 0) {
                                for (LatLng location : LocationList) {
                                    mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("locationLatLngList").child(String.valueOf(i)).child("latitude").setValue(String.valueOf(location.latitude));
                                    mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).child("locationLatLngList").child(String.valueOf(i)).child("longitude").setValue(String.valueOf(location.longitude));
                                    i++;
                                }
                            }

                            // add event listener
                            EventListener(mUserId, id);
                        }
                    }
                }
            }
        });




        return view;
    }

    private static void EventListener(String mUserId, final Long id){
        mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(id)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("masterr", "remove?");
                if (HistoryFragment.old_ids.contains(id)){
                    Log.d("masterr", "hello?");
                    int idx = HistoryFragment.old_ids.indexOf(id);
                    helper.removeEntry(HistoryFragment.ids.get(idx));
                }
                else {
                    helper.removeEntry(id);
                }
                HistoryFragment.historyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void addListenerToAll(){
        mUserId = mFirebaseUser.getUid();
        ArrayList<DbEntry> tempEntries = helper.fetchEntries();
        for(DbEntry entry:tempEntries){
            long idd = entry.getId();
            if (HistoryFragment.ids.contains(idd)) {
                Log.d("masterr", "yes?");
                int idx = HistoryFragment.ids.indexOf(idd);
                EventListener(mUserId, HistoryFragment.old_ids.get(idx));
            }
            else {
                Log.d("masterr", "meh?");
                EventListener(mUserId, idd);
            }
        }
    }

    private void loadLogInView() {

        Intent intent = new Intent(getActivity(), LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}