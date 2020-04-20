package com.app.mint.mint.CompanyPage;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.mint.mint.R;

public class OffersFragment extends Fragment {
//yet to be implemented offers fragment

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_offers, container, false);
        return view;
    }
}