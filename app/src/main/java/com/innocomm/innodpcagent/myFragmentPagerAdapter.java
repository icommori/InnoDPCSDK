package com.innocomm.innodpcagent;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

public class myFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "myFragmentPagerAdapter";
    private Context myContext;
    protected Hashtable<Integer, WeakReference<Fragment>> fragmentReferences = new Hashtable<>();
    public myFragmentPagerAdapter(@NonNull FragmentManager fm, Context myContext) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.myContext = myContext;
    }

    @Override
    public Fragment getItem(int position) {
        Log.v(TAG,"getItem: "+position);
        Fragment fragment;
        switch (position) {
            case 0:
                SpecificDPCFragment specificdpcfragment = new SpecificDPCFragment();
                fragment = specificdpcfragment;
                break;
            case 1:
                UserDPCFragment userdpcfragment = new UserDPCFragment();
                fragment = userdpcfragment;
                break;
            case 2:
                ProprietaryAPIFragment proprietaryapifragment = new ProprietaryAPIFragment();
                fragment = proprietaryapifragment;
                break;
            default:
                return null;
        }

        fragmentReferences.put(position, new WeakReference<Fragment>(fragment));
        return fragment;
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return 3;
    }

    public Fragment getFragment(int fragmentId) {
        WeakReference<Fragment> ref = fragmentReferences.get(fragmentId);
        return ref == null ? null : ref.get();
    }
}
