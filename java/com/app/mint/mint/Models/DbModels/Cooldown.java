package com.app.mint.mint.Models.DbModels;

import java.util.Map;

/*
    POJO Class for CoolDowns, can be added and fetched from database
 */

public abstract class Cooldown extends Model {

    public static final String COMPANY_ID_FIELD_KEY = "company_id";
    public static final String TIMESTAMP_FIELD_KEY = "timestamp";
    public static final String TYPE_FIELD_KEY = "type";

    public static final String COOLDOWN_TYPE_RATING = "RATING";
    public static final String COOLDOWN_TYPE_COMMENT = "COMMENT";

    // default Cooldown for commenting and numOfRatings
    // 30days in milliseconds =
    // 1000ms * 60s * 60min * 24h * 30days
    public static final long DEFAULT_COOL_DOWN = 1000 * 60 * 60 * 24 * 30;


    /*
        classes have to implement these functions
     */
    abstract public long getTimestamp();

    abstract public void setTimestamp(long timestamp);

    abstract public String getId();

    abstract public String getType();

    abstract public String getCompany_id();

    abstract public void setCompany_id(String company_id);


    /*
        Database will return CoolDowns as a Map,
        this function will convert them to Cooldown object
     */
    public static Cooldown mapToCooldown(Map<String, Object> coolDownMap) {
        Cooldown cooldownResult;

        if (coolDownMap.get(TYPE_FIELD_KEY).equals(COOLDOWN_TYPE_COMMENT)) {
            cooldownResult = new CommentCooldown();
        } else {
            cooldownResult = new RatingCooldown();
        }
        String companyId = (String) coolDownMap.get(COMPANY_ID_FIELD_KEY);
        long timestamp = (long) coolDownMap.get(TIMESTAMP_FIELD_KEY);
        cooldownResult.setCompany_id(companyId);
        cooldownResult.setTimestamp(timestamp);

        return cooldownResult;
    }
}
