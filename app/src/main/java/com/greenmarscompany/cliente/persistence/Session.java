package com.greenmarscompany.cliente.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {

    private final SharedPreferences preferences;

    public Session(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setToken(int token) {
        preferences.edit().putInt("IdUsuario", token).apply();
    }

    public int getToken() {
        return preferences.getInt("IdUsuario", 0);
    }

    public void destroySession() {
        preferences.edit().putInt("IdUsuario", 0).apply();
    }
}