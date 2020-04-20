package com.app.mint.mint.LoginAndRegister;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.mint.mint.Main.MainActivity;
import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.Models.DbModels.UserInDb;
import com.app.mint.mint.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class RegisterFragment extends Fragment {

    private View view;
    LoginActivity context;
    private Button registerBtn;
    private EditText signInEmailEditText;
    private EditText signInPasswordEditText;
    private EditText signInRePasswordEditText;

    static final String KEY_USERS = "users";

    UserDataModel userDataModel;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    DocumentReference documentReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = (LoginActivity) container.getContext();
        view = inflater.inflate(R.layout.fragment_register, container, false);
        signInEmailEditText = view.findViewById(R.id.register_email_editText);
        signInPasswordEditText = view.findViewById(R.id.register_password_editText);
        signInRePasswordEditText = view.findViewById(R.id.register_rewritePassword_editText);

        userDataModel = ((MintApplication) context.getApplicationContext()).getUserData();
        db = FirebaseFirestore.getInstance();
        mAuth = userDataModel.getUserAuthentication();

        registerBtn = view.findViewById(R.id.createAcc_button);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signInPassword = signInPasswordEditText.getText().toString();
                String signInRePassword = signInRePasswordEditText.getText().toString();

                //Checks if password length is between 6 and 20 characters and contains at least one number.
                if (signInPassword.matches(".*[0-9]{1,}.*") && signInPassword.length() >= 6 && signInPassword.length() <= 20) {
                    //Checks if password field doesn't match to ReWrite password field and makes toast message to user.
                    if (!signInPassword.equals(signInRePassword)) {
                        Toast.makeText(context, getText(R.string.error_passwords_does_not_match), Toast.LENGTH_SHORT).show();
                    } else {
                        makeAccount();
                    }
                } else {
                    Toast.makeText(context, getText(R.string.error_password_validation), Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    //Saves new account to database and check email-address validation.
    public void makeAccount() {
        String email = signInEmailEditText.getText().toString().trim(); //Trim edits all Uppercase letters to lowercase
        String password = signInRePasswordEditText.getText().toString();
        //Creates new user with email and passwords.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uId = mAuth.getUid(); //Gets users id and saves it to uId
                            documentReference = db.collection(KEY_USERS).document(uId); //Makes new document to database what matches to accounts user id
                            UserInDb user = new UserInDb();

                            //Saves user to database and makes toast message to user.
                            documentReference.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(context, getText(R.string.account_made), Toast.LENGTH_SHORT).show();
                                    if(context.getCallingActivity() != null){
                                        context.finish();
                                    }else{
                                        startActivity(new Intent(context, MainActivity.class));
                                        context.finish();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(context, getText(R.string.error_invalid_email), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
