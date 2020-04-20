package com.app.mint.mint.Models.DbModels;

import android.content.Context;

import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.CompaniesDataModel;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/*
    POJO CLASS for Company, matching content of companies collection documents in database

 */

public class Company extends Model {

    // field and collection names
    public static final String COLLECTION_NAME = "companies";
    public static final String COMMENTS_COLLECTION_NAME = "comments";
    public static final String NAME_FIELD_NAME = "name";
    public static final String ADDRESS_FIELD_NAME = "address";
    public static final String LATITUDE_FIELD_NAME = "latitude";
    public static final String LONGITUDE_FIELD_NAME = "longitude";
    public static final String CATEGORIES_ARRAY_NAME = "address";
    public static final String NAME_TAGS_ARRAY_NAME = "name_tags";
    public static final String ACTIVE_DEALS_FIELD_NAME = "active_deals";
    public static final String NUM_OF_RATINGS_FIELD_NAME = "num_of_ratings";
    public static final String RATING_AVG_FIELD_NAME = "rating_avg";
    public static final String PRICE_LIST_FIELD_NAME = "price_list";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    // all possible categories
    public static final String CATEGORY_HAIR = "hair";
    public static final String CATEGORY_NAILS = "nails";
    public static final String CATEGORY_MAKEUP = "makeup";
    public static final String CATEGORY_PEDICURE = "pedicure";
    public static final String CATEGORY_MANICURE = "manicure";
    public static final String CATEGORY_FACIAL = "facial";
    public static final String CATEGORY_SOLARIUM = "solarium";
    public static final String CATEGORY_EYELASHES = "eyelashes";
    public static final String CATEGORY_EYEBROWS = "eyebrows";


    public static final CollectionReference companyCollectionRef = MintApplication.getDatabaseRef().collection(COLLECTION_NAME);


    private String name;
    private String address;
    private List<String> name_tags = new ArrayList<String>();
    private List<String> categories = new ArrayList<String>();
    private String price_list;
    private String description;

    @Exclude
    private CollectionReference commentsCollectionRef;

    private boolean active_deals;
    private double latitude;
    private double longitude;
    private float rating_avg;
    private int num_of_ratings;


    public Company(String name, String address, List<String> categories, double latitude, double longitude) {
        this.name = name;
        String[] nameTagsArray = name.toLowerCase().split(" ");

        for (String nameTag : nameTagsArray) {
            this.name_tags.add(nameTag);

        }
        this.address = address;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;

    }


    // FireStore requires empty public constructor
    public Company() {

    }


    // FireStore also requires getters and setters for each fields

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getName_tags() {
        return name_tags;
    }

    public void setName_tags(List<String> name_tags) {
        this.name_tags = name_tags;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    // for filtering etc. not for UI use
    public List<String> getCategories() {
        return categories;
    }

    // has to be named isActive_deals, even tho hasActive_deals is better name
    // because of FireStore SDK won't find this if named differently
    public boolean isActive_deals() {
        return active_deals;
    }

    public void setActive_deals(boolean active_deals) {
        this.active_deals = active_deals;
    }

    public void setPrice_list(String text) {
        this.price_list = text;
    }

    public String getPrice_list() {
        return price_list;
    }

    public void setDescription(String text) {
        this.description = text;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLatitude_longitude() {
        return Double.toString(latitude) + "_" + Double.toString(longitude);
    }

    public float getRating_avg() {
        return rating_avg;
    }

    public void setRating_avg(float rating_avg) {
        this.rating_avg = rating_avg;
    }

    public int getNum_of_ratings() {
        return num_of_ratings;
    }

    public void setNum_of_ratings(int num_of_ratings) {
        this.num_of_ratings = num_of_ratings;
    }


    // for UI elements, will return categories on users language
    @Exclude
    public List<String> getLocalizedCategories(Context context) {
        List<String> localizedCategories = new ArrayList<>();

        if (categories.contains(CATEGORY_EYEBROWS))
            localizedCategories.add(context.getString(R.string.eyebrows));
        if (categories.contains(CATEGORY_EYELASHES))
            localizedCategories.add(context.getString(R.string.eyelashes));
        if (categories.contains(CATEGORY_FACIAL))
            localizedCategories.add(context.getString(R.string.facial));
        if (categories.contains(CATEGORY_HAIR))
            localizedCategories.add(context.getString(R.string.hair));
        if (categories.contains(CATEGORY_MAKEUP))
            localizedCategories.add(context.getString(R.string.makeup));
        if (categories.contains(CATEGORY_MANICURE))
            localizedCategories.add(context.getString(R.string.manicure));
        if (categories.contains(CATEGORY_NAILS))
            localizedCategories.add(context.getString(R.string.nails));
        if (categories.contains(CATEGORY_PEDICURE))
            localizedCategories.add(context.getString(R.string.pedicure));
        if (categories.contains(CATEGORY_SOLARIUM))
            localizedCategories.add(context.getString(R.string.solarium));
        return localizedCategories;
    }

    @Exclude
    public CollectionReference getCommentsCollectionRef() {
        if (super.id != null) {
            super.selfReference = companyCollectionRef.document(super.id);
            commentsCollectionRef = super.selfReference.collection(COMMENTS_COLLECTION_NAME);
        }
        return commentsCollectionRef;
    }

    // calculating distance to user
    public double distanceToUser(double userLatitude, double userLongitude) {

        // (a*companyLat - a*userLat)^2 + (b*companyLong - b*userLong)^2 = distance^2

        double distance;
        final double x = CompaniesDataModel.ONE_DEGREE_IN_KM;
        double companyLat = this.latitude;
        double companyLong = this.longitude;

        double a = x;
        double b = Math.cos(Math.toRadians(userLatitude)) * x;

        double firstTerm = Math.pow((a * companyLat - a * userLatitude), 2);
        double secondTerm = Math.pow((b * companyLong - b * userLongitude), 2);

        distance = Math.sqrt(firstTerm + secondTerm);


        return distance;
    }

    // function to calculate "rankingValue"
    // will be used to sort companies in list
    // currently 50% of distance, 40% of rating, and 10% of activeDeals
    // 0-1000 value
    public double getRankingValue(UserDataModel userData, double maxDistance) {
        double rankingValue = 0.0;
        GeoPoint userGeopoint = userData.getUserGeoPoint().getValue();
        double distanceMultiplier = this.distanceToUser(userGeopoint.getLatitude(), userGeopoint.getLongitude()) / maxDistance;
        rankingValue = (1 - distanceMultiplier) * 500;

        double a = this.num_of_ratings / 10;
        if (a > 100) a = 1;

        rankingValue += a * this.getRating_avg() * 80;
        if (this.isActive_deals()) rankingValue += 100;

        if (rankingValue > 1000) rankingValue = 1000;
        if (rankingValue < 0) rankingValue = 0;
        return rankingValue;
    }


    // getter function for databaseID, returns value from base class.
    @Exclude
    public String getId() {
        return super.id;
    }

    // similar for selfReference
    @Exclude
    public DocumentReference getSelfReference() {
        DocumentReference reference = null;
        if (super.selfReference == null) {
            if (super.id != null) {
                reference = Company.companyCollectionRef.document(super.id);
                super.selfReference = reference;
            }
        } else {
            reference = super.selfReference;
        }
        return reference;
    }


}
