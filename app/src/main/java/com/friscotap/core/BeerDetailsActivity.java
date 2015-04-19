package com.friscotap.core;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.friscotap.mugclub.MugClubBeer;

public class BeerDetailsActivity extends Activity {
    private static final String TAG = "Frisco BeerDetails";
    private Beer mBeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_beer_details);

        // Show the Up button in the action bar.
        setupActionBar();

        // Get the specific beer
        Intent intent = getIntent();
        mBeer = (Beer) intent.getSerializableExtra("beer");

        // Set the title of the activity to the beer name
        setTitle(mBeer.getName());

        Log.d(TAG, "Beer: " + mBeer);

        TextView text1 = (TextView) findViewById(R.id.beer_name);
        TextView text2 = (TextView) findViewById(R.id.beer_pour_size);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.beer_rating);
        CheckBox checkBox = (CheckBox) findViewById(R.id.mugclub_add_beer);

        text1.setText(mBeer.getName());
        text1.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        text2.setTextSize(TypedValue.COMPLEX_UNIT_PT, 8);
        text2.setText("" + mBeer.getOunces() + " oz pour");

        // Implement rating bar
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser)
            {
                // Save rating to database based on Frisco ID
                Log.d(TAG, "Rated " + rating + " stars");
            }
        });

        // Setup the checkbox for mug club book
        BeerDataSource mSource = new BeerDataSource(this, BeerDbHelper.TABLE_CLUB, mBeer.getFriscoId());
        MugClubBeer mMug;

        mSource.open();
        mMug = mSource.getMugBeer(mBeer.getName());
        mSource.close();

        if(mMug != null) {
            checkBox.setChecked(true);

            if(mMug.isConfirmed() == true) {
                checkBox.setClickable(false);
            }
        }

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, FriscoMain.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddToMugClub(View v) {
        BeerDataSource mSource =
                new BeerDataSource(
                        getApplicationContext()
                );
        MugClubBeer newMugBeer = new MugClubBeer(mBeer);
        long result = -1;

        mSource.open();

        // Get the state of the check box, by the time we get here the state
        // has been changed.  We check the opposite of what you'd think.
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.mugclub_add_beer);

        if(checkBox.isChecked() == false) {
            // If the beer isn't confirmed, remove it from the database
            MugClubBeer mMug;
            mMug = mSource.getMugBeer(mBeer.getName());

            if(mMug != null) {
                if(mMug.isConfirmed() == false) {
                    mSource.deleteMugBeer(mMug);
                }
            }
        }
        else {
            // Add the beer to the mug club book
            result = mSource.insertMugBeer(newMugBeer);

            if(result == -1) {
                // Display alert that beer is already in the mug club book
                Log.d(TAG, "Failed to insert a beer into the book.");
            }
        }

        mSource.close();
    }
}
