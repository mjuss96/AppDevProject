package com.app.mint.mint.SearchAndResults;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.mint.mint.R;

public class ListViewFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_view_fragment, parent, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
    }
}
