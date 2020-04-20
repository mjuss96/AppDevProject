package com.app.mint.mint.Models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.DbModels.Company;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
    ViewModel for SearchActivity
 */

public class SearchViewModel extends ViewModel implements Observer<List<Company>> {

    private CompaniesDataModel companiesDataModel = CompaniesDataModel.getInstance();
    private MutableLiveData<List<Company>> companies = new MutableLiveData<>();
    private MutableLiveData<List<Company>> searchResults = new MutableLiveData<>();
    private List<String> categoryFilters = new ArrayList<>();
    private double distanceFilter = CompaniesDataModel.DEFAULT_DISTANCE;
    private CircleOptions circleOptions;
    private UserDataModel userdata = ((MintApplication) MintApplication.getAppContext()).getUserData();
    LiveData<GeoPoint> userLocation = ((MintApplication)MintApplication.getAppContext()).getUserData().getUserGeoPoint();


    public SearchViewModel() {
        companiesDataModel.getCurrentCompanies().observeForever(this);
    }

    /*
    GETTERS AND SETTERS
     */

    public LiveData<List<Company>> getCompanies() {
        return companies;
    }

    public LiveData<List<Company>> getSearchResults() {
        return searchResults;
    }

    public CircleOptions getCircleOptions() {
        makeCircleOptions();
        return circleOptions;
    }

    public void setFilters(List<String> categoryFilters, double distanceFilter) {
        if (categoryFilters != null) {
            this.categoryFilters = categoryFilters;
        }
        if (distanceFilter > 0 && distanceFilter <= CompaniesDataModel.DEFAULT_DISTANCE) {
            setDistanceFilter(distanceFilter);

        }

        List<Company> unFilteredCompanies = companiesDataModel.getCurrentCompanies().getValue();
        filterAndUpdateCompanies(unFilteredCompanies);
    }

    private void setDistanceFilter(double distanceFilter) {
        this.distanceFilter = distanceFilter;
        makeCircleOptions();
    }

    public List<String> getCategoryFilters(){
        return categoryFilters;
    }

    public String getDistance() {
        return Double.toString(distanceFilter);
    }



    /*
    FILTERING
     */

    private void filterAndUpdateCompanies(List<Company> unFilteredCompanies) {

        //Get raw data from CompaniesDataModel
        List<Company> filteredCompanyList = unFilteredCompanies;

        // Filter by address
        // check if list is empty before each filter
        if (!filteredCompanyList.isEmpty())
            filteredCompanyList = filterByCategories(filteredCompanyList);
        if (!filteredCompanyList.isEmpty())
            filteredCompanyList = filterByDistance(filteredCompanyList);

        // Sort the results if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            filteredCompanyList.sort(new Comparator<Company>() {
                @Override
                public int compare(Company company1, Company company2) {
                    double a = company1.getRankingValue(userdata,CompaniesDataModel.DEFAULT_DISTANCE);
                    double b = company2.getRankingValue(userdata,CompaniesDataModel.DEFAULT_DISTANCE);

                    return Double.compare(b, a);
                }
            });
        }


        // Testing the sorting
        for (int i = 0; i < filteredCompanyList.size(); i++) {
            Company company = filteredCompanyList.get(i);
            Log.d("SORTING", Integer.toString(i) + ": " + company.getName() + ", " + Double.toString(company.getRankingValue(userdata, 7500)));
        }
        companies.setValue(filteredCompanyList);

    }

    private List<Company> filterByCategories(List<Company> companyList) {
        List<Company> filteredCompanies = new ArrayList<>();

        //if there is category filters
        if (!categoryFilters.isEmpty()) {
            //checking every company if it has all the address
            //adding company to filteredCompanies list if true
            for (Company company : companyList) {
                if (company.getCategories() != null) {
                    if (companyContainsAllCategoryFilters(company)) {
                        filteredCompanies.add(company);
                    }
                }
            }
        }

        // if no category limits, return companyList without modification
        else {
            return companyList;
        }
        return filteredCompanies;
    }

    private List<Company> filterByDistance(List<Company> unFilteredCompanyList) {
        List<Company> filteredCompanies = new ArrayList<>();

        // if given distance filter is in range
        // filter by given distance
        if (distanceFilter > 0 && distanceFilter <= CompaniesDataModel.DEFAULT_DISTANCE) {
            final double userLatitude = userLocation.getValue().getLatitude();
            final double userLongitude = userLocation.getValue().getLongitude();

            for (Company company : unFilteredCompanyList) {
                if(company.distanceToUser(userLatitude, userLongitude) <= (distanceFilter/1000)){
                    Log.d("DISTANCEFILTER", "filterByDistance: " + company.getName() + " "+  Double.toString(company.distanceToUser(userLatitude, userLongitude)));
                    filteredCompanies.add(company);
                }
            }
        } else {
            filteredCompanies = unFilteredCompanyList;
        }
        return filteredCompanies;
    }


    private boolean companyContainsAllCategoryFilters(Company company) {
        List<String> companyCategories = company.getCategories();
        for (String categoryFilter : categoryFilters) {
            if (!companyCategories.contains(categoryFilter)) {
                // Not containing category
                return false;
            }
        }
        return true;
    }


    /*
        SEARCHING
     */

    // Search for company from database, containing search words  in stringList
    public void searchFor(List<String> stringList) {
        final List<String> searchWords = stringList;
        String firstSearchWord = searchWords.get(0);

        Log.d("SEARCH", "model searching: " + firstSearchWord);
        Company.companyCollectionRef.whereArrayContains(Company.NAME_TAGS_ARRAY_NAME, firstSearchWord)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Company> companies = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        List<DocumentSnapshot> documents = querySnapshot.getDocuments();

                        if (documents != null && documents.size() != 0) {
                            for (DocumentSnapshot document : documents) {
                                Company company = document.toObject(Company.class).withId(document.getId());

                                Log.d("SEARCH", "model found: " + company.getName());
                                if(companyContainsAllNameTags(company,searchWords)){
                                    companies.add(company);
                                    Log.d("SEARCH", "company contains all search words: " + company.getName());

                                }
                            }
                        }
                        Log.d("SEARCH", "model sending :" + Integer.toString(companies.size()));
                        searchResults.setValue(companies);
                    }
                });
    }

    private boolean companyContainsAllNameTags(Company company, List<String> searchNameTags){
        List<String> companyNameTags = company.getName_tags();
        for (String nameTag : searchNameTags) {
            if (!companyNameTags.contains(nameTag)) {
                // Not containing search word
                return false;
            }
        }
        return true;
    }


    /*
    OTHERS
     */

    // making options for cirlce to be drawn in map ui
    private void makeCircleOptions() {
        circleOptions = new CircleOptions();
        LatLng center = new LatLng(userLocation.getValue().getLatitude(), userLocation.getValue().getLongitude());
        circleOptions.center(center);
        circleOptions.radius(distanceFilter);

        circleOptions.strokeColor(Color.parseColor("#50509ece"));
        circleOptions.strokeWidth(30);
        circleOptions.fillColor(Color.parseColor("#25509ece"));
    }

    //Removing the observer of currentCompanies from CompaniesDataModel
    //because we are using observeForever() in getCompanies function
    @Override
    protected void onCleared() {
        companiesDataModel.getCurrentCompanies().removeObserver(this);
        super.onCleared();
    }

    // Implementation of Observer<List<Company>>
    // If CompaniesDataModel's company list gets changed
    @Override
    public void onChanged(@Nullable List<Company> companyList) {
        filterAndUpdateCompanies(companyList);
    }

}
