package com.app.mint.mint.OnBoarding;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.app.mint.mint.OnBoarding.OnBoardingActivity;

/*
    Class for storing data for OnBoarding cards
    cards will be displayed by ViewPager and CardAdapter
 */

public class OnBoardingCard {
    int imgResourceId;
    String title;
    String description;


    // Checking if imageResourceId is valid
    // Throwing NotFoundException

    public OnBoardingCard(@NonNull int imgRId, String title, String description,@NonNull Context context){

        this.imgResourceId = imgRId;
        this.title = title;
        this.description = description;
    }

    public int getImgSrc() {
        return imgResourceId;
    }

    // Checking if imageResourceId is valid
    // Throwing NotFoundException
    public void setImgSrc(@NonNull int imgRId,@NonNull Context context) {
        this.imgResourceId = imgRId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
