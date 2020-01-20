package com.zuozhan.app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MyAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> arrayList;

    public MyAdapter(FragmentManager fm, ArrayList<Fragment> arrayList) {
        super(fm);
        this.arrayList = arrayList;
    }

    @Override
    public Fragment getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }
}
