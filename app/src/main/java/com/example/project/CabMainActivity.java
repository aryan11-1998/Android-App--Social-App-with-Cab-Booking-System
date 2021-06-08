package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class CabMainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private String CurrentID, profileimage;
    private ProgressDialog loadingBar;
    TextView headerName;
    CircleImageView headerPic;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    SupportMapFragment mapFragment;
    FirebaseAuth mAuth;
    private DatabaseReference driversLocationRef, currentUserRef,onlineRef,reference;
    GeoFire geoFire;
    LocationManager manager;
    private final int MIN_TIME = 5000;
    private final int MIN_DIST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_main);

        mAuth = FirebaseAuth.getInstance();
        CurrentID = mAuth.getCurrentUser().getUid();
       currentUserRef = FirebaseDatabase.getInstance().getReference().child("DriversLocation").child(CurrentID);
        driversLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversLocation");
       onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
      // geoFire = new GeoFire(driversLocationRef);
        reference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").child(CurrentID);

        manager = (LocationManager)getSystemService(LOCATION_SERVICE);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        initi();
        getLocationUpdates();
      //  registerOnlineSystem();
       // readChanges();
    }

    private void getLocationUpdates() {
        if(manager!=null){
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED &&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME,MIN_DIST, (android.location.LocationListener) this);
                }else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME,MIN_DIST, (android.location.LocationListener) this);
                }else{
                    Toast.makeText(CabMainActivity.this,"No provider Enabled.", Toast.LENGTH_SHORT).show();
                }
            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode ==101){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLocationUpdates();
            }else {
                Toast.makeText(CabMainActivity.this,"Permission Required.", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void readChanges() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location!=null){

                            LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));


                            /*
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                            LatLng myloc = new LatLng(location.getLatitude(),location.getLongitude());
                            myMarker = mMap.addMarker(new MarkerOptions().position(myloc).title("My Location"));
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            myMarker.remove();*/
                        }
                    }catch (Exception e){
                        Toast.makeText(CabMainActivity.this,"kya ye??"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initi() {
        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback();
     /*  DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Locations").child("Drivers Available").child(CurrentID);
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
//////// yaha problem hai  neche vale ko try me daala tha but vo catch me jo toast tha usko show kar rha tha
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null) {
                            LatLng newPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));
                            //Update Location
                            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    new GeoLocation(location.getLatitude(), location.getLongitude()),
                                    (key, error) -> {
                                        if(error!=null){
                                            Snackbar.make(mapFragment.getView(),"yee vala error?"+error.getMessage(),Snackbar.LENGTH_SHORT).show();
                                        }else{
                                            Snackbar.make(mapFragment.getView(),"You Are Online",Snackbar.LENGTH_SHORT).show();
                                        }
                                    });

                           /* myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                            LatLng myloc = new LatLng(location.getLatitude(),location.getLongitude());
                            myMarker = mMap.addMarker(new MarkerOptions().position(myloc).title("My Location"));
                            mMap.getUiSettings().setZoomControlsEnabled(true);
                            mMap.getUiSettings().setAllGesturesEnabled(true);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myloc));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            myMarker.remove();*/
   //                     }


 //               }
 //           }

         //   @Override
       //     public void onCancelled(@NonNull DatabaseError error) {

     //       }
   //     });*/

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CabMainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
         fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

  /*  ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                currentUserRef.onDisconnect().removeValue();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
             Snackbar.make(mapFragment.getView(),"aree bhai"+error.getMessage(),Snackbar.LENGTH_SHORT).show();
        }
    };*/

    @Override
    protected void onDestroy() {
      //  fusedLocationProviderClient.removeLocationUpdates(locationCallback);
       // geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        //reference.removeEventListener(onlineValueEventListener);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
    //    registerOnlineSystem();
       // readChanges();
        super.onResume();
    }

  /*  private void registerOnlineSystem() {
      //   onlineRef.addValueEventListener(onlineValueEventListener);
    }
*/
    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cab_nav_payment:
                //Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                //startActivity(intentSetting);
                Toast.makeText(this, "Payments", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cab_nav_trips:
                //Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                //startActivity(intentSetting);
                Toast.makeText(this, "Trips", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cab_nav_settings:
                //Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                //startActivity(intentSetting);
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.cab_nav_logout:
                // UpdateUserStatus("offline");
                Intent logoutIntent = new Intent(CabMainActivity.this, WelcomeCabActivity.class);
                // FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").setValue("null");
                // reference.removeValue();

                startActivity(logoutIntent);
                finish();
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                //  mAuth.signOut();
                // SendUserToLoginActivity();
                break;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Dexter.withContext(getBaseContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(CabMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CabMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {

                            if (ActivityCompat.checkSelfPermission(CabMainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CabMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                               // return ;
                            }
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                                LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,18f));
                                //---------------------------
                               // mMap.getUiSettings().setZoomControlsEnabled(true);
                               // mMap.getUiSettings().setAllGesturesEnabled(true);
                               // mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                //------------------------
                            }).addOnFailureListener(e -> Toast.makeText(CabMainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                            return true;
                        });

                        //set Layout BUtton
                        View locationButton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1"))
                        .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = ( RelativeLayout.LayoutParams) locationButton.getLayoutParams();

                        //Right Bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,0,50);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(CabMainActivity.this, "Permission"+permissionDeniedResponse.getPermissionName()+" was denied.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

    }


    @Override
    public void onLocationChanged(Location location) {
        if(location !=null){
            saveLoaction(location);
        }else{
            Toast.makeText(CabMainActivity.this,"No Location",Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoaction(Location location) {
        reference.setValue(location);
    }
}