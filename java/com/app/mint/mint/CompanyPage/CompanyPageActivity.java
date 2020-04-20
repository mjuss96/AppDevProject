package com.app.mint.mint.CompanyPage;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.mint.mint.AddCompany.AddCompanyActivity;
import com.app.mint.mint.LoginAndRegister.LoginActivity;
import com.app.mint.mint.Main.MainActivity;
import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.DbModels.Comment;
import com.app.mint.mint.Models.DbModels.Company;
import com.app.mint.mint.Models.CompanyPageViewModel;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.OnBoarding.OnBoardingActivity;
import com.app.mint.mint.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class CompanyPageActivity extends AppCompatActivity {

    public static String COMPANY_ID_EXTRA = "company_id";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Button priceListBtn;
    private Button ratingsBtn;
    private Button infoBtn;
    private TextView companyName;
    Company company = new Company();
    MutableLiveData<List<Comment>> commentsList = new MutableLiveData<>();
    CompanyPageViewModel viewModel;
    UserDataModel userDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_page);
        setupToolbarAndDrawer();

        viewModel = ViewModelProviders.of(this).get(CompanyPageViewModel.class);
        userDataModel = ((MintApplication) getApplicationContext()).getUserData();

        infoBtn = findViewById(R.id.infoBtn);
        ratingsBtn = findViewById(R.id.ratingsBtn);
        priceListBtn = findViewById(R.id.priceListBtn);
        companyName = findViewById(R.id.companyName);

        viewModel.getCommentsLiveData().observe(this, new Observer<List<Comment>>() {   //check if there is a new comment
            @Override
            public void onChanged(@Nullable List<Comment> comments) {   //update the comment list if there is a new comment
                commentsList.setValue(comments);
            }
        });

        priceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new PriceListFragment());  //change the fragment shown
            }
        });

        ratingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new RatingsFragment());    //change the fragment shown
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new InfoFragment());   //change the fragment shown
            }
        });

        Company.companyCollectionRef.document(getIntent().getStringExtra(COMPANY_ID_EXTRA)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) { //get which company to show
                if (task != null) {
                    DocumentSnapshot document = task.getResult();
                    company = document.toObject(Company.class).withId(document.getId());
                    companyName.setText(company.getName()); //set company name in view
                    viewModel.setCompany(company);
                    loadFragment(new InfoFragment());   //show InfoFragment, infoFragment is the default fragment
                    updateUi();
                }
            }
        });

        toolbar = findViewById(R.id.activityToolBar); //setup toolbar
        toolbar.setNavigationIcon(R.drawable.ic_main_menu);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //setup drawer menu
                if (!drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.openDrawer(Gravity.START);
                } else {
                    drawerLayout.closeDrawer(Gravity.START);
                }
            }
        });
    }

    private void updateUi() {   //get pricelist if there is one and show button for it
        if(company.getPrice_list() != null) {
            priceListBtn.setVisibility(View.VISIBLE);
        }
    }

    private void loadFragment(Fragment fragment) {
    // create a FragmentManager
        FragmentManager fm = getFragmentManager();
    // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
    // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }

    private void setupToolbarAndDrawer() {  //more setup for drawer menu and toolbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }

        drawerLayout = findViewById(R.id.mainActivityDrawerLayout);
        navigationView = findViewById(R.id.navigation_drawer);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int id = item.getItemId();
                switch (id) {
                    case R.id.drawer_menu_add_company:
                        startActivity(new Intent(CompanyPageActivity.this, AddCompanyActivity.class));
                        break;
                    case R.id.drawer_menu_followed_companies:
                        startActivity(new Intent(CompanyPageActivity.this, MainActivity.class));
                        break;
                    case R.id.drawer_menu_locate:
                        startActivity(new Intent(CompanyPageActivity.this, OnBoardingActivity.class));
                        break;
                    case R.id.drawer_menu_log_out:
                        // logout
                        break;
                    case R.id.drawer_menu_log_in:
                        startActivity(new Intent(CompanyPageActivity.this, LoginActivity.class));
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //more setup for toolbar
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if(userDataModel.isFollowingCompany(getIntent().getStringExtra(COMPANY_ID_EXTRA))){
            menu.getItem(0).setIcon(R.drawable.ic_favorite);
        }else {
            menu.getItem(0).setIcon(R.drawable.ic_favorite_border);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {   //more setup for toolbar
        int id = item.getItemId();

        switch (id) {
            case R.id.main_menu_search:
                if(!userDataModel.isFollowingCompany(company.getId())){
                    userDataModel.followCompany(company);
                    item.setIcon(R.drawable.ic_favorite);
                }
                else {
                    userDataModel.stopFollowingCompany(company);
                    item.setIcon(R.drawable.ic_favorite_border);
                }
                break;
        }
        return true;
    }
}