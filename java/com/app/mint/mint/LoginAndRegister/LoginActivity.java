package com.app.mint.mint.LoginAndRegister;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.mint.mint.Main.MainActivity;
import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    private ConstraintLayout loginLayout;
    private Button loginBtn;
    private Button joinBtn;
    private EditText logInUserEditText;
    private EditText logInPasswordEditText;
    UserDataModel userDataModel;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDataModel = ((MintApplication) getApplicationContext()).getUserData();
        mAuth = userDataModel.getUserAuthentication();

        loginLayout = findViewById(R.id.login_layout);
        logInUserEditText = findViewById(R.id.userET);
        logInPasswordEditText = findViewById(R.id.passwordET);

        loginBtn = findViewById(R.id.login_button);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });

        joinBtn = findViewById(R.id.join_button);
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLayout.setVisibility(View.GONE);
                loadFragment(new RegisterFragment()); //When user press the joinBtn, LoginActivity will show RegisterFragment view.
            }
        });
    }

    //User Log in with email address and password.
    public void logIn() {

        String email = logInUserEditText.getText().toString().trim(); //Trim edits all Uppercase letters to lowercase
        String password = logInPasswordEditText.getText().toString();

        // "if" sentence checks if password or email field is empty and makes toast message to user.
        if(email.equals("") || password.equals("")){
            Toast.makeText(this, getText(R.string.error_invalid_email_or_password), Toast.LENGTH_SHORT).show();
            return;
        }

        //Makes actual login with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (getCallingActivity() != null) {
                                finish();
                            } else {
                                startActivity(new Intent(LoginActivity.this, MainActivity.class)); //When login is successfully done user goes to MainActivity.
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, getText(R.string.error_invalid_email_or_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //If user have not registered to Mint it happens via fragment
    void loadFragment(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getFragmentManager();
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }
}