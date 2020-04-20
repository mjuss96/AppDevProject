package com.app.mint.mint.AddCompany;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Main.MainActivity;
import com.app.mint.mint.Models.CompaniesDataModel;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.OnBoarding.OnBoardingActivity;
import com.app.mint.mint.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddCompanyActivity extends AppCompatActivity {

    //In this activity users add the companies to database

    private Button searchMap;
    private Button sendData;
    private TextView returnedAddress;
    private EditText companyNameEditText;
    private String address;
    private Double lat;
    private Double lon;
    private CollectionReference companyCollectionRef = Company.companyCollectionRef;
    private CheckBox checkBoxNails, checkBoxHair, checkBoxMakeup;
    private CheckBox checkBoxPedicure, checkBoxManicure, checkBoxFacial;
    private CheckBox checkBoxSolarium, checkBoxEyelashes, checkBoxEyebrows;
    static final int ACTIVITY_RESULT_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_company);
        searchMap = findViewById(R.id.searchMapButton);
        returnedAddress = findViewById(R.id.addressText);
        companyNameEditText = findViewById(R.id.companyNameEditText);
        checkBoxMakeup = findViewById(R.id.checkBoxMakeup);
        checkBoxHair = findViewById(R.id.checkBoxHair);
        checkBoxNails = findViewById(R.id.checkBoxNails);
        checkBoxPedicure = findViewById(R.id.checkBoxPedicure);
        checkBoxManicure = findViewById(R.id.checkBoxManicure);
        checkBoxFacial = findViewById(R.id.checkBoxFacial);
        checkBoxSolarium = findViewById(R.id.checkBoxSolarium);
        checkBoxEyelashes = findViewById(R.id.checkBoxEyelashes);
        checkBoxEyebrows = findViewById(R.id.checkBoxEyebrows);

        setupToolbarAndDrawer();

        //Search button, when clicked will wait for address result
        searchMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(AddCompanyActivity.this, LocationPickActivity.class), ACTIVITY_RESULT_ID);
            }
        });

        sendData = findViewById(R.id.sendCompanyInfo);
        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //sends the company data to database

                //Checking if categories are checked and then are added to database
                String name = companyNameEditText.getText().toString();
                final boolean nailsChecked = checkBoxNails.isChecked();
                final boolean makeupChecked = checkBoxMakeup.isChecked();
                final boolean hairChecked = checkBoxHair.isChecked();
                final boolean pedicureChecked = checkBoxPedicure.isChecked();
                final boolean manicureChecked = checkBoxManicure.isChecked();
                final boolean facialChecked = checkBoxFacial.isChecked();
                final boolean solariumChecked = checkBoxSolarium.isChecked();
                final boolean eyelashesChecked = checkBoxEyelashes.isChecked();
                final boolean eyebrowsChecked = checkBoxEyebrows.isChecked();

                List<String> categoryFilters = new ArrayList<>();

                if(nailsChecked)categoryFilters.add(Company.CATEGORY_NAILS);
                if(makeupChecked)categoryFilters.add(Company.CATEGORY_MAKEUP);
                if(hairChecked)categoryFilters.add(Company.CATEGORY_HAIR);
                if(pedicureChecked)categoryFilters.add(Company.CATEGORY_PEDICURE);
                if(manicureChecked)categoryFilters.add(Company.CATEGORY_MANICURE);
                if(facialChecked)categoryFilters.add(Company.CATEGORY_FACIAL);
                if(solariumChecked)categoryFilters.add(Company.CATEGORY_SOLARIUM);
                if(eyelashesChecked)categoryFilters.add(Company.CATEGORY_EYELASHES);
                if(eyebrowsChecked)categoryFilters.add(Company.CATEGORY_EYEBROWS);

                //Checks if given information is empty
                if (name.equals("") || lat == null || lon == null) {
                    Toast toast = Toast.makeText(AddCompanyActivity.this, R.string.adding_company_failed, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                //adds the company data to database
                final Company company = new Company(name, address, categoryFilters, lat, lon);
                companyCollectionRef.whereEqualTo("name", name).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                            if (documents.size() == 0) {
                                companyCollectionRef.add(company);
                                Toast toast = Toast.makeText(AddCompanyActivity.this, R.string.company_added_success, Toast.LENGTH_LONG);
                                toast.show();
                                CompaniesDataModel.getInstance().updateCurrentCompanies();
                            }
                            else {
                                Toast toast = Toast.makeText(AddCompanyActivity.this, R.string.company_name_taken, Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                    }
                });
                finish();
            }
        });
    }



    private void setupToolbarAndDrawer() { //sets the navigation menu and the buttons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }

        final DrawerLayout drawerLayout = findViewById(R.id.mainActivityDrawerLayout);
        NavigationView navigationView = findViewById(R.id.navigation_drawer);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_menu_add_company:
                        startActivity(new Intent(AddCompanyActivity.this, AddCompanyActivity.class));
                        break;
                    case R.id.drawer_menu_followed_companies:
                        startActivity(new Intent(AddCompanyActivity.this, MainActivity.class));
                        break;
                    case R.id.drawer_menu_locate:
                        startActivity(new Intent(AddCompanyActivity.this, OnBoardingActivity.class));
                        break;
                    case R.id.drawer_menu_log_out:
                        // logout
                        break;
                    case R.id.drawer_menu_log_in:
                        startActivity(new Intent(AddCompanyActivity.this, LoginActivity.class));
                        break;
                }
                return true;
            }
        });

        Toolbar toolbar = findViewById(R.id.activityToolBar);
        toolbar.setNavigationIcon(R.drawable.ic_main_menu);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.openDrawer(Gravity.START);
                } else {
                    drawerLayout.closeDrawer(Gravity.START);
                }
            }
        });
    }


    //is used after location has been selected from LocationPickActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_RESULT_ID && resultCode == RESULT_OK) {
            returnedAddress.setText(data.getSerializableExtra("address").toString());
            address = returnedAddress.getText().toString();
            lat = data.getDoubleExtra("latitude", 0);
            lon = data.getDoubleExtra("longitude", 0);

        }
    }
}
