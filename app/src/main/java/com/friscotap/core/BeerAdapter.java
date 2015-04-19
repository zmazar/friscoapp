package com.friscotap.core;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class BeerAdapter extends ArrayAdapter<Beer> implements Filterable{
    private int mStateChangePosition = -1;
    private boolean mState = false;
    private ArrayList<Beer> mList;
    private ArrayList<Beer> mFilteredList;
    private ArrayList<Beer> mOriginalList;
    private BeerFilter mFilter;

    public BeerAdapter(Context context, int resource, List<Beer> objects) {
        super(context, resource, objects);
        this.mList = (ArrayList<Beer>) objects;
    }

    /**
     * Here's an added function to adjust the state of a single item.
     */
    public void setNewBeerState(int position, boolean state) {
        mStateChangePosition = position;
        mState = state;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        int fontSize;
        Context pContext = parent.getContext();
        LayoutInflater vi = LayoutInflater.from(getContext());
        SharedPreferences prefs = pContext.getSharedPreferences(Frisco.PREFS_APP, Context.MODE_PRIVATE);
        View v = vi.inflate(R.layout.beer_item, null);

        try {
            // Grab the font size to use for the beer list from the preferences.
            fontSize = prefs.getInt(Frisco.PREFS_FONT_SIZE, 7);
        }
        catch(ClassCastException e) {
            Log.d("BeerAdapter", e.toString());
            fontSize = 7;
        }

        Beer b = mList.get(pos);

        if(b != null) {
            TextView name = (TextView) v.findViewById(R.id.beer_name);
            TextView abv = (TextView) v.findViewById(R.id.beer_abv);

            // Check to see if the state of an item has been changed
            if(pos == mStateChangePosition) {
                b.setNewBeer(mState);
                mStateChangePosition = -1;
            }

            name.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);
            abv.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);

            if(name != null && abv != null) {
                name.setText(b.getName());
                abv.setText(b.getAbv());

                if(b.getOunces() == 10) {
                    name.setTextColor(Color.rgb(255, 215, 0));
                    abv.setTextColor(Color.rgb(255, 215, 0));
                }
                else if(b.getOunces() == 8) {
                    name.setTextColor(Color.GREEN);
                    abv.setTextColor(Color.GREEN);
                }

                if(b.isNewBeer()) {
                    name.setTypeface(null, Typeface.BOLD_ITALIC);
                    abv.setTypeface(null, Typeface.BOLD_ITALIC);
                }
            }
        }

        return v;
    }

    @Override
    public Filter getFilter() {
        if(mFilter == null) {
            mFilter = new BeerFilter();
        }

        return mFilter;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class BeerFilter extends Filter {

        @SuppressWarnings("unchecked")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();

            Log.d("BeerFilter", "performFiltering() " + constraint);

            if(mOriginalList == null) {
                mOriginalList = (ArrayList<Beer>) mList.clone();
            }

            if (constraint != null && constraint.length() > 0 ||
                    !constraint.equals(""))
            {
                ArrayList<Beer> tempList = new ArrayList<Beer>();

                // search content in friend list
                for (Beer beer : mOriginalList) {
                    if (beer.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(beer);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            }
            else {
                filterResults.count = mList.size();
                filterResults.values = mOriginalList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mFilteredList = (ArrayList<Beer>) results.values;
            notifyDataSetChanged();

            Log.d("BeerFilter", "publishResults()");
            clear();

            for(int i = 0; i < mFilteredList.size(); i++) {
                add(mFilteredList.get(i));
            }

            notifyDataSetInvalidated();
        }
    }
}
