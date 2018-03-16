package com.example.ping_jungliu.myruns3;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Ping-Jung Liu on 2018/1/12.
 */

// implement fragmentPagerAdapter
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int pos){
        return fragments.get(pos);
    }

    @Override
    public int getCount(){
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        if(position == 0)
            return "Start";
        else if(position == 1)
            return "History";
        else if(position == 2)
            return "Setting";
        else
            return null;
    }
}