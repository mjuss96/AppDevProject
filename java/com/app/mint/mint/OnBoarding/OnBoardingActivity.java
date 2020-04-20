package com.app.mint.mint.OnBoarding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.mint.mint.AddCompany.LocationPickActivity;
import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.R;
import com.app.mint.mint.Models.UserDataModel;

import java.util.ArrayList;

/*
    Activity to display OnBoardingCards,
    will be called from MainActivity if user launches the app first time
 */

public class OnBoardingActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private ArrayList<OnBoardingCard> onBoardingCards = new ArrayList<>();
    private UserDataModel userDataModel;

    private LinearLayout dotsLayout;
    private TextView[] indicatorDots;

    private Button nextBtn;
    private Button prevBtn;
    private Button allowGPSBtn;
    private Button denyGPSBtn;
    private int currentPage;

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    static final int ACTIVITY_RESULT_ID = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDataModel = ((MintApplication)getApplicationContext()).getUserData();

        //Remove notification and title bars
        try
        {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            this.getSupportActionBar().hide();
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        catch (NullPointerException e){}

        setContentView(R.layout.activity_on_boarding);


        nextBtn = (Button) findViewById(R.id.btnNext);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(currentPage+1);
            }
        });

        prevBtn = (Button) findViewById(R.id.btnPrevious);
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(currentPage-1);
            }
        });


        allowGPSBtn = findViewById(R.id.allowLocation);
        allowGPSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(OnBoardingActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(OnBoardingActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        ActivityCompat.requestPermissions(OnBoardingActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    // Permission has already been granted
                    userDataModel.setOnBoardingShown(true);
                    userDataModel.locateUser(OnBoardingActivity.this);
                    finish();
                }
            }
        });


        denyGPSBtn = findViewById(R.id.denyLocation);
        denyGPSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(OnBoardingActivity.this, LocationPickActivity.class), ACTIVITY_RESULT_ID);
            }
        });


        dotsLayout = (LinearLayout)findViewById(R.id.onBoardingActivityDotsLayout);

        onBoardingCards.add(new OnBoardingCard(R.drawable.onboarding_search, getString(R.string.search), getString(R.string.onboarding_search_description),getApplicationContext()));
        onBoardingCards.add(new OnBoardingCard(R.drawable.onboarding_location_picking, getString(R.string.location), getString(R.string.onboarding_location_pick_description),getApplicationContext()));
        onBoardingCards.add(new OnBoardingCard(R.drawable.onboarding_location, getString(R.string.location),getString(R.string.onboarding_location_description),getApplicationContext()));


        viewPager = (ViewPager) findViewById(R.id.onBoardingActivityViewPager);
        CardAdapter cardAdapter = new CardAdapter(onBoardingCards, this);
        viewPager.setAdapter(cardAdapter);

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        if(onBoardingCards.size() == 1) {
            onlyOneCard();
        }else{
            addDotsIndicator(0);
        }
    }


    // function to disable and show correct buttons if only one card
    private void onlyOneCard() {

        nextBtn.setEnabled(false);
        nextBtn.setVisibility(View.INVISIBLE);

        prevBtn.setEnabled(false);
        prevBtn.setVisibility(View.INVISIBLE);

        allowGPSBtn.setEnabled(true);
        denyGPSBtn.setEnabled(true);
        allowGPSBtn.setVisibility(View.VISIBLE);
        denyGPSBtn.setVisibility(View.VISIBLE);
    }


    // function to add the dots to indicate current "page"

    public void addDotsIndicator( int position){
        dotsLayout.removeAllViews();
        int numOfCards = onBoardingCards.size();
        indicatorDots = new TextView[numOfCards];
        for (int i = 0; i<numOfCards; i++) {
            indicatorDots[i] = new TextView(this);
            indicatorDots[i].setText("\u2022");
            indicatorDots[i].setTextSize(35);
            indicatorDots[i].setTextColor(getColor(R.color.colorPrimary));
            dotsLayout.addView(indicatorDots[i]);
        }
        if (numOfCards > 0) {
            indicatorDots[position].setTextColor(getColor(R.color.colorAccent));
        }


    }


    // Listener for ViewPager pageChange
    // adding new dots indicator
    // changes visibility of "next", "previous" and "locate" buttons

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            int maxPage = indicatorDots.length -1;
            addDotsIndicator(i);
            currentPage = i;


            if (i == 0){
                allowGPSBtn.setEnabled(false);
                denyGPSBtn.setEnabled(false);
                prevBtn.setEnabled(false);
                prevBtn.setVisibility(View.INVISIBLE);
                nextBtn.setEnabled(true);
            }else if(i == maxPage){
                nextBtn.setEnabled(false);
                nextBtn.setVisibility(View.INVISIBLE);
                prevBtn.setEnabled(true);

                allowGPSBtn.setEnabled(true);
                denyGPSBtn.setEnabled(true);
                allowGPSBtn.setVisibility(View.VISIBLE);
                denyGPSBtn.setVisibility(View.VISIBLE);
            }else{
                nextBtn.setEnabled(true);
                nextBtn.setVisibility(View.VISIBLE);

                prevBtn.setEnabled(true);
                prevBtn.setVisibility(View.VISIBLE);

                allowGPSBtn.setVisibility(View.INVISIBLE);
                denyGPSBtn.setVisibility(View.INVISIBLE);
                allowGPSBtn.setEnabled(false);
                denyGPSBtn.setEnabled(false);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };


    // this function will be called after location permission query
    // if user gave permission activity will be finished
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    userDataModel.locateUser(OnBoardingActivity.this);
                    userDataModel.setOnBoardingShown(true);
                    finish();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_ID && resultCode == RESULT_OK) {
            userDataModel.setUserCoordinates(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
            userDataModel.setOnBoardingShown(true);
            finish();

        }
    }
}

