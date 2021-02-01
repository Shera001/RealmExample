package com.example.realmexample.db;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmDatabase extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("users.realm")
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(config);
    }
}
