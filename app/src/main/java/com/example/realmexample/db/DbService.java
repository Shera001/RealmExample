package com.example.realmexample.db;

import android.util.Log;

import com.example.realmexample.model.UserModel;

import java.util.List;

import io.realm.Realm;

public class DbService {

    private static final Realm realm = Realm.getDefaultInstance();

    private static final Class<UserModel> modelClass = UserModel.class;

    public static void insert(UserModel model) {

        Number currentId = realm.where(modelClass).max("id");

        int id;

        if (currentId == null) {
            id = 1;
        }
        else {
            id = currentId.intValue() + 1;
        }

        model.setId(id);

        realm.executeTransaction(realm -> realm.copyToRealm(model));
    }

    public static List<UserModel> getAll() {
        return realm.where(modelClass).findAll();
    }

    public static void update(int id, String name, String phone, byte[] image) {
        UserModel model = realm.where(modelClass).equalTo("id", id).findFirst();

        realm.executeTransaction(realm -> {
            Log.d("TAG", "id: " + id);
            Log.d("TAG", "Phone: " + phone);
            Log.d("TAG", "Name: " + name);
            assert model != null;
            model.setName(name);
            model.setPhone(phone);
            model.setImage(image);

            realm.copyToRealmOrUpdate(model);
        });
    }

    public static void delete(int id) {
        UserModel model = realm.where(modelClass).equalTo("id", id).findFirst();

        realm.executeTransaction(realm -> model.deleteFromRealm());
    }

    public static void deleteAll() {
        realm.executeTransaction(realm -> realm.deleteAll());
    }
}
