package com.app.mint.mint.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.R;

import java.util.List;

/*
    Custom RecyclerViewAdapter for MainActivity RecyclerView
 */

public class CompanyRecyclerViewAdapter extends RecyclerView.Adapter<CompanyRecyclerViewAdapter.ViewHolder> {
    private List<Company> companies;
    private LayoutInflater inflater;
    private ItemClickListener itemClickListener;

    CompanyRecyclerViewAdapter(Context context, List<Company> companies) {
        this.inflater = LayoutInflater.from(context);
        this.companies = companies;

    }

    @NonNull
    @Override
    public CompanyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.recycler_view_company_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        Company company = companies.get(i);
        viewHolder.textViewName.setText(company.getName());
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    // convenience method for getting data at click position
    public Company getItem(int id) {
        return companies.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.recyclerViewCompanyItemName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) itemClickListener.onItemClick(v, getAdapterPosition());

        }
    }
}
