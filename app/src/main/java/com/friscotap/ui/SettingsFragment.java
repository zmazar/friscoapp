package com.friscotap.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.friscotap.core.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("PreferenceFragment", "onCreate()");

        // Load the preferences.xml
        addPreferencesFromResource(R.xml.preferences);
    }
}
