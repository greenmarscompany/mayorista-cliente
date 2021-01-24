package com.greenmarscompany.mayoristacliente.utils;

import android.app.Activity;

public class VersionHelper {
    public static void refreshActionBarMenu(Activity activity) {
        activity.invalidateOptionsMenu();
    }
}
