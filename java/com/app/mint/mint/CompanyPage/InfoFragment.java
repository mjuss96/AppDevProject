package com.app.mint.mint.CompanyPage;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.Models.DbModels.Cooldown;
import com.app.mint.mint.R;

import java.util.List;

public class InfoFragment extends Fragment {

    private static final int LOGIN_RESULT_KEY = 4;
    private View view;
    private CompanyPageActivity context;
    private Company company;
    private List<String> categories;
    private TextView categoriesTextView;
    private TextView descriptionTextView;
    private TextView numOfRatings;
    private RatingBar ratingBar2;
    private RatingBar ratingBar;
    private float rating_avg;
    private int num_of_ratings;
    private Button addRatingBtn;
    private Button addRatingBtn2;
    private float new_rating_avg;
    private float new_rating_bar_rating;
    private float n;
    private String description_text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    // Inflate the layout for this fragment
        context = (CompanyPageActivity) container.getContext();
        view = inflater.inflate(R.layout.fragment_info, container, false);

        company = context.viewModel.getCompany();
        categories = company.getLocalizedCategories(context);
        rating_avg = company.getRating_avg();
        num_of_ratings = company.getNum_of_ratings();


        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        categoriesTextView = getView().findViewById(R.id.categoriesTextView); //add categories from database
        String joined = "#";
        joined += TextUtils.join(" #", categories);
        categoriesTextView.setText(joined);

        ratingBar2 = getView().findViewById(R.id.ratingBar2); //get rating average from database
        ratingBar2.setRating(rating_avg);

        numOfRatings = getView().findViewById(R.id.numOfRatings); //get number of ratings from database
        String numOfRatingsString = Integer.toString(num_of_ratings);
        numOfRatings.setText("("+numOfRatingsString+")");

        descriptionTextView = getView().findViewById(R.id.descriptionTextView); //get description from database, if description has been added
        description_text = company.getDescription();
        if(description_text != null) {
            descriptionTextView.setText(description_text);
        }

        ratingBar = getView().findViewById(R.id.ratingBar);
        addRatingBtn = getView().findViewById(R.id.addRatingBtn);
        addRatingBtn2 = getView().findViewById(R.id.addRatingBtn2);
        addRatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.userDataModel.isLoggedIn() == false) {                           //check if user has logged in
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());   // if user hasnt, show pop up to login
                    builder.setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() { //send user to login page
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivityForResult(intent,LOGIN_RESULT_KEY);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    builder.setMessage(R.string.logged_in_question);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else if(context.userDataModel.hasCooldown(Cooldown.COOLDOWN_TYPE_RATING, context.company.getId())){ //check if user has cooldown
                    Toast.makeText(context,"You have cooldown", Toast.LENGTH_SHORT).show();
                }else {
                    ratingBar.setVisibility(View.VISIBLE);  //view ratingbar so user can rate
                    addRatingBtn2.setVisibility(View.VISIBLE);
                    addRatingBtn.setText("");
                }
            }
        });

        if(context.userDataModel.hasCooldown(Cooldown.COOLDOWN_TYPE_RATING, context.company.getId())){ //check if user has cooldown
            addRatingBtn.setVisibility(View.INVISIBLE);
        }

        addRatingBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRatingBtn.setText("+");                          //calculate new rating average and number of ratings
                new_rating_bar_rating = ratingBar.getRating();      //and update them in database and in view
                num_of_ratings++;
                n = num_of_ratings;
                new_rating_avg = rating_avg * ( n - 1 ) / n + new_rating_bar_rating / n;
                company.getSelfReference().update(Company.NUM_OF_RATINGS_FIELD_NAME, num_of_ratings);
                company.getSelfReference().update(Company.RATING_AVG_FIELD_NAME, new_rating_avg);
                context.userDataModel.setCooldown(Cooldown.COOLDOWN_TYPE_RATING, context.company.getId());  //add rating cooldown for user
                ratingBar.setVisibility(View.GONE);
                addRatingBtn2.setVisibility(View.GONE);
                company.setNum_of_ratings(num_of_ratings);
                company.setRating_avg(new_rating_avg);
                ratingBar2.setRating(new_rating_avg);
                String numOfRatingsString = Integer.toString(num_of_ratings);
                numOfRatings.setText("("+numOfRatingsString+")");
                rating_avg = company.getRating_avg();
                addRatingBtn.setVisibility(View.INVISIBLE);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}