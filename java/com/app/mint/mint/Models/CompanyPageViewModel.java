package com.app.mint.mint.Models;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.app.mint.mint.Models.DbModels.Comment;
import com.app.mint.mint.Models.DbModels.Company;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/*
    ViewModel for CompanyPageActivity
 */

public class CompanyPageViewModel extends ViewModel {
    private MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();
    private Company company;


    public MutableLiveData<List<Comment>> getCommentsLiveData() {
        return commentsLiveData;
    }

    public Company getCompany() {
        return company;
    }

    // when company has been set, updateCommentsList will be called
    public void setCompany(Company company) {
        this.company = company;
        updateCommentsList();
    }

    // fetching comments from database
    // and setting up listener for collection
    private void updateCommentsList() {
        company.getCommentsCollectionRef().addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                List<Comment> commentsList = new ArrayList<>();
                if (e == null) {
                    for (QueryDocumentSnapshot doc : value) {
                        Comment comment = doc.toObject(Comment.class);
                        if (comment != null) {
                            commentsList.add(comment);
                        }
                    }
                    commentsLiveData.setValue(commentsList);
                }
            }
        });
    }
}
