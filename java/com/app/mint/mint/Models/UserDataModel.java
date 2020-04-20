package com.app.mint.mint.Models;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.DbModels.Comment;
import com.app.mint.mint.Models.DbModels.CommentCooldown;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.Models.DbModels.Cooldown;
import com.app.mint.mint.Models.DbModels.RatingCooldown;
import com.app.mint.mint.Models.DbModels.UserInDb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDataModel {
    public static final CollectionReference usersCollectionReference = MintApplication.getDatabaseRef().collection("users");

    public static final String SHARED_PREF_KEY = "UserDataModel";
    private final String FIRST_NAME_PREF_KEY = "UserFirstName";
    private final String LAST_NAME_PREF_KEY = "UserLastName";
    private final String LOCATION_PREF_KEY = "UserLocation";
    private final String PHONE_NUMBER_PREF_KEY = "UserPhoneNumber";
    private final String ONBOARDING_SHOWN_PREF_KEY = "OnBoardingShown";
    private final String DEFAULT_LOCATION = "Oulu, Finland";

    private FirebaseAuth userAuthentication = FirebaseAuth.getInstance();
    private MutableLiveData<GeoPoint> userGeoPoint = new MutableLiveData<>();
    private MutableLiveData<List<Company>> followedCompanies = new MutableLiveData<>();


    private Context applicationContext = MintApplication.getAppContext();
    private SharedPreferences preferences;
    private boolean userIsLoggedIn = false;
    private String userFirstName;
    private String userLastName;
    private String userLocation;
    private String userExactLocation;
    private String userPhoneNumber;
    boolean onBoardingShown = false;

    static UserDataModel instance;
    private UserInDb userInDb;


    public static UserDataModel getInstance() {
        if (instance == null) {
            instance = new UserDataModel();
        }
        return instance;
    }

    private UserDataModel() {
        followedCompanies.setValue(new ArrayList<Company>());
        userAuthentication.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    notifyUserLoggedIn();
                } else {

                }
            }
        });

    }

    public LiveData<List<Company>> getFollowedCompaniesLiveData(){
        return followedCompanies;
    }

    private void userDataFetched() {

        // getting users followed companies
        for (String companyId : getFollowedCompaniesIds()) {
            Company.companyCollectionRef.document(companyId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                Company company = document.toObject(Company.class).withId(document.getId());
                                List<Company> companies = followedCompanies.getValue();
                                companies.add(company);
                                Log.d("COMPANIES", "followed " + Integer.toString(companies.size()));
                                followedCompanies.setValue(companies);
                            }
                        }
                    });
        }
    }


    /*
        DATABASE FUNCTIONS
     */
    public boolean isFollowingCompany(String companyId) {
        if (!isLoggedIn()) return false;
        if (userInDb.getFollowed_companies().contains(companyId)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean followCompany(Company company) {
        if (isLoggedIn()) {
            List<Company> companies = followedCompanies.getValue();
            companies.add(company);
            followedCompanies.setValue(companies);
            Log.d("COMPANIES", "followCompany: " + company.getName());

            userInDb.addFollowedCompany(company.getId());
            return true;
        }
        return false;
    }

    public boolean stopFollowingCompany(Company company) {
        if (isLoggedIn()) {

            List<Company> companies = followedCompanies.getValue();
            for(int i = 0 ; i < companies.size() ; i++){
                if(companies.get(i).getId().equals(company.getId())){
                    companies.remove(i);
                    followedCompanies.setValue(companies);
                    Log.d("COMPANIES", "stopFollowingCompany: " + company.getName());
                    userInDb.removeFollowedCompany(company.getId());
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public List<String> getFollowedCompaniesIds() {
        return userInDb.getFollowed_companies();
    }

    // Adds comment to company and cooldown to user,
    // returns false if not logged in or already has cooldown
    public boolean commentCompany(Company company, Comment comment) {
        if (isLoggedIn() && !hasCooldown(Cooldown.COOLDOWN_TYPE_COMMENT, company.getId())) {
            //adding comment for company
            company.getCommentsCollectionRef().add(comment);
            //adding cooldown for user
            Cooldown cooldown = new CommentCooldown();
            cooldown.setCompany_id(company.getId());
            cooldown.setTimestamp(System.currentTimeMillis() + Cooldown.DEFAULT_COOL_DOWN);
            userInDb.addCooldown(cooldown);
            return true;
        }
        return false;
    }

    public boolean hasCooldown(String type, String companyId) {
        if (isLoggedIn()) {
            List<Cooldown> usersCooldowns = userInDb.getCooldownsList();
            if (usersCooldowns.isEmpty()) {
                return false;
            } else {
                for (Cooldown cooldown : usersCooldowns) {
                    if (cooldown.getCompany_id().equals(companyId)) {
                        //company matches

                        if (cooldown.getType().equals(type)) {
                            //type matches, user has cooldown
                            return true;
                        }
                    }
                }
                return false;
            }
        } else {
            return false;
        }
    }

    public void setCooldown(String type, String companyId) {
        if (!isLoggedIn() || type == null || companyId == null) {
            return;
        }

        Cooldown cooldown;
        if (type == Cooldown.COOLDOWN_TYPE_COMMENT) {
            cooldown = new CommentCooldown();
        } else {
            cooldown = new RatingCooldown();
        }

        cooldown.setCompany_id(companyId);
        cooldown.setTimestamp(System.currentTimeMillis() + Cooldown.DEFAULT_COOL_DOWN);

        userInDb.addCooldown(cooldown);
    }

    public boolean isLoggedIn() {
        return (userAuthentication.getCurrentUser() != null);
    }

    public FirebaseAuth getUserAuthentication() {
        return userAuthentication;
    }






    /*
        LOCAL DATA FUNCTIONS
     */

    // setting preferences, can be done only once.
    // this should be in MintApplication onCreate() after getting the first instance
    public void setPreferences(SharedPreferences preferences) {
        if (this.preferences == null) {
            this.preferences = preferences;
        }
    }

    public LiveData<GeoPoint> getUserGeoPoint() {
        if (userGeoPoint.getValue() == null) {
            String userLocationStr = getUserLocation();
            Address address = locationToAddress(userLocationStr);
            GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
            userGeoPoint.setValue(geoPoint);
        }
        return userGeoPoint;
    }

    public void setUserCoordinates(double userLatitude, double userLongitude) {
        userGeoPoint.setValue(new GeoPoint(userLatitude, userLongitude));

        Geocoder geocoder;
        geocoder = new Geocoder(applicationContext, Locale.getDefault());
        try {
            Address address = geocoder.getFromLocation(userLatitude, userLongitude, 1).get(0);
            if (address != null) {
                String city = address.getLocality();
                String country = address.getCountryName();
                setUserLocation(city + ", " + country);
                addressToExactLocation(address, city);
                // 1

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUserCoordinates(String location) {
        setUserLocation(location);
        Address userAddress = locationToAddress(location);
        GeoPoint geoPoint = new GeoPoint(userAddress.getLatitude(), userAddress.getLongitude());
        userGeoPoint.setValue(geoPoint);
    }

    public void setUserFirstName(String name) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(FIRST_NAME_PREF_KEY, name);
            editor.commit();
        }
        this.userFirstName = name;
    }

    public String getUserFirstName() {
        if (userFirstName == null) {
            if (preferences != null) {
                userFirstName = preferences.getString(FIRST_NAME_PREF_KEY, null);
            }
        }
        return userFirstName;
    }

    public void setUserLastName(String name) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LAST_NAME_PREF_KEY, name);
            editor.commit();
        }
        this.userLastName = name;
    }

    public String getUserLastName() {
        if (userLastName == null) {
            if (preferences != null) {
                userLastName = preferences.getString(LAST_NAME_PREF_KEY, null);
            }
        }
        return userLastName;
    }

    public String getUserLocation() {
        if (userLocation == null) {
            if (preferences != null) {
                userLocation = preferences.getString(LOCATION_PREF_KEY, null);
                if (userLocation == null) {
                    setUserLocation(DEFAULT_LOCATION);
                }
            }
        }

        return userLocation;
    }

    public void setUserPhoneNumber(String number) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PHONE_NUMBER_PREF_KEY, number);
            editor.commit();
        }
        this.userPhoneNumber = number;
    }

    public String getUserPhoneNumber() {
        if (userPhoneNumber == null) {
            if (preferences != null) {
                userPhoneNumber = preferences.getString(PHONE_NUMBER_PREF_KEY, null);
            }
        }
        return userPhoneNumber;
    }

    public void setOnBoardingShown(boolean shown) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ONBOARDING_SHOWN_PREF_KEY, shown);
            editor.commit();
        }
        this.onBoardingShown = shown;
    }

    public boolean isOnBoardingShown() {
        if (preferences != null && onBoardingShown == false) {
            onBoardingShown = preferences.getBoolean(ONBOARDING_SHOWN_PREF_KEY, false);
        }
        return onBoardingShown;
    }

    public String getUserExactLocation() {
        return userExactLocation;
    }

    public void setUserExactLocation(String userExactLocation) {
        Log.d("exactLocation", userExactLocation);
        this.userExactLocation = userExactLocation;
    }


    /*
        LOCATING USER WITH GPS
     */

    // returns false if no permission
    public boolean locateUser(Activity activity) {
        final UserDataModel model = this;
        FusedLocationProviderClient fusedLocationClient;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext);
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                double latitude;
                                double longitude;
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                model.setUserCoordinates(latitude, longitude);
                                Log.d("LOCATION", Double.toString(latitude) + ", " + Double.toString(longitude));
                            }
                        }
                    });
            return true;
        } catch (SecurityException e) {
            Log.d("LOCATION", "no permission");
            return false;
        }
    }


    /*
        PRIVATE FUNCTIONS
     */
    private Address locationToAddress(String location) {
        Address address = null;

        Geocoder geocoder;
        geocoder = new Geocoder(applicationContext, Locale.getDefault());
        try {
            address = geocoder.getFromLocationName(location, 1).get(0);

            String city = address.getLocality();
            addressToExactLocation(address, city);
            // 2
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void setUserLocation(String location) {
        if (preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(LOCATION_PREF_KEY, location);
            editor.commit();
        }
        this.userLocation = location;
    }

    private void addressToExactLocation(Address address, String city) {
        String exactLocation = "";
        if (address.getThoroughfare() != null) {
            exactLocation = address.getThoroughfare();
            if (address.getSubThoroughfare() != null) {
                exactLocation += " " + address.getSubThoroughfare() + ", " + city;

            } else {
                exactLocation += ", " + city;
            }

        } else {
            setUserExactLocation(address.getFeatureName() + ", " + city);
        }
        setUserExactLocation(exactLocation);
    }

    private void notifyUserLoggedIn() {
        Log.d("USER", "notifyUserLoggedIn: " + getUserId());
        if (getUserId() == null) return;

        if (userInDb == null) {
            MintApplication.getDatabaseRef()
                    .collection("users")
                    .document(userAuthentication.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null) {
                                    userInDb = document.toObject(UserInDb.class).withId(document.getId());
                                    userInDb.startListeningDataBase();
                                    userDataFetched();
                                }
                            }
                        }
                    });
        }
    }

    private String getUserId() {
        String uId = null;

        if (isLoggedIn()) {
            if (userAuthentication.getUid() != null) {
                uId = userAuthentication.getUid();
            }
        }
        return uId;
    }
}
