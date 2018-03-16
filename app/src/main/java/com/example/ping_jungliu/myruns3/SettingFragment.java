package com.example.ping_jungliu.myruns3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by Ping-Jung Liu on 2018/1/12.
 */

public class SettingFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the settings_preferences from an XML resource
        addPreferencesFromResource(R.xml.setting_fragment);


    }


}