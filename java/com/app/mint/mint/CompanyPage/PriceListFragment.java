package com.app.mint.mint.CompanyPage;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.R;

public class PriceListFragment extends Fragment {

    private View view;
    private CompanyPageActivity context;
    private TextView priceListTextView;
    private Company company;
    private String price_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    // Inflate the layout for this fragment
        context = (CompanyPageActivity) container.getContext();
        view = inflater.inflate(R.layout.fragment_pricelist, container, false);
        company = context.viewModel.getCompany();
        price_list = company.getPrice_list();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        priceListTextView = getView().findViewById(R.id.priceListTextView);
        if(price_list != null){
            priceListTextView.setText(price_list); //show price list
        }

        super.onViewCreated(view, savedInstanceState);
    }
}