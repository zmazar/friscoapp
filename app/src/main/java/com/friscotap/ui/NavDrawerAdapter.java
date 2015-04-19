package com.friscotap.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.friscotap.core.R;

public class NavDrawerAdapter extends ArrayAdapter<NavDrawerItem> {
    private ArrayList<NavDrawerItem> items;

    public NavDrawerAdapter(Context context, int resource,
                            List<NavDrawerItem> objects) {
        super(context, resource, objects);
        this.items = (ArrayList<NavDrawerItem>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = LayoutInflater.from(getContext());
        View v = vi.inflate(R.layout.drawer_item, null);

        NavDrawerItem menu = items.get(position);

        if(menu != null) {
            TextView tv = (TextView) v.findViewById(R.id.drawer_menu_title);
            tv.setText(menu.getTitle());

            if(menu.getEnabled() == false) {
                tv.setClickable(false);
            }

            if(menu.getSubmenu() == true) {
                tv.setAllCaps(true);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5);
            }
        }

        return v;
    }
}
