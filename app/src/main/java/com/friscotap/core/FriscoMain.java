package com.friscotap.core;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.friscotap.mugclub.MugClubActivity;
import com.friscotap.notification.NotificationReceiver;
import com.friscotap.ui.DefaultLocationDialog;
import com.friscotap.ui.NavDrawerAdapter;
import com.friscotap.ui.NavDrawerItem;
import com.friscotap.ui.SettingsActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class FriscoMain
        extends FragmentActivity
        implements DialogInterface.OnCancelListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "Frisco Main";

    // BeerList class variables
    private BeerPageAdapter mPageAdapter;
    private ViewPager mPager;
    private List<Fragment> mPagerFragments;

    // Notification class variables
    private NotificationReceiver mTappedNotify;

    // Drawer variables
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ArrayList<NavDrawerItem> mMenus;
    private int mDrawerSelect;

    // Location related variables
    private GoogleApiClient mLocClient;

    // Preferences
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // Set the view
        setContentView(R.layout.activity_frisco_main);


        // Set the pager view
        mPager = (ViewPager) findViewById(R.id.beer_pager);

        // Setup the Navigation drawer
        setupNavigationInterface(savedInstanceState);
        mDrawerSelect = -1;

        // Setup the initial beer list based on location
        setupInitialConfigs();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Frisco.REQUEST_CONNECTION_RESULT:

                switch(resultCode) {
                    case Activity.RESULT_OK:
                        mLocClient.connect();
                } // End switch(resultCode)
        } // End switch(requestCode)
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action buttons
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        // Startup the alarm
        startNotificationReceiver();

        // Connect to Google Play Services
        mLocClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect from Google Play Services.  This is important so that the
        // location and GPS aren't continued on stoppage of the app.  This
        // would ensure the app won't drain the battery.
        mLocClient.disconnect();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = true;
        int fontSize = 7;
        int newFontSize = 0;

        try {
            switch(keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if(mPrefs != null) {
                        fontSize = mPrefs.getInt(Frisco.PREFS_FONT_SIZE, 7);
                        newFontSize = fontSize - 1;

                        // Make sure that the font size keeps within a reasonable
                        // font size.
                        if(newFontSize >= Frisco.FONT_SIZE_MIN &&
                                newFontSize <= Frisco.FONT_SIZE_MAX)
                        {
                            mPrefs.edit().putInt(Frisco.PREFS_FONT_SIZE, newFontSize).commit();

                            // The beer list fragments need to be refreshed for the
                            // new font sizes to take effect.
                            for(int i = 0; i < mPagerFragments.size(); i++) {
                                BeerListFragment f = (BeerListFragment) mPagerFragments.get(i);

                                if(f != null) {
                                    f.refreshListAdapter();
                                }
                            }

                            // Reset newFontSize just to ensure no other corruption
                            // of the font size setting occurs.
                            newFontSize = 0;
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if(mPrefs != null) {
                        fontSize = mPrefs.getInt(Frisco.PREFS_FONT_SIZE, 7);
                        newFontSize = fontSize + 1;

                        // Make sure that the font size keeps within a reasonable
                        // font size.
                        if(newFontSize >= Frisco.FONT_SIZE_MIN &&
                                newFontSize <= Frisco.FONT_SIZE_MAX)
                        {
                            mPrefs.edit().putInt(Frisco.PREFS_FONT_SIZE, newFontSize).commit();

                            // The beer list fragments need to be refreshed for the
                            // new font sizes to take effect.
                            for(int i = 0; i < mPagerFragments.size(); i++) {
                                BeerListFragment f = (BeerListFragment) mPagerFragments.get(i);

                                if(f != null) {
                                    f.refreshListAdapter();
                                }
                            }

                            // Reset newFontSize just to ensure no other corruption
                            // of the font size setting occurs.
                            newFontSize = 0;
                        }
                    }
                    break;
                default:
                    ret = super.onKeyDown(keyCode, event);
            }
        }
        catch(ClassCastException e) {
            Log.d(TAG, e.toString());
        }

        return ret;
    }

    private List<Fragment> getColumbiaFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        BeerListFragment f;

        // Grab all the 16 oz beers
        f = BeerListFragment.newInstance(
                BeerDbHelper.TABLE_COLUMBIA,
                Frisco.ColumbiaUrl
        );
        fList.add(f);

        return fList;
    }

    private List<Fragment> getCroftonFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();
        BeerListFragment f;

        // Grab all the 16 oz beers
        f = BeerListFragment.newInstance(
                BeerDbHelper.TABLE_COLUMBIA,
                Frisco.CroftonUrl
        );
        fList.add(f);

        return fList;
    }

    private int getDefaultLocation() {
        int defaultLocation = Frisco.LOCATION_COLUMBIA;

        // Setup the initial fragment based on the default location set by
        // the user.
        mPrefs = getSharedPreferences(Frisco.PREFS_APP, Context.MODE_PRIVATE);

        if(mPrefs != null) {
            defaultLocation = mPrefs.getInt(Frisco.PREFS_DEFAULT_LOCATION, -1);

            if(defaultLocation == -1) {
                defaultLocation = Frisco.LOCATION_COLUMBIA;
            }
        }

        Log.d(TAG, "returning " + defaultLocation);

        return defaultLocation;
    }

    private void handleMenuSelection(int menuSelect) {
        // Close the drawer right away
        mDrawerLayout.closeDrawer(mDrawerList);

        // Only switch menus if we need to
        if(menuSelect != mDrawerSelect) {
            switch(menuSelect) {
                case 0:
                    // Setup the fragments for Columbia
                    mPagerFragments = getColumbiaFragments();

                    if(mPagerFragments != null) {
                        // Setup the fragments for the listviews and the page adapter
                        mPageAdapter = new BeerPageAdapter(getSupportFragmentManager(), mPagerFragments);
                        mPageAdapter.notifyDataSetChanged();
                        mPager.setAdapter(mPageAdapter);
                    }

                    getActionBar().setSubtitle("Columbia");
                    break;
                case 1:
                    // Setup the fragments based on location
                    mPagerFragments = getCroftonFragments();

                    if(mPagerFragments != null) {
                        // Setup the fragments for the listviews and the page adapter
                        mPageAdapter = new BeerPageAdapter(getSupportFragmentManager(), mPagerFragments);
                        mPageAdapter.notifyDataSetChanged();
                        mPager.setAdapter(mPageAdapter);
                    }

                    getActionBar().setSubtitle("Crofton");
                    break;
                case 2:
                    Frisco.toast(this, "Display Mug Club Book");
                    Intent clubIntent =
                            new Intent(this, MugClubActivity.class);

                    clubIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(clubIntent);

                    // Restore the menu select
                    menuSelect = mDrawerSelect;
                    break;
                case 3:
                    Frisco.toast(this, "Display My Rated Beers");
                    break;
                case 4:
                    // Show the default location selection dialog
                    DefaultLocationDialog f = DefaultLocationDialog.newInstance();
                    f.show(getFragmentManager(), TAG);

                    // Restore the menu select
                    menuSelect = mDrawerSelect;
                    break;
                case 5:
                    Intent settingsIntent =
                            new Intent(this, SettingsActivity.class);

                    settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(settingsIntent);

                    // Restore the menu select
                    menuSelect = mDrawerSelect;
                    break;
                case 6:
                    Frisco.toast(this, "Display About Frisco Taphouse");
                    break;
                default:
                    Frisco.toast(this, "Pressed: " + menuSelect);
            }

            // Set the new menu to be displayed
            mDrawerSelect = menuSelect;
        }
    }

    public void setupInitialConfigs() {
        int gpStatus = -1;

        // Set the initial value of the font size to be a default 7.
        mPrefs = getSharedPreferences(Frisco.PREFS_APP, Context.MODE_PRIVATE);

        if(mPrefs != null && !mPrefs.contains(Frisco.PREFS_FONT_SIZE)) {
            mPrefs.edit().putInt(Frisco.PREFS_FONT_SIZE, 7).commit();
        }

        // Ensure that Google Play Services is installed
        gpStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(gpStatus == ConnectionResult.SUCCESS) {
            // Grab the current location if Google Play Services was
            // successfully found on the device.
            mLocClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        else {
            // Show the error dialog returned.  This activity contains the
            // onCancel callback.
            Dialog d = GooglePlayServicesUtil.getErrorDialog(gpStatus, this, 0, this);
            d.show();
        }

        // Initialize our alarm for notifications
        //mTappedNotify = new NotificationReceiver();
    }

    public void setupNavigationInterface(Bundle savedInstanceState) {
        mTitle = mDrawerTitle = getTitle();
        mMenus = new ArrayList<NavDrawerItem>();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Setup the list of menu items
        mMenus.	add(new NavDrawerItem("Columbia", true, false));
        mMenus.add(new NavDrawerItem("Crofton", true, false));
        mMenus.add(new NavDrawerItem("Mug Club Book", true, false));
        mMenus.add(new NavDrawerItem("My Rated Beers", false, false));
        mMenus.add(new NavDrawerItem("Default Location", false, true));
        //mMenus.add(new NavDrawerItem("Settings", true, true));
        //mMenus.add(new NavDrawerItem("About Frisco", false, true));

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(
                R.drawable.drawer_shadow,
                GravityCompat.START
        );

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new NavDrawerAdapter(this,
                R.layout.drawer_item, mMenus));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void startNotificationReceiver() {
        Context context = getApplicationContext();

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
            // Don't want to do anything
            Log.d(TAG, "Alarm already active");
        }
        else {
            if(mTappedNotify != null) {
                Log.d(TAG, "Set alarm");
                mTappedNotify.setAlarm(context);
            }
            else {
                Log.d(TAG, "Failed to initialize the alarm");
            }
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            handleMenuSelection(position);
        }
    }

    /**
     * The onCancel listener in case the Google Play Services error dialog is
     * cancelled.  It will only set the initial beer list to be displayed.
     *
     * Since the location via Google Play Services has failed, we'll just use
     * the default location stored in the preferences.
     */
    @Override
    public void onCancel(DialogInterface dialog) {
        handleMenuSelection(getDefaultLocation());
        dialog.dismiss();
    }

    /**
     * Implementation of both onConnectionFailed and ConnectionCallback
     * interfaces.
     *
     * These are used specifically for the LocationClient to ensure that a
     * current location for the device can be found.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects.
        // If the error has a resolution, try sending an Intent to
        // start a Google Play services activity that can resolve
        // error.
        if(connectionResult.hasResolution()) {
            try {
                // Thrown if Google Play services canceled the original
                // PendingIntent

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        Frisco.REQUEST_CONNECTION_RESULT
                );
            }
            catch(IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        }
        else {
            // TODO: If no resolution is available, display a dialog to the
            // user with the error.
            Frisco.toast(this, "SUPER FAIL Google Play Services");
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Alright, lets update the location each time we connect to Google
        // Play services, and center the map view with it.
        if(mLocClient.isConnected()) {
            Log.d(TAG, "Connected to Google Play Services");

            double distToColumbia = 0.0;
            double distToCrofton= 0.0;
            Location currLoc = LocationServices.FusedLocationApi.getLastLocation(mLocClient);
            Location croftonLoc = new Location("");
            Location columbiaLoc = new Location("");

            // Set the latitude and longitude for each Frisco location
            columbiaLoc.setLatitude(Frisco.LAT_COLUMBIA);
            columbiaLoc.setLongitude(Frisco.LNG_COLUMBIA);
            croftonLoc.setLatitude(Frisco.LAT_CROFTON);
            croftonLoc.setLongitude(Frisco.LNG_CROFTON);

            // Calculate if we're within a specific Frisco Location.
            distToColumbia = currLoc.distanceTo(columbiaLoc);
            distToCrofton = currLoc.distanceTo(croftonLoc);

            // Determine which location (if any) the device is at.
            if(distToColumbia < Frisco.FRISCO_RADIUS) {
                Frisco.toast(this, "Location: Columbia\n");
                handleMenuSelection(Frisco.LOCATION_COLUMBIA);
            }
            else if(distToCrofton < Frisco.FRISCO_RADIUS) {
                Frisco.toast(this, "Location: Crofton\n");
                handleMenuSelection(Frisco.LOCATION_CROFTON);
            }
            else {
                handleMenuSelection(getDefaultLocation());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }
}
