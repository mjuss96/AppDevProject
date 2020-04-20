package com.app.mint.mint.Models.DbModels;

import com.google.firebase.firestore.Exclude;

/*
    Cooldown for Commenting
 */

public class CommentCooldown extends Cooldown {


    long timestamp = 0;
    String company_id;

    @Override
    public String getType() {
        return COOLDOWN_TYPE_COMMENT;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    @Override
    public String getId() {
        return super.id;
    }

    @Override
    public String getCompany_id() {
        return company_id;
    }

    @Override
    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

}
