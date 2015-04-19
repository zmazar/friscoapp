package com.friscotap.mugclub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.friscotap.core.BeerDataSource;
import com.friscotap.core.R;

public class MugClubFragment extends ListFragment {
    private static final String TAG = "MugClub Fragment";
    ArrayList<MugClubBeer> mDisplayedList;
    BeerDataSource mSource;
    MugClubAdapter mAdapter;

    public MugClubFragment() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // Enable action bar
        setHasOptionsMenu(true);

        // Setup the list structure
        mDisplayedList = new ArrayList<MugClubBeer>();

        // Get the mug club beers out of the database
        mSource = new BeerDataSource(getActivity());
        mSource.open();
        mDisplayedList = mSource.getMugBook();
        mSource.close();

        // Insert filler beers and 100th Natty Boh
        insertFillerBeers();

        // Setup adapter with this list
        mAdapter = new MugClubAdapter(
                getActivity(),
                R.id.mugclub_list_view,
                mDisplayedList
        );
        mAdapter.setNotifyOnChange(true);

        // Set the ListFragment's adapter
        setListAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mugclub_actionbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean bReturn = false;

        // Handle the presses of each item with a switch
        switch(item.getItemId()) {
            case R.id.action_confirm:
                onClickConfirm();
                bReturn = true;
                break;
            default:
                bReturn = super.onOptionsItemSelected(item);
        }

        return bReturn;
    }

    public void onClickConfirm() {
        ArrayList<MugClubBeer> unconfirmedList = new ArrayList<MugClubBeer>();

        // TODO: Grab only the unconfirmed beers from the database
        mSource.open();
        mDisplayedList = mSource.getMugBook();

        for(int i = 0; i < mDisplayedList.size(); i++) {
            MugClubBeer beer = mDisplayedList.get(i);

            if(!beer.isConfirmed()) {
                // Add to the unconfirmed list
                unconfirmedList.add(beer);
            }
        }

        mSource.close();

        // Insert null beers and a Natty Boh up to 100 beers total
        //insertFillerBeers();

        // TODO: Part of what needs to get moved
        // Lets refresh the adapter
		/*
		mAdapter.clear();
		mAdapter.addAll(mDisplayedList);
		mAdapter.notifyDataSetChanged();
		*/

        // Create the confirmation fragment with the unconfirmed list of
        // beers in the mug club book
        DialogFragment f = ConfirmationFragment.newInstance(unconfirmedList);
        f.show(getChildFragmentManager(), TAG);
    }

    private void insertFillerBeers() {
        // Check to see if we should insert the filler beers
        if(mDisplayedList.size() < 100) {
            while(mDisplayedList.size() < 99) {
                MugClubBeer nullBeer = new MugClubBeer();

                // Add the null beer to the list
                mDisplayedList.add(nullBeer);
            }

            MugClubBeer nattyBoh = new MugClubBeer();
            nattyBoh.setName("National Bohemian");
            mDisplayedList.add(nattyBoh);
        }
    }

    private class MugClubAdapter extends ArrayAdapter<MugClubBeer> {
        private ArrayList<MugClubBeer> items;

        public MugClubAdapter(Context context, int resource, List<MugClubBeer> objects) {
            super(context, resource, objects);
            this.items = (ArrayList<MugClubBeer>) objects;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            View v = vi.inflate(R.layout.mugclub_beer_item, null);
            int position = pos + 1;

            MugClubBeer b = items.get(pos);

            if(b != null) {
                // Setup the view for this specific beer
                TextView tt1 = (TextView) v.findViewById(R.id.mugclub_beer_item_index);
                TextView tt2 = (TextView) v.findViewById(R.id.mugclub_beer_item_text);
                TextView tt3 = (TextView) v.findViewById(R.id.mugclub_beer_item_subtext);
                Date date = b.getDate();
                String beerName = b.getName();

                if(position % 10 == 0) {
                    v.setBackgroundColor(Color.rgb(50, 50, 50));
                    tt1.setBackgroundColor(Color.rgb(50, 50, 50));
                    tt2.setBackgroundColor(Color.rgb(50, 50, 50));
                    tt3.setBackgroundColor(Color.rgb(50, 50, 50));
                }

                tt1.setText("" + position);
                tt2.setText(beerName);

                // If the beer isn't confirmed, put "Added on "
                if(!b.isConfirmed()) {
                    tt1.setTextColor(Color.rgb(211, 211, 211));
                    tt2.setTextColor(Color.rgb(211, 211, 211));

                    // If it's a filler beer then don't print the date
                    if(!beerName.equals("") && !beerName.equals("National Bohemian")) {
                        tt3.setText("Added on " + date.toString());
                    }
                }
                // Otherwise then put "Verified on "
                else {
                    tt3.setText("Verified on " + date.toString());
                }
            }

            return v;
        }
    }
}
