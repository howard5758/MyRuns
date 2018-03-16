package com.example.ping_jungliu.myruns3;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Ping-Jung Liu on 2018/2/5.
 */

public class Tracking_Service extends Service implements LocationListener, SensorEventListener {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE_BROADCAST_KEY="StopServiceBroadcastKey";
    final static int RQS_STOP_SERVICE = 1;
    NotifyServiceReceiver notifyServiceReceiver;

    Tracking_Binder trackingBinder = new Tracking_Binder();
    LocationManager locationManager;
    Location previous_location;
    String provider;
    int time_passed;
    Timer timer;
    DbEntry entry;
    boolean working;

    private SensorManager mSensorManager;
    static ArrayBlockingQueue<Double> sensor_que;
    SensorTask asyncTask;


    @Override
    public void onCreate() {
        super.onCreate();

        // set up some variables, timer, and entry
        time_passed = 0;
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                time_passed = time_passed + 1;
            }
        };
        timer.schedule(timerTask, 0, 1000);

        sensor_que =new ArrayBlockingQueue<Double>(2048);

        ArrayList<LatLng> locList;
        entry = new DbEntry();
        locList = new ArrayList<LatLng>();
        entry.setLocationList(locList);
        entry.setDateTime(Calendar.getInstance().getTimeInMillis());
        entry.setDistance(0);
        entry.setDuration(0);
        entry.setCalories(0);
        entry.setHeartRate(0);

        // Sets up receiver
        notifyServiceReceiver = new NotifyServiceReceiver();

        // service working
        working = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // find best provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);

        // set up sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);

        // get location
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {};
                Location l = locationManager.getLastKnownLocation(provider);
                // update location!
                Log.d("master", "start?");
                location_update(l,true);
            }
        }, 100);

        // request location update for first location
        locationManager.requestLocationUpdates(provider, 0, 0, this);

        // set up notification
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        Context context = getApplicationContext();
        String notificationTitle = "MyRuns";
        String notificationText = "Recording your path now";

        Intent resultIntent = new Intent(context, MapsActivity.class);

        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
        int notifyID = 1;
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "channel";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText).setSmallIcon(R.drawable.test)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pendingIntent).build();

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager.notify(notifyID , notification);

        //// I found out that channelId is required for 8.0 to show notification :/



        asyncTask = new SensorTask();
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return trackingBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
        mSensorManager.unregisterListener(this);
        this.unregisterReceiver(notifyServiceReceiver);
        working = false;
    }


    // update location
    public void onLocationChanged(Location location) {
        Log.d("master", "changed?");
        location_update(location, false);
    }

    // the boolean indicates whether the location is start or not
    public void location_update(Location location, boolean start) {

        if (location != null) {
            LatLng latLng = LocationToLatLng(location);
            entry.addLocationList(latLng);

            Intent intent = new Intent();
            intent.setAction("NotifyLocationUpdate");

            // If this is start
            if (start) {
                intent.putExtra("Update_Key", 1);
            } else {
                update_entry(location);
                intent.putExtra("Update_Key", 2);
            }
            // converts to km/hr
            intent.putExtra("current_speed", location.getSpeed() * 3.6);
            sendBroadcast(intent);

        }
    }

    public void startActivityUpdate(double classify){

        // Prepares intent to notify the MapDisplayActivity of an activity type update
        Intent intent = new Intent();
        intent.setAction("NotifyLocationUpdate");

        // Adds to the intent the message that there's been an activity type update
        intent.putExtra("Update_Key", 3);

        // Sets up activity type based on classify parameter
        int activityType;
        if(classify == 0){
            activityType = 2;
        }
        else if(classify == 1){
            activityType = 1;
        }
        else if(classify == 2){
            activityType = 0;
        }
        else{
            activityType = 20;
        }

        intent.putExtra("activity_type", activityType);

        // activity update
        sendBroadcast(intent);
    }

    // update the entry
    public void update_entry(Location location){

        if (location != null) {

            double newDistance = 0;
            double newClimb = 0;

            // get new distance and climb
            if (previous_location != null) {
                newDistance = (double) location.distanceTo(previous_location) / 1000;
                newClimb = (location.getAltitude() - previous_location.getAltitude()) / 1000;
            }

            previous_location = location;

            // update distance and climb
            entry.setDistance(entry.getDistance() + newDistance);
            entry.setClimb(entry.getClimb() + newClimb);

        }

        double distance = entry.getDistance();

        if (distance > 0) {
            // Update the average speed and calories
            entry.setAvgSpeed(60 * 60 * distance / time_passed);
            entry.setCalories( (int) ((distance/1.61) * 100) );

        }
    }

    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    // define binder
    public class Tracking_Binder extends Binder {
        public Tracking_Service return_service() {
            return Tracking_Service.this;
        }
    }

    public class NotifyServiceReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            int rqs = arg1.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == RQS_STOP_SERVICE){
                stopSelf();
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .cancelAll();
            }
        }
    }

    public DbEntry getEntry(){
        return entry;
    }

    public static LatLng LocationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public int service_time_passe(){
        return time_passed;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION)
            return;

        //acceleration
        double acc = Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]);

        // check capacity of que
        try {
            // add to que
            sensor_que.add(new Double(acc));
        }
        // too big, create larger buffer
        catch (IllegalStateException e) {

            ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(sensor_que.size() * 2);

            sensor_que.drainTo(newBuf);
            sensor_que = newBuf;
            sensor_que.add(new Double(acc));
        }
    }


    private class SensorTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {

            int size = 0;
            Double[] featVec = new Double[65];
            FFT fft = new FFT(64);

            double[] accBlock = new double[64];
            double[] re = accBlock;
            double[] im = new double[64];
            Log.d("Testing", "Do in background");

            while (true) {
                try {

                    if (isCancelled () == true){
                        return null;
                    }
                    // Adds to the block from the sensor queue
                    try {
                        accBlock[size++] = sensor_que.take().doubleValue();
                    }catch(Exception e) {
                        Log.d("no", "no");
                    }

                    // reset
                    if (size >= 64) {
                        size = 0;

                        // Finds the max value from the block
                        double max = 0.0;
                        for (int idx = 0; idx < accBlock.length; idx++){
                            if (max < accBlock[idx]){
                                max = accBlock[idx];
                            }
                        }

                        fft.fft(re, im);

                        // magnitude of feature
                        for (int i = 0; i < re.length; i++) {
                            double mag = Math.sqrt(re[i] * re[i] + im[i]
                                    * im[i]);
                            featVec[i] = mag;
                            im[i] = .0;
                        }

                        featVec[64] = max;
                        // WEKA!
                        double classify = WekaClassifier.classify(featVec);
                        // update activity
                        startActivityUpdate(classify);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
