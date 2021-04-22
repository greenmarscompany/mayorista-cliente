package com.greenmarscompany.cliente.utils;

import android.app.Activity;

public class VersionHelper {
    public static void refreshActionBarMenu(Activity activity) {
        activity.invalidateOptionsMenu();
    }
}
