package com.friscotap.mugclub;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.friscotap.core.R;

public class MugClubActivity
        extends FragmentActivity
{
    private static final String TAG = "MugClub Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_mugclub);

        // Setup the action bar
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        // Launch the MugClubBook fragment to display the list of
        // beers already in the book.
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.mugclub_container, new MugClubFragment())
                .addToBackStack(null)
                .commit();
    }
}
