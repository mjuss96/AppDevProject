package com.app.mint.mint.Main;


import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.app.mint.mint.Adapters.CompanyRecyclerViewAdapter;
import com.app.mint.mint.AddCompany.AddCompanyActivity;
import com.app.mint.mint.CompanyPage.CompanyPageActivity;
import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Models.CompaniesDataModel;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.OnBoarding.OnBoardingActivity;
import com.app.mint.mint.R;
import com.app.mint.mint.SearchAndResults.SearchActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private UserDataModel userData;
    private TextView textViewFollowedCompanies;
    private TextView textViewNearbyCompanies;
    private RecyclerView recyclerViewFollowedCompanies;
    private RecyclerView recyclerViewNearbyCompanies;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // adapters
    private CompanyRecyclerViewAdapter nearbyCompaniesAdapter;
    private CompanyRecyclerViewAdapter followedCompaniesAdapter;

    // Company Lists
    private List<Company> nearbyCompanies = new ArrayList<>();
    private List<Company> followedCompanies = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting instance of UserDataModel
        userData = ((MintApplication) getApplicationContext()).getUserData();

        // "findViewById()" for ui elements
        findUiElements();

        setupToolbarAndDrawer();

        setupRecyclerViews();

        // fetching content for RecyclerViews
        userData.getFollowedCompaniesLiveData().observe(this, new Observer<List<Company>>() {
            @Override
            public void onChanged(@Nullable List<Company> companies) {
                Log.d("COMPANIES", "onChanged: " + Integer.toString(companies.size()));
                updateFollowedCompanies(companies);
            }
        });

        CompaniesDataModel.getInstance().getCurrentCompanies().observe(this, new Observer<List<Company>>() {
            @Override
            public void onChanged(@Nullable List<Company> companies) {
                if (companies != null && !nearbyCompanies.equals(companies)) {
                    updateNearByCompanies(companies);
                }
            }
        });

    }

    private void setupToolbarAndDrawer() {
        // changing StatusBar color if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }

        // setting up the ItemSelectListener for NavigationView
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_menu_add_company:
                        startActivity(new Intent(MainActivity.this, AddCompanyActivity.class));
                        drawerLayout.closeDrawer(Gravity.START);
                        break;
                    case R.id.drawer_menu_followed_companies:
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        drawerLayout.closeDrawer(Gravity.START);
                        break;
                    case R.id.drawer_menu_locate:
                        startActivity(new Intent(MainActivity.this, OnBoardingActivity.class));
                        drawerLayout.closeDrawer(Gravity.START);
                        break;
                    case R.id.drawer_menu_log_out:
                        // logout
                        break;
                    case R.id.drawer_menu_log_in:
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        drawerLayout.closeDrawer(Gravity.START);
                        break;
                }
                return true;
            }
        });

        // Setting up toolbar
        Toolbar toolbar = findViewById(R.id.activityToolBar);
        toolbar.setNavigationIcon(R.drawable.ic_main_menu);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.openDrawer(Gravity.START);
                } else {
                    drawerLayout.closeDrawer(Gravity.START);
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.main_menu_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // if user is logged in, show followed companies
        if (userData.isLoggedIn()) {
            textViewFollowedCompanies.setVisibility(View.VISIBLE);
            recyclerViewFollowedCompanies.setVisibility(View.VISIBLE);
        } else {
            textViewFollowedCompanies.setVisibility(View.GONE);
            recyclerViewFollowedCompanies.setVisibility(View.GONE);
        }

        // if user haven't seen onBoarding (is using app first time),
        // OnBoardingActivity will be launched
        if (!userData.isOnBoardingShown()) {
            startActivity(new Intent(MainActivity.this, OnBoardingActivity.class));
        }

        // updating the nearByCompanies, if the results has changed
        List<Company> nearByCompanies = CompaniesDataModel.getInstance().getCurrentCompanies().getValue();
        if (nearByCompanies != null && !this.nearbyCompanies.equals(nearByCompanies)) {
            updateNearByCompanies(nearByCompanies);
        }
    }


    // functions to update contents inside the RecyclerViews

    private void updateNearByCompanies(List<Company> companies) {
        Log.d("COMPANIES", "update to nearby companies " + Integer.toString(companies.size()));

        if (companies.isEmpty()) {
            textViewNearbyCompanies.setVisibility(View.GONE);
            recyclerViewNearbyCompanies.setVisibility(View.GONE);
            if (nearbyCompanies.isEmpty()) {
                return;
            }
        } else {
            this.nearbyCompanies = companies;

            textViewNearbyCompanies.setVisibility(View.VISIBLE);
            recyclerViewNearbyCompanies.setVisibility(View.VISIBLE);

            nearbyCompaniesAdapter = new CompanyRecyclerViewAdapter(this, nearbyCompanies);
            CompanyRecyclerViewAdapter.ItemClickListener nearbyItemClickListener = new CompanyRecyclerViewAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Company company = nearbyCompaniesAdapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this, CompanyPageActivity.class);
                    intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, company.getId());
                    startActivity(intent);
                }
            };
            nearbyCompaniesAdapter.setClickListener(nearbyItemClickListener);

            recyclerViewNearbyCompanies.setAdapter(nearbyCompaniesAdapter);
            nearbyCompaniesAdapter.notifyDataSetChanged();
        }
    }

    private void updateFollowedCompanies(List<Company> companies) {
        Log.d("COMPANIES", "update to followed companies " + Integer.toString(companies.size()));

        if (companies.isEmpty()) {
            textViewFollowedCompanies.setVisibility(View.GONE);
            recyclerViewFollowedCompanies.setVisibility(View.GONE);
            if (followedCompanies.isEmpty()) {
                return;
            }
        } else {
            this.followedCompanies = companies;
            textViewFollowedCompanies.setVisibility(View.VISIBLE);
            recyclerViewFollowedCompanies.setVisibility(View.VISIBLE);

            followedCompaniesAdapter = new CompanyRecyclerViewAdapter(this, followedCompanies);
            CompanyRecyclerViewAdapter.ItemClickListener nearbyItemClickListener = new CompanyRecyclerViewAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Company company = followedCompaniesAdapter.getItem(position);
                    Intent intent = new Intent(MainActivity.this, CompanyPageActivity.class);
                    intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, company.getId());
                    startActivity(intent);
                }
            };
            followedCompaniesAdapter.setClickListener(nearbyItemClickListener);

            recyclerViewFollowedCompanies.setAdapter(followedCompaniesAdapter);
            followedCompaniesAdapter.notifyDataSetChanged();
        }
    }



    private void findUiElements(){
        textViewFollowedCompanies = findViewById(R.id.textViewFollowedCompanies);
        textViewNearbyCompanies = findViewById(R.id.textViewNearYou);
        recyclerViewFollowedCompanies = findViewById(R.id.recyclerViewFollowedCompanies);
        recyclerViewNearbyCompanies = findViewById(R.id.recyclerViewNearYou);
        drawerLayout = findViewById(R.id.mainActivityDrawerLayout);
        navigationView = findViewById(R.id.navigation_drawer);
    }

    private void setupRecyclerViews(){
        LinearLayoutManager linearLayoutManagerA = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager linearLayoutManagerB = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFollowedCompanies.setLayoutManager(linearLayoutManagerA);
        recyclerViewNearbyCompanies.setLayoutManager(linearLayoutManagerB);

        followedCompaniesAdapter = new CompanyRecyclerViewAdapter(this, followedCompanies);

        CompanyRecyclerViewAdapter.ItemClickListener followedItemClickListener = new CompanyRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Company company = followedCompaniesAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, CompanyPageActivity.class);
                intent.putExtra(CompanyPageActivity.COMPANY_ID_EXTRA, company.getId());
                startActivity(intent);
            }
        };

        followedCompaniesAdapter.setClickListener(followedItemClickListener);

        recyclerViewFollowedCompanies.setAdapter(followedCompaniesAdapter);

    }
}

