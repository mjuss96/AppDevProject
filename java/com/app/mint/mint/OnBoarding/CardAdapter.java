package com.app.mint.mint.OnBoarding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.mint.mint.R;

import java.util.ArrayList;

/*
    CUSTOM PAGER ADAPTER FOR ON BOARDING CARDS
 */

public class CardAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<OnBoardingCard> onBoardingCards = new ArrayList<>();

    public CardAdapter(@NonNull ArrayList<OnBoardingCard> cards, @NonNull Context context){
        this.onBoardingCards = cards;
        this.context = context;
    }

    @Override
    public int getCount() {
        return onBoardingCards.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == (LinearLayout) o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.on_boarding_card, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.onBoardingCardImageView);
        TextView textViewTitle = (TextView) view.findViewById(R.id.onBoardingCardTextViewTitle);
        TextView textViewDesc = (TextView) view.findViewById(R.id.onBoardingCardTextViewDescription);

        imageView.setImageResource(onBoardingCards.get(position).getImgSrc());
        textViewTitle.setText(onBoardingCards.get(position).getTitle());
        textViewDesc.setText(onBoardingCards.get(position).getDescription());

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
