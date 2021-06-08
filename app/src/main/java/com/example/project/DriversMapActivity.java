package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
//import com.google.android.gms.location.places.Place;
//import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DriversMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private GoogleMap mMap;
    private DatabaseReference reference;
    FirebaseAuth mAuth;
    String currentUserId;
    private final int MIN_TIME = 5000;
    private final int MIN_DISTANCE = 1;
    private LocationManager manager;
    Marker myMarker;
    private Button LogoutDriverBtn, SettingsDriverBtn;
    private Boolean currentLogoutDriverStatus = false;
    // int myloc;
    LatLng myloc, endLatLng;
    Marker currentMarker, destMarker;
    private Button SourceBtn,DestBtn;
    private static String TAG = "Info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers_map);

       mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").child(currentUserId);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SettingsDriverBtn =(Button) findViewById(R.id.driver_setting_btn);
        LogoutDriverBtn =(Button) findViewById(R.id.driver_logout_btn);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationUpdates();

     readChanges();

        LogoutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(DriversMapActivity.this,WelcomeCabActivity.class);
                logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                reference.removeValue();
                startActivity(logoutIntent);
                finish();
            }
        });

    }

    private void readChanges() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);

                        if (location!=null){
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                            LatLng myloc = new LatLng(location.getLatitude(),location.getLongitude());
                            myMarker = mMap.addMarker(new MarkerOptions().position(myloc).title("My Location"));
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            Geocoder geocoder = new Geocoder(DriversMapActivity.this, Locale.getDefault());
                            try {
                                List<Address> myaddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                                String address = myaddress.get(0).getAddressLine(0);
                                String city = myaddress.get(0).getLocality();
                                SourceBtn.setText(address+" "+city);
                            }catch (IOException e){
                                e.printStackTrace();
                            }

                            myMarker.remove();
                            currentMarker.remove();
                        }else{
                            currentMarker.setPosition(myloc);

                        }
                    }catch (Exception e){
                        Toast.makeText(DriversMapActivity.this," ", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLocationUpdates() {
        if(manager!=null){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                } else {
                    Toast.makeText(this, "No provider Enabled", Toast.LENGTH_SHORT).show();
                }
            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101){
            if(grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }else{
                Toast.makeText(this, "Permission Required.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        int n =50;
        LatLng sydney = new LatLng(29.945690, 78.164246);

        myMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("My Location"));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


    @Override
    public void onLocationChanged(Location location) {
        if(location !=null){
            saveLoaction(location);
          //  MoveMarker(location);
        }else{
            Toast.makeText(this,"No Location",Toast.LENGTH_LONG).show();
        }
    }

    private void MoveMarker(Location location) {
    }

    private void saveLoaction(Location location) {
        reference.setValue(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStop() {

        reference.removeValue();
        super.onStop();
    }

}