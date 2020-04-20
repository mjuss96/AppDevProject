package com.app.mint.mint.CompanyPage;

import android.app.AlertDialog;
import android.app.Fragment;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Models.DbModels.Comment;
import com.app.mint.mint.Adapters.CommentArrayAdapter;
import com.app.mint.mint.Models.DbModels.Cooldown;
import com.app.mint.mint.R;

import java.util.ArrayList;
import java.util.List;

public class RatingsFragment extends Fragment {

    private static final int LOGIN_RESULT_KEY = 4;
    private View view;
    private CompanyPageActivity context;
    private ListView listView;
    private CommentArrayAdapter commentArrayAdapter;
    private ImageButton addCommentBtn;
    private EditText editTitle;
    private EditText editComment;
    private Button addCommentBtn2;

    List<Comment> commentlist = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    // Inflate the layout for this fragment
        context = (CompanyPageActivity) container.getContext();
        view = inflater.inflate(R.layout.fragment_ratings, container, false);

        return view;
    }

    @Override
    public void onDestroyView() {
        context.commentsList.removeObservers(context);  //stop checking for updates when leaving
        super.onDestroyView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        listView = getView().findViewById(R.id.commentsList);
        context.commentsList.observe(context, new Observer<List<Comment>>() {   //check if new comments
            @Override
            public void onChanged(@Nullable List<Comment> comments) {   //update if there is new comments
                updateCommentsList(comments);
            }
        });

        super.onViewCreated(view, savedInstanceState);

        addCommentBtn = getView().findViewById(R.id.addCommentBtn);
        addCommentBtn2 = getView().findViewById(R.id.addCommentBtn2);
        if(!context.userDataModel.hasCooldown(Cooldown.COOLDOWN_TYPE_COMMENT,context.company.getId())){ //check if user has cooldown
            addCommentBtn2.setVisibility(View.VISIBLE);
        }

        addCommentBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editComment.setVisibility(View.VISIBLE);
                editTitle.setVisibility(View.VISIBLE);
                addCommentBtn.setVisibility(View.VISIBLE);
                addCommentBtn2.setVisibility(View.GONE);
            }
        });

        editTitle = getView().findViewById(R.id.editTitle);
        editComment = getView().findViewById(R.id.editComment);
        if(context.userDataModel.hasCooldown(Cooldown.COOLDOWN_TYPE_COMMENT, context.company.getId())){ //check if user doesnt have cooldown
            editComment.setVisibility(View.GONE);
            editTitle.setVisibility(View.GONE);
            addCommentBtn.setVisibility(View.GONE);
        }

        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context.userDataModel.isLoggedIn() == false) {                           //check if user has logged in
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());   //if user hasnt, show pop up to login
                    builder.setPositiveButton(R.string.log_in, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {                   //send user to login page
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
                }else if(context.userDataModel.hasCooldown(Cooldown.COOLDOWN_TYPE_COMMENT, context.company.getId())){   //check if user has cooldown
                    Toast.makeText(context,"You have cooldown", Toast.LENGTH_SHORT).show();
                }else {
                    Comment comment = new Comment();                //create new comment
                    String Title = editTitle.getText().toString();
                    comment.setTitle(Title);
                    String Comment = editComment.getText().toString();
                    comment.setText(Comment);
                    if(Comment.length() > 0 && Title.length() > 0) {    //check if comment has title and text
                        context.company.getCommentsCollectionRef().add(comment);    //add comment to database
                        context.userDataModel.setCooldown(Cooldown.COOLDOWN_TYPE_COMMENT, context.company.getId()); //add comment cooldown for user
                        editTitle.getText().clear();
                        editComment.getText().clear();
                        editComment.setVisibility(View.GONE);
                        editTitle.setVisibility(View.GONE);
                        addCommentBtn.setVisibility(View.GONE);
                        addCommentBtn2.setVisibility(View.GONE);
                    }else {
                        String text = getString(R.string.add_title_and_comment);    //show toast if comment has no title or text
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
            }
        });
    }

    private void updateCommentsList(List<Comment> comments) {   //update comment list
        commentlist = comments;
        commentArrayAdapter = new CommentArrayAdapter(context,R.layout.comment_list_item, commentlist);
        listView.setAdapter(commentArrayAdapter);
    }
}