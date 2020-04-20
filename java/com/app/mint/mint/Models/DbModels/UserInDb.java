package com.app.mint.mint.Models.DbModels;

import android.util.Log;

import com.app.mint.mint.Models.UserDataModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class UserInDb extends Model {
    public static final String USER_EXP_FIELD_NAME = "user_exp";
    public static final String COOLDOWNS_FIELD_NAME = "cooldowns";
    public static final String FOLLOWED_COMPANIES_FIELD_NAME = "followed_companies";

    List<Map<String, Object>> cooldowns = new ArrayList<>();
    List<String> followed_companies = new ArrayList<>();
    int user_exp = 0;

    @Exclude
    List<Cooldown> cooldownsList = new ArrayList<>();

    // FireStore requires empty public constructor
    public UserInDb(){
    }


    public UserInDb(String id){
        super.id = id;
    }


    public void startListeningDataBase(){
        Log.d("USER", " adding EventListener for " + this.getId());
        getSelfReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d("USER", "change in db detected");

                if(documentSnapshot != null && documentSnapshot.exists()){
                    UserInDb updatedUser = documentSnapshot.toObject(UserInDb.class);

                    /*
                        UPDATE USER FIELDS HERE
                     */

                    //updating cooldowns
                    updateCooldowns(updatedUser.getCooldowns());

                }
            }
        });
    }

    public void addCooldown(Cooldown cooldown){
        if(cooldown == null) return;
        cooldownsList.add(cooldown);

        Map<String, Object> cooldownMap = new HashMap<>();

        if(cooldown instanceof RatingCooldown){
            cooldownMap.put(Cooldown.TYPE_FIELD_KEY,Cooldown.COOLDOWN_TYPE_RATING);
        }else{
            cooldownMap.put(Cooldown.TYPE_FIELD_KEY,Cooldown.COOLDOWN_TYPE_COMMENT);
        }

        cooldownMap.put(Cooldown.TIMESTAMP_FIELD_KEY, cooldown.getTimestamp());
        cooldownMap.put(Cooldown.COMPANY_ID_FIELD_KEY, cooldown.getCompany_id());

        this.cooldowns.add(cooldownMap);
        this.selfReference.update(COOLDOWNS_FIELD_NAME, this.cooldowns);
    }

    public void addFollowedCompany(String companyId){
        this.followed_companies.add(companyId);
        this.getSelfReference().update(FOLLOWED_COMPANIES_FIELD_NAME,followed_companies);
    }

    public void removeFollowedCompany(String companyId){
        if(followed_companies.contains(companyId)){
            this.followed_companies.remove(companyId);
            this.getSelfReference().update(FOLLOWED_COMPANIES_FIELD_NAME,followed_companies);
        }
    }



    private void updateCooldowns(List<Map<String,Object>> updatedCooldownsMap){
        if(updatedCooldownsMap == null) return;
        List<Cooldown> updatedCooldownList = new ArrayList<>();

        for (Map<String,Object> map: updatedCooldownsMap) {
            updatedCooldownList.add(Cooldown.mapToCooldown(map));
        }

        //update the Cooldowns list if different than old
        if(!this.cooldownsList.equals(updatedCooldownList)){
            this.cooldowns = updatedCooldownsMap;
            this.cooldownsList = updatedCooldownList;
        }
    }

    @Exclude
    public List<Cooldown> getCooldownsList(){
        return this.cooldownsList;
    }



    /*
        FOR FIRESTORE SDK
     */

    // only used to get the cooldowns from database after user logs in
    public void setCooldowns(List<Map<String, Object>> cooldowns) {
        this.cooldowns = cooldowns;
    }

    // for posting the cooldowns to database
    public List<Map<String, Object>> getCooldowns() {
        return cooldowns;
    }

    public int getUser_exp() {
        return user_exp;
    }

    public void setUser_exp(int user_exp) {
        this.user_exp = user_exp;
    }

    public List<String> getFollowed_companies() {
        return followed_companies;
    }

    public void setFollowed_companies(List<String> followed_companies) {
        this.followed_companies = followed_companies;
    }




    // getter function for databaseID, returns value from base class.
    @Exclude
    public String getId(){
        return super.id;
    }

    @Exclude
    public DocumentReference getSelfReference() {
        DocumentReference reference = null;
        if(super.selfReference == null){
            if(super.id != null){
                reference = UserDataModel.usersCollectionReference.document(super.id);
                super.selfReference = reference;
            }
        }else {
            reference = super.selfReference;
        }
        return reference;
    }
}
