package com.friscotap.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.friscotap.core.R;
import com.friscotap.notification.BeersTappedDialog;
import com.friscotap.notification.NotificationReceiver;


public class BeerListFragment extends ListFragment implements SearchView.OnQueryTextListener {
    private static final String TAG = "Frisco BeerFragment";
    private ArrayList<Beer> mDisplayedList;
    private BeerAdapter mAdapter;
    private BeerDataSource mDataSource;
    private NotificationReceiver mAlarm;
    private String mTableName;
    private String mUrl;
    private int mLocation;
    private int mNumNewBeers;
    private int mFriscoId;

    private SearchView mSearchView;

    public static final BeerListFragment newInstance(
            String tableName,
            String url
    )
    {
        BeerListFragment f = new BeerListFragment();
        Bundle bdl = new Bundle(2);

        // Set bundle arguments to pass in
        bdl.putString("TableName", tableName);
        bdl.putString("Url", url);
        f.setArguments(bdl);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // Set private variables with arguments
        mTableName = getArguments().getString("TableName");
        mUrl = getArguments().getString("Url");

        // Set the Frisco location ID
        if(mUrl.contains("wcmobile")) {
            mFriscoId = Frisco.LOCATION_CROFTON;
        }
        else {
            mFriscoId = Frisco.LOCATION_COLUMBIA;
        }

        // Setup action bar
        setupActionBar();

        // Setup the database and read initial database beers out of it
        mDataSource = new BeerDataSource(getActivity(), mTableName, mFriscoId);
        mDataSource.open();
        mDisplayedList = mDataSource.getAllBeers();
        mDataSource.close();

        // Create the beer adapter, and set it.
        mAdapter = new BeerAdapter(
                getActivity(),
                R.layout.beer_item,
                mDisplayedList
        );
        mAdapter.setNotifyOnChange(true);

        // Setup the list adapter
        setListAdapter(mAdapter);

        // Run the update task
        new RemoteUpdateTask().execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frisco_actionbar, menu);

        // Setup the search widget stuff
        MenuItem searchItem = menu.findItem(R.id.action_search);

        // Allow the list to be searchable
        getListView().setTextFilterEnabled(true);
        setupSearchView(searchItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean bReturn = false;

        // Handle the presses of each item with a switch
        switch(item.getItemId()) {
            case R.id.action_refresh:
                new RemoteUpdateTask().execute();
                bReturn = true;
                break;
            case R.id.action_clear_beers:
                resetNewBeers();
                bReturn = true;
                break;
            default:
                bReturn = super.onOptionsItemSelected(item);
        }

        return bReturn;
    }

    @Override
    public void onStop() {
        super.onStop();

        new SaveBeerListTask().execute();
    }

    @Override
    public void onListItemClick(ListView l, View v, int index, long id) {
        super.onListItemClick(l, v, index, id);
        Log.d(TAG, "onListItemClick()");

        // Remove the New Beer attribute from the clicked beer item
        mAdapter.setNewBeerState(index, false);
        mAdapter.notifyDataSetChanged();

        /**
         * For now this section is removed,, until a legit details page can
         * be displayed
         *
         // Get the beer from the list
         Beer b = (Beer) l.getItemAtPosition(index);

         Intent intent = new Intent(v.getContext(), BeerDetailsActivity.class);
         intent.putExtra("beer", b);
         startActivity(intent);
         */
    }

    public void cancelNotificationReceiver() {
        Context context = getActivity();

        // Check to see if the alarm is already running
        Intent intent = new Intent(context, NotificationReceiver.class);
        boolean alarmUp = (
                PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_NO_CREATE
                ) != null
        );

        if(alarmUp) {
            // Alarm is active, lets cancel it.
            if(mAlarm != null) {
                Log.d(TAG, "Canceling alarm");
                mAlarm.cancelAlarm(context);
            }
            else {
                Log.d(TAG, "Alarm is up, but unable to cancel");
            }
        }
        else {
            // Don't want to do anything
            Log.d(TAG, "Alarm isn't active to cancel");
        }
    }

    public void refreshListAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    public void resetNewBeers() {
        // Reset the current list of beers
        Log.d(TAG, "resetNewBeers()");

        for(int i = 0; i < mDisplayedList.size(); i++) {
            Beer b = mDisplayedList.get(i);
            b.setNewBeer(false);

            mDisplayedList.set(i, b);
        }

        // Update the displayed beer list
        mAdapter.clear();
        mAdapter.addAll(mDisplayedList);
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<Beer> compareAndUpdate(ArrayList<Beer> newList) {
        ArrayList<Beer> newTappedBeers = new ArrayList<Beer>();

        for(int i = 0; i < newList.size(); i++) {
            // Get the current beer that we're checking
            Beer beer = newList.get(i);

            // Now check to see if the current beer is in the old list.
            int oldIndex = mDisplayedList.indexOf(beer);

            if(oldIndex == -1) {
                // If it's not in the old list, then the beer is new.
                beer.setNewBeer(true);
                newTappedBeers.add(beer);
            }
            else {
                Beer oldBeer = mDisplayedList.get(oldIndex);

                // The current beer is in the old list, so get the attributes
                beer.setNewBeer(oldBeer.getNewBeer());
            }

            // Set the current beer to active, and add it to the list.
            beer.setActive(true);
        }

        return newTappedBeers;
    }

    private void setupActionBar() {
        // Activate the ActionBar
        setHasOptionsMenu(true);

        if(mLocation == Frisco.LOCATION_COLUMBIA) {
            getActivity().getActionBar().setSubtitle("Columbia");
        }
        else if (mLocation == Frisco.LOCATION_CROFTON) {
            getActivity().getActionBar().setSubtitle("Crofton");
        }
    }

    private void setupSearchView(MenuItem item) {
        SearchManager sManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        item.setShowAsActionFlags(
                MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW |
                        MenuItem.SHOW_AS_ACTION_ALWAYS
        );

        mSearchView = (SearchView) item.getActionView();

        if(sManager != null && mSearchView != null) {
            mSearchView.setSearchableInfo(
                    sManager.getSearchableInfo(getActivity().getComponentName())
            );
            mSearchView.setOnQueryTextListener(this);
        }
    }

    private class RemoteUpdateTask
            extends AsyncTask<Void, Void, ArrayList<Beer>> {
        private static final String TAG = "FriscoService";
        private ProgressDialog mProgress;

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(getActivity());
            mProgress.setTitle("Frisco Taphouse");
            mProgress.setCancelable(false);
            mProgress.setMessage("Loading beer list...");
            mProgress.show();
        }

        @Override
        protected ArrayList<Beer> doInBackground(Void... params) {
            Document html;
            ArrayList<Beer> newList = new ArrayList<Beer>();

            try {
                // Grab the entire beer page from the given URL
                html = Jsoup.connect(mUrl).get();

                // Setup all the name and abv element variables.  Each needs to
                // be declared for 16oz, 10oz and featured beers.
                // 16oz
                Elements names16 = html.select(Frisco.SixteenOunceNameQuery);
                Elements abvs16 = html.select(Frisco.SixteenOunceAbvQuery);

                // 10oz
                Elements names10 = html.select(Frisco.TenOunceNameQuery);
                Elements abvs10 = html.select(Frisco.TenOunceAbvQuery);

                // Featured
                Elements namesF = html.select(Frisco.FeaturedNameQuery);
                Elements abvsF = html.select(Frisco.FeaturedAbvQuery);

                // Make sure that number of names found is the same as the
                // number of ABVs found.  Otherwise, return null.
                if(names16.size() != abvs16.size() ||
                        names10.size() != abvs10.size() ||
                        namesF.size() != abvsF.size())
                {
                    Log.d(TAG, "Unmatched list sizes");
                    return null;
                }

                // 16oz Beers: Parse out each beer
                for(int i = 0; i < names16.size(); i++) {
                    Element name = names16.get(i);
                    Element abv = abvs16.get(i);

                    Beer b = new Beer();
                    b.setOunces(16);
                    b.setName(name.text());
                    b.setAbv(abv.text());
                    b.setFriscoId(mFriscoId);

                    newList.add(b);
                }

                // 10oz Beers: Parse out each beer
                for(int i = 0; i < names10.size(); i++) {
                    Element name = names10.get(i);
                    Element abv = abvs10.get(i);

                    Beer b = new Beer();
                    b.setOunces(10);
                    b.setName(name.text());
                    b.setAbv(abv.text());
                    b.setFriscoId(mFriscoId);

                    newList.add(b);
                }

                // Featured Beers: Parse out each beer
                for(int i = 0; i < namesF.size(); i++) {
                    Element name = namesF.get(i);
                    Element abv = abvsF.get(i);

                    Beer b = new Beer();
                    b.setOunces(8);
                    b.setName(name.text());
                    b.setAbv(abv.text());
                    b.setFriscoId(mFriscoId);

                    newList.add(b);
                }
            }
            catch (IOException e) {
                Log.d(TAG, "IOException: " + e.toString());
            }
            catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }

            return newList;
        }

        @Override
        protected void onPostExecute(ArrayList<Beer> listItems) {
            Log.d(TAG, "Put " + listItems.size() + " beers in the list");
            ArrayList<Beer> newTappedBeers;

            if(listItems.size() > 0) {
                // Now we need to make a comparison, and find our new beers
                newTappedBeers = compareAndUpdate(listItems);
                mNumNewBeers = newTappedBeers.size();

                // Update the displayed list
                mDisplayedList = listItems;

                // Display an alert dialog to notify how many new beers
                // were found on refresh.
                if(mNumNewBeers > 0) {
                    // Create the beers tapped dialogue fragment and display it
                    DialogFragment f = BeersTappedDialog.newInstance(newTappedBeers);
                    f.show(getFragmentManager(), TAG);
                }

                Collections.sort(mDisplayedList, new BeerSort());

                // Lets refresh the adapter
                mAdapter.clear();
                mAdapter.addAll(mDisplayedList);
                mAdapter.notifyDataSetChanged();
            }
            else {
                Frisco.toast(getActivity(), "Failed to connect to friscogrille.com");
            }

            mProgress.dismiss();
        }
    }

    private class SaveBeerListTask
            extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Only save to the database if there are beers being displayed.
            // This should be a very rare occurance, but we don't want to write
            // zero beers into the database.
            if(mDisplayedList.size() > 0) {
                // Open the database and delete the old entries
                mDataSource.open();
                mDataSource.clearTable();

                // Add each beer from the draft list to the database
                for(int i = 0; i < mDisplayedList.size(); i++) {
                    Beer b = mDisplayedList.get(i);
                    mDataSource.insertBeer(b);
                }

                mDataSource.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }

    /**************************************************************************
     *
     * Implementation of SearchView on text query listeners
     *
     */

    @Override
    public boolean onQueryTextSubmit(String query) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Filter the list based on the new text
        mAdapter.getFilter().filter(newText);

        return true;
    }
}
