package com.app.mint.mint.Models.DbModels;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

/*
    Base class for every database based object
 */

@IgnoreExtraProperties
public class Model {
    @Exclude
    protected String id;
    @Exclude
    protected DocumentReference selfReference;

    public <T extends Model> T withId(@NonNull final String id) {
        this.id = id;
        return (T) this;
    }
}
