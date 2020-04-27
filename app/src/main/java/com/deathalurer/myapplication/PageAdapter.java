package com.deathalurer.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by Abhinav Singh on 26,April,2020
 */
public class PageAdapter extends FragmentPagerAdapter {
    int tabs;

    public PageAdapter(@NonNull FragmentManager fm, int tabs) {
        super(fm);
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                Fragment_Two fragment2 = new Fragment_Two();
                return fragment2;
            case 1:
                Fragment_Three fragment3 = new Fragment_Three();
                return fragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabs;
    }
}
