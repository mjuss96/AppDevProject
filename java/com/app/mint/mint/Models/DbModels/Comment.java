package com.app.mint.mint.Models.DbModels;

import com.google.firebase.firestore.Exclude;

/*
    POJO CLASS for Comment, matching content of Comments collection documents in database

 */

public class Comment extends Model {

    public static final String COLLECTION_NAME = "comments";
    public static final String TITLE_FIELD_NAME = "title";
    public static final String TEXT_FIELD_NAME = "text";
    public static final String USER_ID_FIELD_NAME =  "user_id";

    // basically same fields as in database, and getters and setters for each.

    private String title;
    private String text;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Exclude
    public String getId(){
        return super.id;
    }
}