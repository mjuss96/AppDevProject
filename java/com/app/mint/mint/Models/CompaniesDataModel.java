package com.app.mint.mint.Models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.app.mint.mint.Models.DbModels.Company;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/*
    DataModel for near by companies, inside DEFAULT_DISTANCE
    - Following singleton design pattern
 */
public class CompaniesDataModel {
    public static final double DEFAULT_DISTANCE = 7500;

    // One Degree Of coordinate movement in KM
    // Not perfectly accurate, but close enough for our use
    public static final double ONE_DEGREE_IN_KM = 111.00;
    private double centerLat;
    private double centerLong;
    private double distance = DEFAULT_DISTANCE;
    private MutableLiveData<List<Company>> currentCompanies = new MutableLiveData<>();
    private UserDataModel userData;
    private static CompaniesDataModel instance;

    // Constructor, will fetch data from database everytime user location changes
    // Need to be implemented: some kind of timer/listener to update data
    private CompaniesDataModel() {
        userData = UserDataModel.getInstance();
        LiveData<GeoPoint> userGeoPoint = userData.getUserGeoPoint();
        centerLat = userGeoPoint.getValue().getLatitude();
        centerLong = userGeoPoint.getValue().getLongitude();
        updateCurrentCompanies();
        userGeoPoint.observeForever(new Observer<GeoPoint>() {
            @Override
            public void onChanged(@Nullable GeoPoint geoPoint) {
                centerLat = geoPoint.getLatitude();
                centerLong = geoPoint.getLongitude();
                updateCurrentCompanies();
            }
        });
    }

    public static CompaniesDataModel getInstance() {
        if (instance == null) {
            instance = new CompaniesDataModel();
        }
        return instance;
    }


    // fetching data from database
    public void updateCurrentCompanies() {
        if (distance == 0) {
            distance = DEFAULT_DISTANCE;
        }
        Query query = makeQuery();

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Company> companies = new ArrayList<>();
                QuerySnapshot querySnapshot = task.getResult();
                List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                if (documents != null && documents.size() != 0) {
                    for (DocumentSnapshot document : documents) {
                        Company company = document.toObject(Company.class).withId(document.getId());
                        companies.add(company);
                    }
                }
                final double userLat = userData.getUserGeoPoint().getValue().getLatitude();
                final double userLong = userData.getUserGeoPoint().getValue().getLongitude();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    companies.sort(new Comparator<Company>() {
                        @Override
                        public int compare(Company company1, Company company2) {
                            double a = company1.distanceToUser(userLat, userLong);
                            double b = company2.distanceToUser(userLat, userLong);

                            return Double.compare(a, b);
                        }
                    });
                }

                int i = 0;
                for (Company company : companies) {
                    i++;
                    Log.d("SORTING", Integer.toString(i) + ": " + company.distanceToUser(userLat, userLong));
                }

                currentCompanies.setValue(companies);
            }
        });
    }

    // getter for companies livedata
    public LiveData<List<Company>> getCurrentCompanies() {
        if (currentCompanies.getValue() == null) {
            updateCurrentCompanies();
        }
        return currentCompanies;
    }

    // making the query for updateCurrentCompanies()
    // will query in square shape around user
    // (FireStore doesn't support proper location based queries)
    private Query makeQuery() {
        Query query = Company.companyCollectionRef;
        double distanceInKm = distance / 1000;
        double userLatitudeRadians = Math.toRadians(centerLat);
        double latitudeCosine = Math.cos(userLatitudeRadians);

        final double x = ONE_DEGREE_IN_KM;

        double distanceInLatDegrees = distanceInKm / x;
        double distanceInLonDegrees = (distanceInKm / x) / latitudeCosine;

        double latitudeMax = centerLat + distanceInLatDegrees;
        double latitudeMin = centerLat - distanceInLatDegrees;

        double longitudeMax = centerLong + distanceInLonDegrees;
        double longitudeMin = centerLong - distanceInLonDegrees;


        String minIndex = Double.toString(latitudeMin) + "_" + Double.toString(longitudeMin);
        String maxIndex = Double.toString(latitudeMax) + "_" + Double.toString(longitudeMax);
        Log.d("QUERY", "center" + Double.toString(centerLat) + ", " + Double.toString(centerLong));
        Log.d("QUERY", "makeQuery: " + minIndex + " " + maxIndex);
        query = query.whereGreaterThanOrEqualTo("latitude_longitude", minIndex)
                .whereLessThanOrEqualTo("latitude_longitude", maxIndex);


        return query;
    }


}
