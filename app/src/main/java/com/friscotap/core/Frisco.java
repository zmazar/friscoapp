package com.friscotap.core;

import android.app.Activity;
import android.widget.Toast;

public class Frisco {
    public static final String SixteenOunceNameQuery = ".row > .sixteenounce > .name";
    public static final String SixteenOunceAbvQuery = ".row > .sixteenounce > .abv";
    public static final String TenOunceNameQuery = ".row > .tenounce > .name";
    public static final String TenOunceAbvQuery = ".row > .tenounce > .abv";
    public static final String FeaturedNameQuery = ".row > .eightounce > .name";
    public static final String FeaturedAbvQuery = ".row > .eightounce > .abv";

    public static final String ColumbiaUrl = "http://www.friscogrille.com/cmobile-alt.php";
    public static final String CroftonUrl = "http://www.friscogrille.com/wcmobile-alt.php";

    public static final int LOCATION_COLUMBIA = 0x0;
    public static final int LOCATION_CROFTON = 0x1;
    public static final String[] defaultLocations = new String[] {
            "Columbia",
            "Crofton"
    };

    // Preferences
    public static final String PREFS_APP = "com.friscotaphouse";
    public static final String PREFS_DEFAULT_LOCATION = "prefs-default-loc";
    public static final String PREFS_FONT_SIZE = "prefs-font-size";

    // Location
    public static final int REQUEST_CONNECTION_RESULT = 0x10000001;
    public static final double FRISCO_RADIUS = 1609.34;
    public static final double LAT_COLUMBIA = 39.186012;
    public static final double LNG_COLUMBIA = -76.825151;
    public static final double LAT_CROFTON = 39.036523;
    public static final double LNG_CROFTON = -76.680798;

    // Font sizes
    public static final int FONT_SIZE_DEFAULT = 7;
    public static final int FONT_SIZE_MAX = 12;
    public static final int FONT_SIZE_MIN = 5;

    public static void toast(Activity activity, String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }
}
