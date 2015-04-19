package com.friscotap.core;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class BeerPageAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;

    public BeerPageAdapter(FragmentManager fm, List<Fragment> frags) {
        super(fm);
        this.fragments = frags;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public int getItemPosition(Object obj) {
        return POSITION_NONE;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        CharSequence title = "Beer List";

        switch (position) {
            case 0:
                title = "Draft Beers";
                break;
        }

        return title;
    }
}
