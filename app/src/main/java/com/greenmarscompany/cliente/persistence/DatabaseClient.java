package com.greenmarscompany.cliente.persistence;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.room.Room;

public class DatabaseClient {
    @SuppressLint("StaticFieldLeak")
    private static DatabaseClient miInstance;
    private AppDatabase appDatabase;
    private Context ctx;

    private DatabaseClient(Context context) {
        this.ctx = context.getApplicationContext();
        appDatabase = Room.databaseBuilder(this.ctx, AppDatabase.class, "freebusiness")
                .enableMultiInstanceInvalidation()
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized DatabaseClient getInstance(Context context) {
        if (miInstance == null) {
            miInstance = new DatabaseClient(context);
        }
        return miInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
