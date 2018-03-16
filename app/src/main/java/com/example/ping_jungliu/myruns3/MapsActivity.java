package com.example.ping_jungliu.myruns3;

import android.support.v4.app.FragmentActivity;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements ServiceConnection, OnMapReadyCallback{

    private GoogleMap mMap;
    public boolean is_bounded, is_history, map_update;
    private Tracking_Service trackingService;
    broadCastReceiver broadCastReceiver;
    double current_speed;
    Intent serviceIntent;
    ServiceConnection connection = this;
    private MyDbHelper DbHelper;
    private DbEntry entry;
    Marker start_marker, end_marker;
    long rowid;

    int current_activity;
    int run, walk, stand;
    int input_typee;
    String temp_type;

    public static double speedd;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // set up needed variables
        DbHelper = new MyDbHelper(this);
        rowid = getIntent().getLongExtra("row_id", -1);
        is_history = getIntent().getBooleanExtra("is_history", false);

        // map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (savedInstanceState != null ) {
            Bundle bundle = getIntent().getExtras();
            run = bundle.getInt("run");
            walk = bundle.getInt("walk");
            stand = bundle.getInt("stand");
        }

        // since we are using maps for both tracking and show history, check if this is history
        if (!is_history) {

            // start and bind
            startService();
            broadCastReceiver = new broadCastReceiver();
            is_bounded = false;
//            bindService();
        }
        else {

            // if is history, get rid of buttons
            (findViewById(R.id.button_save_gps)).setVisibility(View.GONE);
            (findViewById(R.id.button_cancel_gps)).setVisibility(View.GONE);

            entry = new ReadFromDB(this).loadInBackground();
            // will draw the map in onMapReady to fix a bug
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the counts of each activity type
        outState.putInt("run", run);
        outState.putInt("walk", walk);
        outState.putInt("stand", stand);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!is_history) {
            bindService();
            IntentFilter intentFilter = new IntentFilter();
            Log.d("resume???", "ok");
            intentFilter.addAction("NotifyLocationUpdate");
            registerReceiver(broadCastReceiver, intentFilter);
        }
    }

    protected void onPause() {
        super.onPause();
        if (!is_history) {
            unregisterReceiver(broadCastReceiver);
            unbindService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            exitt();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Africa"));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // if is history, draw and update after making sure map is ready
        if(is_history){
            drawHistoryOnMap();
            updateStat();
        }
        else{
            bindService();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        trackingService = ((Tracking_Service.Tracking_Binder) service).return_service();
        if (map_update) {
            entry = trackingService.getEntry();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        trackingService = null;
    }

    // the broad cast receiver!!
    public class broadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (trackingService != null) {

                int broadCastType = intent.getIntExtra("Update_Key", 0);
                entry = trackingService.getEntry();
                current_speed = intent.getDoubleExtra("current_speed", 0);
                if (broadCastType == 3) {
                    current_activity = intent.getIntExtra("activity_type", 0);
                    Log.d("holy" + current_activity, "man");
                    if(current_activity == 0){
                        run ++;
                    }
                    else if(current_activity == 1){
                        walk ++;
                    }
                    else if(current_activity == 2){
                        stand++;
                    }
                    updateStat();
                }
                else{
                    drawTraceOnMap(broadCastType);
                    updateStat();
                }
            }
        }
    }

    /////////// buttons
    public void map_save(View v) {

        if (entry.getLocationList() != null && entry.getLocationList().size() > 1)
            entry.getLocationList().remove(0);

        // set time_passed as duration
        entry.setDuration(trackingService.service_time_passe());
        // get the input type
        String input_type = getIntent().getStringExtra("input_type");

        if (input_type.equals("GPS")){
            entry.setInputType(1);
        }
        else if (input_type.equals("Automatic")){
            entry.setInputType(2);
            int maxCount = Math.max(Math.max(run, walk), stand);

            // Set the current activity based on the majority activity
            if (maxCount == run)
                entry.setActivityType("running");
            else if (maxCount == walk)
                entry.setActivityType("walking");
            else
                entry.setActivityType("standing");
        }

        // write to database
        new writeDB().execute();
        exitt();
        finish();
    }

    public void map_cancel(View v) {
        Toast.makeText(getApplicationContext(), "CANCEL",
                Toast.LENGTH_SHORT).show();
        exitt();
        finish();
    }

    @Override
    public void onBackPressed() {
        exitt();
        finish();
        super.onBackPressed();
    }

    // draw on map
    public void drawTraceOnMap(int isFirst) {

        // this could prevent other threads from changing the value of entry during the opertaion
        synchronized (entry.getLocationList()) {

            // If this is the first point
            if (isFirst == 1 && entry.getLocationList() != null && entry.getLocationList().size() == 1) {

                String input_type = getIntent().getStringExtra("input_type");
                if (input_type.equals("GPS")){
                    input_typee = 1;
                    entry.setInputType(1);
                    entry.setActivityType(getIntent().getStringExtra("activity_type"));
                }
                else if (input_type.equals("Automatic")){
                    input_typee = 2;
                    entry.setInputType(2);
                }


                // Draw start
                ArrayList<LatLng> latLngList = entry.getLocationList();
                start_marker = mMap.addMarker(new MarkerOptions().position(latLngList.get(0)).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN)));

                // Draw end
                end_marker = mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size() - 1))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                // Zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0),
                        17));

            } else if (entry.getLocationList() != null && entry.getLocationList().size() > 1) {

                // Get the start marker
                ArrayList<LatLng> latLngList = entry.getLocationList();
                if (start_marker != null) {
                    start_marker.remove();
                }
                start_marker = mMap.addMarker(new MarkerOptions().position(latLngList.get(1)).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN)));

                // Draw polyline
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLACK);
                polylineOptions.width(7);
                ArrayList<LatLng> temp = new ArrayList<>(latLngList);
                temp.remove(0);
                polylineOptions.addAll(temp);
                mMap.addPolyline(polylineOptions);

                // Draw end
                if (end_marker != null) {
                    end_marker.remove();
                }
                end_marker = mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size() - 1))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(latLngList.size() - 1),
                        17));

            }
        }
    }

    // draw history on map
    public void drawHistoryOnMap() {

        ArrayList<LatLng> latLngList = entry.getLocationList();

        // Get start
        LatLng latlng = latLngList.get(0);
        start_marker = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)));
        // Zoom
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,17));

        // Draw polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(7);
        polylineOptions.color(Color.BLACK);
        polylineOptions.addAll(latLngList);
        mMap.addPolyline(polylineOptions);

        // Draw end
        end_marker = mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size() - 1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    // update the stat numbers
    public void updateStat() {

        // Get unit preference
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString("unit_prefernece", "Kilometers");

        // set some textviews
        TextView mapType = (TextView) findViewById(R.id.map_type);
        mapType.setText("Type: " + entry.getActivityType());
        if (input_typee == 2){
            int maxCount = Math.max(Math.max(run, walk), stand);

            // Set the current activity based on the majority activity
            if (maxCount == stand)
                temp_type = "standing"; // set to "Running"
            else if (maxCount == walk)
                temp_type = "walking"; // set to "Walking"
            else
                temp_type = "running"; // set to "Standing"
            mapType.setText("Type: " + temp_type);
        }


        speedd = entry.getAvgSpeed();
        String unit = "km/h";
        if (unitPref.equals("Miles")) {
            speedd /= 1.61; // converts from km to miles
            unit = "m/h";
        }
        TextView gpsAvgSpeed = (TextView) findViewById(R.id.map_avg_speed);
        gpsAvgSpeed.setText("Avg speed: " + String.format("%.2f", speedd) + " " + unit);

        String cur_speed = "n/a";
        unit = "km/h";
        speedd = current_speed;
        if (unitPref.equals("Miles")) {
            speedd /= 1.61; // converts from km to miles
            unit = "m/h";
        }
        TextView gpsCurSpeed = (TextView) findViewById(R.id.map_cur_speed);
        gpsCurSpeed.setText("Cur speed: " + String.format("%.2f", speedd) + " " + unit);

        TextView gpsClimb = (TextView) findViewById(R.id.map_climb);
        gpsClimb.setText("Climb: " + String.format("%.2f", entry.getClimb()) + " " + unitPref);

        TextView gpsCalorie = (TextView) findViewById(R.id.map_calories);
        gpsCalorie.setText("Calorie: "+entry.getCalories());

        TextView gpsDistance = (TextView) findViewById(R.id.map_distance);
        gpsDistance.setText("Distance: " + String.format("%.2f", entry.getDistance()) + " " + unitPref);
    }

    // async save just like last time
    private class writeDB extends AsyncTask<DbEntry, Integer, String> {

        protected String doInBackground(DbEntry... exerciseEntries) {
            long id = DbHelper.insertEntry(entry);
            return ""+id;
        }

        protected void onPostExecute(String result) {
            HistoryFragment.historyAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Entry #"+result+" saved.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // i learned to read with asyn this time :/
    public class ReadFromDB extends AsyncTaskLoader<DbEntry> {

        public ReadFromDB(Context context){
            super(context);
        }

        @Override
        public DbEntry loadInBackground() {
            DbEntry entry = DbHelper.fetchEntryByIndex(rowid);
            return entry;
        }
    }

    // menu related functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(is_history) {
            MenuItem menuu = menu.add(Menu.NONE, 0, 0, "DELETE");
            menuu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
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
                            DbHelper.removeEntry(rowid);
                            HistoryFragment.historyAdapter.notifyDataSetChanged();
                            mUserId = mFirebaseUser.getUid();

                            if (HistoryFragment.ids.contains(rowid)) {
                                int idx = HistoryFragment.ids.indexOf(rowid);
                                mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(HistoryFragment.old_ids.get(idx))).removeValue();
                            }
                            else {
                                mDatabase.child("users").child(mUserId).child("runs").child(String.valueOf(rowid)).removeValue();
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

    /////////////////////////service helpers
    // start service
    public void startService() {
        serviceIntent = new Intent(this, Tracking_Service.class);
        startService(serviceIntent);
    }

    // bind service
    void bindService() {
        if (!is_bounded) {
            bindService(this.serviceIntent, connection, Context.BIND_AUTO_CREATE);
            is_bounded = true;
        }
    }

    // unbind service
    public void unbindService() {
        if (is_bounded) {
            unbindService(this.connection);
            is_bounded = false;
        }
    }

    // destroy service
    private void exitt() {
        if (!is_history) {
            if (trackingService != null) {
                Intent intent = new Intent();
                intent.setAction(Tracking_Service.ACTION);
                intent.putExtra(Tracking_Service.STOP_SERVICE_BROADCAST_KEY, Tracking_Service.RQS_STOP_SERVICE);
                sendBroadcast(intent);
                unbindService();
                stopService(serviceIntent);
            }
        }
    }
}
