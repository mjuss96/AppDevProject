package com.app.mint.mint.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.mint.mint.Models.DbModels.Comment;
import com.app.mint.mint.R;

import java.util.List;

/*
    Custom ArrayAdapter for comments
    will be used by CompanyPageActivity
 */

public class CommentArrayAdapter extends ArrayAdapter {
    List<Comment> comments;
    Context context;
    int layoutResourceId;

    TextView title, text;

    public CommentArrayAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.context = context;
        this.comments = objects;
        this.layoutResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);
        }
        Comment comment = comments.get(position);

        title = listItem.findViewById(R.id.commentListItemTitle);
        text = listItem.findViewById(R.id.commentListItemText);

        title.setText(comment.getTitle());
        text.setText(comment.getText());

        return listItem;
    }
}