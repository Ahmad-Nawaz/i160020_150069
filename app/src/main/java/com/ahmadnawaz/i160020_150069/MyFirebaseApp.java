

package com.ahmadnawaz.i160020_150069;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class MyFirebaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
