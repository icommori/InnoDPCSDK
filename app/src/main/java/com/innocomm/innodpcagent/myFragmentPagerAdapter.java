package com.innocomm.innodpcagent;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class myFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "myFragmentPagerAdapter";
    private Context myContext;

    public myFragmentPagerAdapter(@NonNull FragmentManager fm, Context myContext) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.myContext = myContext;
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG,"getItem: "+position);
        switch (position) {
            case 0:
                SpecificDPCFragment specificdpcfragment = new SpecificDPCFragment();
                return specificdpcfragment;
            case 1:
                UserDPCFragment userdpcfragment = new UserDPCFragment();
                return userdpcfragment;
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return 2;
    }
}
