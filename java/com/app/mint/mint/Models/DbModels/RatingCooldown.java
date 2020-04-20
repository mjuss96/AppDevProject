package com.app.mint.mint.Models.DbModels;

import com.app.mint.mint.Models.DbModels.Cooldown;
import com.google.firebase.firestore.Exclude;

/*
    Cooldown for Rating
 */
public class RatingCooldown extends Cooldown {

    long timestamp = 1000;

    String company_id;

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getType() {
        return COOLDOWN_TYPE_RATING;
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
