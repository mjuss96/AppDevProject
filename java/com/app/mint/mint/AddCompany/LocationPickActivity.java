package com.app.mint.mint.AddCompany;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.mint.mint.Main.MintApplication;
import com.app.mint.mint.Models.UserDataModel;
import com.app.mint.mint.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText addressLocationEditText;
    private Button acceptLocationBtn;
    private Button searchLocationBtn;
    private String addressToSend;
    private String locationToSearchString;
    private Boolean searchMode;
    private double latitude;
    private double longitude;
    private UserDataModel userDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_pick);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userDataModel = ((MintApplication) getApplication()).getUserData();

        addressLocationEditText = findViewById(R.id.nearestAddress);
        acceptLocationBtn = findViewById(R.id.acceptAddress);
        searchLocationBtn = findViewById(R.id.searchAddress);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  //Used for loading the google maps
        mMap = googleMap;

        try {
            boolean success = mMap.setMapStyle( //setting the visual style of map
                    MapStyleOptions.loadRawResourceStyle(
                            LocationPickActivity.this, R.raw.custommap));

            if (!success) {
                Log.e("fail", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("fail", "Can't find style. Error: ", e);
        }

        final LatLng userLocation = new LatLng(userDataModel.getUserGeoPoint().getValue().getLatitude(),
                userDataModel.getUserGeoPoint().getValue().getLongitude());  //getting the latest user location and moving the camera to it
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


            @Override
            public void onMapClick(LatLng latLng) { //checks the clicked area and if valid address is found, it will be shown

                //When user clicks on the map, the address is located and marker is placed
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(LocationPickActivity.this, Locale.getDefault());
                try {
                    String sPlace;
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3);

                    //Checks if clicked area is out of bounds (cant find any address)
                    if(addresses != null && addresses.size()>0) {

                        mMap.clear();

                        latitude = latLng.latitude;
                        longitude = latLng.longitude;

                        String address = addresses.get(0).getAddressLine(0);
                        String city = addresses.get(0).getAddressLine(1);

                        String[] splitAddress = address.split(",");
                        sPlace = splitAddress[0] + "\n";
                        if (city != null && !city.isEmpty()) {
                            String[] splitCity = city.split(",");
                            sPlace += splitCity[0];
                        }

                        //checks if address is valid
                        if (address.startsWith("Unnamed Road") || splitAddress.length <= 2 ) {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.address_is_not_valid, Toast.LENGTH_SHORT);
                            toast.show();

                            return;
                        }
                        addressLocationEditText.setText(sPlace);
                        if (searchMode) {
                            acceptLocationBtn.setEnabled(true);
                            acceptLocationBtn.setVisibility(View.VISIBLE);
                            searchLocationBtn.setVisibility(View.GONE);
                            searchLocationBtn.setEnabled(false);
                            searchMode = false;
                        }
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(city)
                                .snippet(address));

                        addressToSend = address;

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        addressLocationEditText.addTextChangedListener(new TextWatcher() { //checks if address text is changed and changes the button
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Changes the OK button to search Button
                acceptLocationBtn.setEnabled(false);
                acceptLocationBtn.setVisibility(View.GONE);
                searchLocationBtn.setVisibility(View.VISIBLE);
                searchLocationBtn.setEnabled(true);
                searchMode = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        acceptLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Returns the address back to previous activity
                if (addressToSend == null) {
                    Toast toast = Toast.makeText(LocationPickActivity.this, R.string.address_is_not_valid, Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("address", addressToSend);
                returnIntent.putExtra("latitude", latitude);
                returnIntent.putExtra("longitude", longitude);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        searchLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  //Searches the given address from Google Maps
                locationToSearchString = addressLocationEditText.getText().toString();
                mMap.clear();

                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(LocationPickActivity.this, Locale.getDefault());
                Address chosenAddress = null;

                try {
                    addresses = geocoder.getFromLocationName(locationToSearchString, 3);
                    for (Address address : addresses) {
                        if(address != null){
                            String cityCountry = address.getLocality() + ", " + address.getCountryName();
                            if(cityCountry.equals(userDataModel.getUserLocation())){
                                chosenAddress = address;
                            }
                        }
                    }
                    if(chosenAddress == null){
                        chosenAddress = addresses.get(0);
                    }

                    if (addresses.get(0).getAddressLine(0).startsWith(userDataModel.getUserLocation())) {
                        addresses = geocoder.getFromLocationName(locationToSearchString, 3); }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if(addresses == null || addresses.size() == 0 || chosenAddress == null){ //checks if address is valid
                    Toast toast = Toast.makeText(getApplicationContext(),"Not found", Toast.LENGTH_SHORT);
                    toast.show();

                    return;
                }
                Address userAddress = chosenAddress;
                LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                if (searchMode) { //changes the button to "accept" if it wasn't previously
                    acceptLocationBtn.setEnabled(true);
                    acceptLocationBtn.setVisibility(View.VISIBLE);
                    searchLocationBtn.setVisibility(View.GONE);
                    searchLocationBtn.setEnabled(false);
                    searchMode = false;
                }
                String userAddressToSend = addresses.get(0).getAddressLine(0);
                addressToSend = userAddressToSend;

                latitude = latLng.latitude;
                longitude = latLng.longitude;

            }
        });
    }
}
