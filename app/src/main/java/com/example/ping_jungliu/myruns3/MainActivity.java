package com.example.ping_jungliu.myruns3;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    StartFragment startFragment;
    HistoryFragment historyFragment;
    SettingFragment settingFragment;
    ViewPager viewPager;
    TabLayout tabLayout;
    MyFragmentPagerAdapter myFragmentPagerAdapter;
    ArrayList<Fragment> fragments;
    SharedPreferences pref;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;
    private String mUserId;

    MyDbHelper helper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        // when logout   erase all local memory
        // will get data from firebase upon login!
        if (id == R.id.action_logout) {

            helper = new MyDbHelper(this);
            ArrayList<DbEntry> entries = helper.fetchEntries();
            Log.d("master", "whoa");
            for(DbEntry entry:entries){
                final long idd = entry.getId();
                helper.removeEntry(idd);
            }
            HistoryFragment.historyAdapter.notifyDataSetChanged();

            mFirebaseAuth.signOut();
            loadLogInView();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        com.example.ping_jungliu.myruns3.Util.checkPermission(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                HistoryFragment.historyAdapter.notifyDataSetChanged();
            }
        };
        pref = PreferenceManager.getDefaultSharedPreferences(this);


        // define viewPager and tabLayout
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        tabLayout = (TabLayout)findViewById(R.id.tab);
        // define the three fragments
        startFragment = new StartFragment();
        historyFragment = new HistoryFragment();
        settingFragment = new SettingFragment();

        // specify PagerAdapter and tabLayout
        fragments = new ArrayList<Fragment>();
        fragments.add(startFragment);
        fragments.add(historyFragment);
        fragments.add(settingFragment);

        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getFragmentManager(), fragments);
        viewPager.setAdapter(myFragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


    }

    protected void onResume(){
        super.onResume();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(listener);
    }

    protected void onPause(){
        super.onPause();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }
    protected void onDestroy(){
        super.onDestroy();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void loadLogInView() {

        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


}
