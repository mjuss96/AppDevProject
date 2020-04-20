package com.app.mint.mint.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.R;

import java.util.List;

/*
    Custom ArrayAdapter for Companies
    will be used by SearchActivity
 */

public class CompaniesArrayAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    List<Company> companies;

    TextView title, address, numOfRatings;
    RatingBar ratingBar;


    public CompaniesArrayAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.companies = objects;
        this.context = context;
        this.layoutResourceId = resource;
    }

    public void updateCompanies(List companies) {
        this.companies = companies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
        }
        Company company = companies.get(position);

        title = listItem.findViewById(R.id.companyListItemTextViewTitle);
        numOfRatings = listItem.findViewById(R.id.companyListItemTextViewNumOfRatings);
        address = listItem.findViewById(R.id.companyListItemTextViewAddress);
        ratingBar = listItem.findViewById(R.id.companyListItemRatingBar);

        title.setText(company.getName());
        numOfRatings.setText("(" + Integer.toString(company.getNum_of_ratings()) + ")");
        address.setText(company.getAddress());
        ratingBar.setRating(company.getRating_avg());

        return listItem;
    }
}
