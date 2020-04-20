package com.app.mint.mint.Main;

import android.app.Application;
import android.content.Context;

import com.app.mint.mint.Models.UserDataModel;
import com.google.firebase.firestore.FirebaseFirestore;

/*
    CUSTOM APPLICATION CLASS
 */

public class MintApplication extends Application {

    // Storing important data here.
    // Data that will be used in multiple activities
    private UserDataModel userData;
    private static FirebaseFirestore database;
    private static Context appContext;


    @Override
    public void onCreate() {
        appContext = this;
        userData = UserDataModel.getInstance();
        userData.setPreferences(this.getSharedPreferences(UserDataModel.SHARED_PREF_KEY, Context.MODE_PRIVATE));

        super.onCreate();
    }

    public static Context getAppContext() {
        return appContext;
    }

    public UserDataModel getUserData() {
        if(userData == null){
            userData = UserDataModel.getInstance();
            userData.setPreferences(this.getSharedPreferences(UserDataModel.SHARED_PREF_KEY, Context.MODE_PRIVATE));
        }
        return userData;
    }

    public static FirebaseFirestore getDatabaseRef(){
        if(database == null){
            database = FirebaseFirestore.getInstance();
        }
        return  database;
    }
}
