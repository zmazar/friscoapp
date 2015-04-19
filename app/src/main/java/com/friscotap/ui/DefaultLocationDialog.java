package com.friscotap.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.friscotap.core.Frisco;
import com.friscotap.core.R;

public class DefaultLocationDialog extends DialogFragment {
    private int mCurrentSelection;
    private SharedPreferences mPrefs;

    public static final DefaultLocationDialog newInstance() {
        DefaultLocationDialog f = new DefaultLocationDialog();

        return f;
    }

    public DefaultLocationDialog() {
        // Required empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize variables
        mCurrentSelection = -1;

        // Setup the preferences in order to save the default location
        mPrefs = getActivity().getSharedPreferences(Frisco.PREFS_APP, Context.MODE_PRIVATE);
        int storedLoc = mPrefs.getInt(Frisco.PREFS_DEFAULT_LOCATION, -1);

        if(storedLoc == Frisco.LOCATION_CROFTON) {
            mCurrentSelection = 1;
        }
        else {
            mCurrentSelection = 0;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity()
                .getLayoutInflater()
                .inflate(R.layout.layout_default_location, null);

        // Now build the dialog box to be displayed
        AlertDialog.Builder dialogBuilder =
                new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);

        // Build the dialog box
        dialogBuilder
                .setTitle("Set Default Location")
                .setSingleChoiceItems(Frisco.defaultLocations, mCurrentSelection, new ChoiceClick())
                .setPositiveButton("Ok", new NotifyOk())
                .setView(v);

        return dialogBuilder.create();
    }

    private class NotifyOk implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    private class ChoiceClick implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int position) {
            int defaultLocation = -1;

            // Determine what value to store
            switch(position) {
                case 0:
                    defaultLocation = Frisco.LOCATION_COLUMBIA;
                    break;
                case 1:
                    defaultLocation = Frisco.LOCATION_CROFTON;
                    break;
            }

            // When the default location is chosen the position indicates
            // which option was clicked.  Save it in the preferences.
            if(mPrefs != null) {
                mPrefs.edit()
                        .putInt(Frisco.PREFS_DEFAULT_LOCATION, defaultLocation)
                        .commit();
            }
        }
    }
}
