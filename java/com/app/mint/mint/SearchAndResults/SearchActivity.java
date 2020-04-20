package com.app.mint.mint.SearchAndResults;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mint.mint.AddCompany.AddCompanyActivity;
import com.app.mint.mint.AddCompany.LocationPickActivity;
import com.app.mint.mint.CompanyPage.CompanyPageActivity;
import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Main.MainActivity;
import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Adapters.CompaniesArrayAdapter;
import com.app.mint.mint.Models.CompaniesDataModel;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.Models.SearchViewModel;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.OnBoarding.OnBoardingActivity;
import com.app.mint.mint.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ACTIVITY_RESULT_ID = 2;
    private UserDataModel userData;
    private EditText editTextSearch;
    private TextView textViewDistance;
    private TextView searchResults;
    private ImageButton textSearchBtn;
    private Button searchBtn;
    private SeekBar seekBar;
    private CheckBox checkBoxNails, checkBoxHair, checkBoxMakeup;
    private CheckBox checkBoxPedicure, checkBoxManicure, checkBoxFacial;
    private CheckBox checkBoxSolarium, checkBoxEyelashes, checkBoxEyebrows;
    private SearchViewModel searchViewModel;
    private ListView listView;
    private ScrollView scrollView;
    private CompaniesArrayAdapter companiesArrayAdapter;
    private CustomMapFragment mapFragment;
    private GoogleMap companiesMap;
    private List<Company> visibleCompanies = new ArrayList<>();
    private List<Company> searchResultCompanies = new ArrayList<>();
    private RelativeLayout container;
    private DrawerLayout drawer;
    private NavigationView sidebarFilter;
    private NavigationView sidebarNavigation;
    private double distance = CompaniesDataModel.DEFAULT_DISTANCE;


    Observer<List<Company>> resultObserver = new Observer<List<Company>>() {
        @Override
        public void onChanged(@Nullable List<Company> companies) { //checks if the companies are changed
            Log.d("SEARCH", "activity received : " + Integer.toString(companies.size()));
            searchResultCompanies = companies;
            showSearchResults();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setupViews();

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        });

        addFloatingActionButtons();

        setupToolbarsAndDrawers();

        //Show only map fragment by default
        //Hiding listViewFragment
        //fragmentManager.beginTransaction().hide(listViewFragment).commit();

        userData.getUserGeoPoint().observe(this, new Observer<GeoPoint>() {
            @Override
            public void onChanged(@Nullable GeoPoint geoPoint) {
                GeoPoint userGeoPoint = userData.getUserGeoPoint().getValue();
                LatLng userLocation = new LatLng(userGeoPoint.getLatitude(), userGeoPoint.getLongitude());
                if (companiesMap != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 13);
                    companiesMap.animateCamera(cameraUpdate);
                }
            }
        });

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                companiesMap = googleMap;
                try {

                    boolean success = companiesMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    SearchActivity.this, R.raw.custommap));

                    if (!success) {
                        Log.e("fail", "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e("fail", "Can't find style. Error: ", e);
                }
                companiesMap.setMinZoomPreference(10.0f);
                companiesMap.getUiSettings().setMapToolbarEnabled(false);
                companiesMap.getUiSettings().setCompassEnabled(false);

                mapFragment.setListener(new CustomMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    }
                });

                if (companiesMap != null) {
                    companiesMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            //if marker is clicked, shows window that can be clicked and redirects to company page
                            for (int i = 0; i < visibleCompanies.size(); i++) {
                                if (marker.getTitle().equals(visibleCompanies.get(i).getName())) {
                                    Intent intent = new Intent(SearchActivity.this, CompanyPageActivity.class);
                                    intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, visibleCompanies.get(i).getId());
                                    startActivity(intent);
                                }
                            }
                        }
                    });

                }
                addMarkers(true, visibleCompanies);
            }
        });

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        searchViewModel.getCompanies().observe(this, new Observer<List<Company>>() {
            @Override
            public void onChanged(@Nullable List<Company> companyList) {

                setVisibleCompanies(companyList); //sets companies visible that are in search range

            }
        });

        seekBar.setMax((int) distance);
        seekBar.setProgress((int) distance);
        textViewDistance.setText(Double.toString(distance) + " m");

        searchBtn.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { //used for changing the search range
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String distanceStr = Integer.toString(progress);
                textViewDistance.setText(distanceStr + " m");
                distance = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        companiesArrayAdapter = new CompaniesArrayAdapter(this, R.layout.company_list_item, visibleCompanies);
        listView.setAdapter(companiesArrayAdapter);

        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {  //when list item is clicked, redirects to company page
                Company company = (Company) parent.getItemAtPosition(position);
                Intent intent = new Intent(SearchActivity.this, CompanyPageActivity.class);
                intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, company.getId());
                Log.d("EXTRA", "onItemClick: " + company.getId());
                startActivity(intent);

            }
        });

        textSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForResults();
            }

        });

        if (companiesMap == null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {

                }
            });
        }
    }

    private void searchForResults() { //used for searching companies by name
        String searchStr = editTextSearch.getText().toString();
        List<String> searchWords = new ArrayList<>();

        searchStr = searchStr.toLowerCase();

        if (!searchStr.matches("^[ A-Öa-ö]+$")) { //checking if given name is invalid
            Toast.makeText(SearchActivity.this, "Only characters and spaces", Toast.LENGTH_SHORT).show();
            return;
        }
        if (searchStr.contains(" ")) {
            String splittedStr[];
            splittedStr = searchStr.split(" ");
            for (String s : splittedStr) {
                if (!s.equals("")) {
                    searchWords.add(s);
                }
            }
        } else {
            searchWords.add(searchStr);
        }
        if (!searchWords.isEmpty()) {
            searchViewModel.searchFor(searchWords);

            String debugStr = "";
            for (String word : searchWords) {
                debugStr += word + ", ";
            }
            Log.d("SEARCH", "searching: " + debugStr);
            Toast.makeText(SearchActivity.this, "Searching.. ", Toast.LENGTH_SHORT).show();
            searchViewModel.getSearchResults().observe(SearchActivity.this, resultObserver);
        }
    }

    private void setupToolbarsAndDrawers() { //sets the navigation menu and the buttons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }

        Toolbar toolbar = findViewById(R.id.activityToolBar);
        toolbar.setNavigationIcon(R.drawable.ic_main_menu);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(Gravity.START)) {
                    drawer.closeDrawer(Gravity.END);
                    drawer.openDrawer(Gravity.START);
                } else {
                    drawer.closeDrawer(Gravity.START);
                }
            }
        });


        sidebarNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_menu_add_company:
                        startActivity(new Intent(SearchActivity.this, AddCompanyActivity.class));
                        break;
                    case R.id.drawer_menu_followed_companies:
                        startActivity(new Intent(SearchActivity.this, MainActivity.class));
                        break;
                    case R.id.drawer_menu_locate:
                        startActivity(new Intent(SearchActivity.this, OnBoardingActivity.class));
                        break;
                    case R.id.drawer_menu_log_out:
                        // logout
                        break;
                    case R.id.drawer_menu_log_in:
                        startActivity(new Intent(SearchActivity.this, LoginActivity.class));
                        break;
                }
                return true;
            }
        });
    }

    private void showSearchResults() { //shows company results after companies have been searched and found successfully
        int numOfResults = searchResultCompanies.size();
        Log.d("SEARCH", "showSearchResults: " + Integer.toString(numOfResults));
        switch (numOfResults) {
            case 0:
                Toast.makeText(SearchActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                break;
            default:
                setVisibleCompanies(searchResultCompanies);
                addMarkers(true, searchResultCompanies);
                break;
        }
        drawer.closeDrawer(sidebarFilter, true);
    }

    private void setVisibleCompanies(List<Company> updatedCompanies) {
        visibleCompanies.clear();
        visibleCompanies.addAll(updatedCompanies);
        companiesArrayAdapter.updateCompanies(visibleCompanies);

        searchResults.setText(getString(R.string.distance) + ": " + searchViewModel.getDistance());

        if (searchViewModel.getCategoryFilters() != null) {
            String listString = TextUtils.join(", ", searchViewModel.getCategoryFilters());
            searchResults.setText(searchResults.getText() + "\n" + getString(R.string.categories) + ": " + listString);
        }
        addMarkers(false, visibleCompanies);
    }


    private void setupViews() { //getting all the view ID's
        scrollView = findViewById(R.id.searchScrollView);
        sidebarNavigation = findViewById(R.id.navigation_drawer);
        container = findViewById(R.id.activitySearchResultContainer);
        checkBoxMakeup = findViewById(R.id.checkBoxMakeup);
        checkBoxHair = findViewById(R.id.checkBoxHair);
        checkBoxNails = findViewById(R.id.checkBoxNails);
        checkBoxPedicure = findViewById(R.id.checkBoxPedicure);
        checkBoxManicure = findViewById(R.id.checkBoxManicure);
        checkBoxFacial = findViewById(R.id.checkBoxFacial);
        checkBoxSolarium = findViewById(R.id.checkBoxSolarium);
        checkBoxEyelashes = findViewById(R.id.checkBoxEyelashes);
        checkBoxEyebrows = findViewById(R.id.checkBoxEyebrows);
        searchBtn = findViewById(R.id.SearchActivityButtonSearch);
        textViewDistance = findViewById(R.id.SearchActivityTextViewDistanceValue);
        seekBar = findViewById(R.id.SearchActivitySeekBarDistance);
        listView = findViewById(R.id.companiesList);
        userData = ((MintApplication) getApplicationContext()).getUserData();
        mapFragment = (CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.activitySearchMapFragment);
        drawer = findViewById(R.id.drawer_layout);
        sidebarFilter = findViewById(R.id.sidebar);
        textSearchBtn = findViewById(R.id.ButtonTextSearch);
        editTextSearch = findViewById(R.id.editTextSearch);
        searchResults = findViewById(R.id.search_results);
    }


    private void addMarkers(boolean moveCamera, final List<Company> markersToAdd) {
        //used to update the map with markers that contain valid companies in the search range
        if (companiesMap != null) {
            companiesMap.clear();
            GeoPoint userGeoPoint = userData.getUserGeoPoint().getValue();
            LatLng userLocation = new LatLng(userGeoPoint.getLatitude(), userGeoPoint.getLongitude());
            companiesMap.addMarker(new MarkerOptions().position(userLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocationmarker)));


            //Listener for marker InfoWindow clicks
            companiesMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    for (int i = 0; i < markersToAdd.size(); i++) {
                        if (marker.getTitle().equals(markersToAdd.get(i).getName())) {
                            Intent intent = new Intent(SearchActivity.this, CompanyPageActivity.class);
                            intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, markersToAdd.get(i).getId());
                            startActivity(intent);
                        }
                    }
                }
            });


            //Adding markers to map
            for (int i = 0; i < markersToAdd.size(); i++) {
                String categories = "";
                List<String> categoriesList = markersToAdd.get(i).getLocalizedCategories(SearchActivity.this);
                for (int e = 0; e < categoriesList.size(); e++) {
                    categories += " " + categoriesList.get(e);
                }
                if (i >= 0 && i <= 2 && markersToAdd.size() > 5) {
                    LatLng latLng = new LatLng(markersToAdd.get(i).getLatitude(), markersToAdd.get(i).getLongitude());
                    companiesMap.addMarker(new MarkerOptions().position(latLng).title(markersToAdd.get(i).getName()).snippet(categories)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.firemarker)));
                } else if (markersToAdd.get(i).isActive_deals()) {
                    LatLng latLng = new LatLng(markersToAdd.get(i).getLatitude(), markersToAdd.get(i).getLongitude());
                    companiesMap.addMarker(new MarkerOptions().position(latLng).title(markersToAdd.get(i).getName()).snippet(categories)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.activedealsmarker)));
                } else {
                    LatLng latLng = new LatLng(markersToAdd.get(i).getLatitude(), markersToAdd.get(i).getLongitude());
                    companiesMap.addMarker(new MarkerOptions().position(latLng).title(markersToAdd.get(i).getName()).snippet(categories)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.normalmarker)));
                }
            }

            // Drawing polygon to show filtered area
            // if not adding searchResults to map
            if (markersToAdd != searchResultCompanies) {
                CircleOptions circleOptions = searchViewModel.getCircleOptions();
                if (circleOptions != null) {
                    companiesMap.addCircle(circleOptions);
                }
            }

            //moving camera
            if (moveCamera) {
                if (markersToAdd.size() == 1) {
                    LatLng companyMarkerLatLng = new LatLng(markersToAdd.get(0).getLatitude(), markersToAdd.get(0).getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(companyMarkerLatLng, 13);
                    companiesMap.animateCamera(cameraUpdate);
                } else {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 13);
                    companiesMap.animateCamera(cameraUpdate);
                }
            }
        }
    }


    private void addFloatingActionButtons() {  //used to add buttons for automatic location and manual address selection
        float d = this.getResources().getDisplayMetrics().density;

        FloatingActionButton fabLocate = new FloatingActionButton(this);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) (8 * d), (int) (80 * d), (int) (8 * d), (int) (80 * d));
        fabLocate.setLayoutParams(layoutParams);
        fabLocate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
        fabLocate.setScaleType(ImageView.ScaleType.CENTER);
        fabLocate.setSize(FloatingActionButton.SIZE_NORMAL);
        fabLocate.setImageResource(R.drawable.ic_my_location);
        fabLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We are showing only toast message. However, you can do anything you need.
                boolean locateSuccessful = userData.locateUser(SearchActivity.this);
                if (!locateSuccessful) {
                    Toast.makeText(getApplicationContext(), "No Permissions to use GPS", Toast.LENGTH_SHORT).show();
                }

            }
        });
        container.addView(fabLocate);

        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams2.setMargins((int) (8 * d), (int) (150 * d), (int) (8 * d), (int) (80 * d));
        FloatingActionButton fabPickLocation = new FloatingActionButton(this);
        fabPickLocation.setLayoutParams(layoutParams2);
        fabPickLocation.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorPrimary)));
        fabPickLocation.setScaleType(ImageView.ScaleType.CENTER);
        fabPickLocation.setSize(FloatingActionButton.SIZE_NORMAL);
        fabPickLocation.setImageResource(R.drawable.ic_map);
        fabPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We are showing only toast message. However, you can do anything you need.
                startActivityForResult(new Intent(SearchActivity.this, LocationPickActivity.class), ACTIVITY_RESULT_ID);
            }
        });
        container.addView(fabPickLocation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.SearchActivityButtonSearch) { //button for searching companies with category filters and search range
            final boolean nailsChecked = checkBoxNails.isChecked();
            final boolean makeupChecked = checkBoxMakeup.isChecked();
            final boolean hairChecked = checkBoxHair.isChecked();
            final boolean pedicureChecked = checkBoxPedicure.isChecked();
            final boolean manicureChecked = checkBoxManicure.isChecked();
            final boolean facialChecked = checkBoxFacial.isChecked();
            final boolean solariumChecked = checkBoxSolarium.isChecked();
            final boolean eyelashesChecked = checkBoxEyelashes.isChecked();
            final boolean eyebrowsChecked = checkBoxEyebrows.isChecked();

            List<String> categoryFilters = new ArrayList<>();

            if (nailsChecked) categoryFilters.add(Company.CATEGORY_NAILS);
            if (makeupChecked) categoryFilters.add(Company.CATEGORY_MAKEUP);
            if (hairChecked) categoryFilters.add(Company.CATEGORY_HAIR);
            if (pedicureChecked) categoryFilters.add(Company.CATEGORY_PEDICURE);
            if (manicureChecked) categoryFilters.add(Company.CATEGORY_MANICURE);
            if (facialChecked) categoryFilters.add(Company.CATEGORY_FACIAL);
            if (solariumChecked) categoryFilters.add(Company.CATEGORY_SOLARIUM);
            if (eyelashesChecked) categoryFilters.add(Company.CATEGORY_EYELASHES);
            if (eyebrowsChecked) categoryFilters.add(Company.CATEGORY_EYEBROWS);

            searchViewModel.setFilters(categoryFilters, distance);

            drawer.closeDrawer(sidebarFilter, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //used when manual address pick is selected from LocationPickActivity
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_ID && resultCode == RESULT_OK) {
            userData.setUserCoordinates(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.getItem(0).setIcon(R.drawable.ic_tune);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.main_menu_search:
                if (!drawer.isDrawerOpen(Gravity.END)) {
                    drawer.closeDrawer(Gravity.START);
                    drawer.openDrawer(Gravity.END);
                } else {
                    drawer.closeDrawer(Gravity.END);
                }
                break;
        }
        return true;
    }
}


