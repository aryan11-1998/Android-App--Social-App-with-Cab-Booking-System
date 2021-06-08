package com.example.project;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Permission;
import java.util.HashMap;
import java.util.List;

public class CustMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private final int MIN_TIME = 5000;
    private final int MIN_DISTANCE = 1;
    private LocationManager manager;
    Marker myMarker, DriverMarker;
    private DatabaseReference reference, CustomerDatabaseReference, driverAvailaibleRef,driverRef, driverLocationRef;
    FirebaseAuth mAuth;
    String currentUserId;
    private Button LogoutCustBtn, SettingscustBtn , CallCab;
    LatLng custPickupLocation;
    private  int radius = 1;
    private  boolean driverfound = false;
    private String driverfoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cust_map);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Customer").child(currentUserId);
        CustomerDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Customer");
        driverAvailaibleRef = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").child(currentUserId);
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Working");
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SettingscustBtn =(Button) findViewById(R.id.customer_setting_btn);
        LogoutCustBtn =(Button) findViewById(R.id.customer_logout_btn);
        CallCab = (Button) findViewById(R.id.call_a_cab_btn);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationUpdates();
        readChanges();

        LogoutCustBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(CustMapActivity.this,WelcomeCabActivity.class);
               // FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").setValue("null");
                reference.removeValue();

                startActivity(logoutIntent);
                finish();
            }
        });

        CallCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyLocation location = new MyLocation();
                GeoFire geoFire = new GeoFire((reference));
                geoFire.setLocation(currentUserId,new GeoLocation(location.getLatitude(),location.getLongitude()));
                custPickupLocation = new LatLng(location.getLatitude(),location.getLongitude());
                myMarker.remove();
                mMap.addMarker(new MarkerOptions().position(custPickupLocation).title("Customer Pickup"));

                CallCab.setText("Searching...");

                getClosestDriverCab();
            }
        });
    }

    private void getClosestDriverCab() {
        MyLocation location = new MyLocation();
        GeoFire geoFire = new GeoFire(driverAvailaibleRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverfound){
                    driverfound = true;
                    driverfoundId = key;

                    driverRef = FirebaseDatabase.getInstance().getReference().child("Cab Details").child("Drivers").child(driverfoundId);
                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID",currentUserId);
                    driverRef.updateChildren(driverMap);


                    GettingDriverLocation();
                    CallCab.setText("Getting Driver Location...");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverfound){
                    radius = radius+1;
                    getClosestDriverCab();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation() {
        driverLocationRef.child(driverfoundId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();
                            double LocationLat = 0;
                            double LocationLong = 0;
                            CallCab.setText("Driver Found.");

                            if(driverLocationMap.get(9)!=null){
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if(driverLocationMap.get(10)!=null){
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }

                            LatLng DriverLatLng = new LatLng(LocationLat,LocationLong);
                            if(DriverMarker!=null){
                                DriverMarker.remove();
                            }

                            Location location1 = new Location("");
                            location1.setLatitude(custPickupLocation.latitude);
                            location1.setLongitude(custPickupLocation.longitude);

                            Location location2 = new Location("");
                            location2.setLatitude(DriverLatLng.latitude);
                            location2.setLongitude(DriverLatLng.longitude);

                            float Distance = location1.distanceTo(location2);
                            CallCab.setText("Driver Found"+String.valueOf(Distance));


                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Your Driver"));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
                            myMarker.remove();
                        }
                    }catch (Exception e){
                        Toast.makeText(CustMapActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

        if(location!=null){
            saveLocation(location);
        }else{
            Toast.makeText(this, "No Location", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveLocation(Location location) {
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