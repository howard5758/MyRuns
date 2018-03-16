package com.example.ping_jungliu.myruns3;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ping-Jung Liu on 2018/1/28.
 */

// define the database entry
public class DbEntry implements Serializable {

    long id;
    int inputType;
    String activityType;
    long dateTime;
    double duration;
    double distance;
    double calories;
    double heartRate;
    String comment;

    double avg_speed;
    double climb;
    ArrayList<LatLng> LocationList;

    public void DbEntry(){
        activityType = "";
    }

    public void setId(long id){
        this.id = id;
    }
    public long getId(){
        return id;
    }

    public void setInputType(int inputType){
        this.inputType = inputType;
    }
    public int getInputType(){
        return inputType;
    }

    public void setActivityType(String activityType){
        this.activityType = activityType;
    }
    public String getActivityType(){
        return activityType;
    }

    public void setDateTime(long dateTime){
        this.dateTime = dateTime;
    }
    public long getDateTime(){
        return dateTime;
    }

    public void setDuration(double duration){
        this.duration = duration;
    }
    public double getDuration(){
        return duration;
    }

    public void setDistance(double distance){
        this.distance = distance;
    }
    public double getDistance() {
        return distance;
    }

    public void setCalories(double calories){
        this.calories = calories;
    }
    public double getCalories(){
        return calories;
    }

    public void setHeartRate(double heartRate){
        this.heartRate = heartRate;
    }
    public double getHeartRate(){
        return heartRate;
    }

    public void setComment(String comment){
        this.comment = comment;
    }
    public String getComment(){
        return comment;
    }

    public void setAvgSpeed(double avg_speed) {
        this.avg_speed = avg_speed;
    }
    public double getAvgSpeed() {
        return avg_speed;
    }

    public void setClimb(double climb) {
        this.climb = climb;
    }
    public double getClimb() {
        return climb;
    }

    public void setLocationList(ArrayList<LatLng> LocationList) { this.LocationList = LocationList; }
    public void addLocationList(LatLng LatLng) { LocationList.add(LatLng); }
    public ArrayList<LatLng> getLocationList() { return LocationList; }
}
