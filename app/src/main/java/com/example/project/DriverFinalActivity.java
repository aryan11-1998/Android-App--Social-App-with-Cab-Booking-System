package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverFinalActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;  //
    FusedLocationProviderClient fusedLocationProviderClient; //
    Double CurrentLat, CurrentLong; //
    Location lastLocation; //
    private Button LogoutDriverBtn, SettingsDriverBtn; //
    private FirebaseAuth mAuth; //
    private FirebaseUser currentUser;//
    private Boolean currentLogoutDriverStatus = false; //
    private DatabaseReference AssignedCustRef, AssignedCustPickupRef;
    private String DriverID, CustID ="";
    Marker pickupMarker; //pending
    LatLng CustPickupLocation; //pending
    LatLng PickupCustomer;
   // private ValueEventListener AssignedCustPickupRefListner; //pending

    private TextView txtName,txtPhone;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_final);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        DriverID = mAuth.getCurrentUser().getUid();

        LogoutDriverBtn =(Button)findViewById(R.id.driver_final_logout_btn);
        SettingsDriverBtn =(Button)findViewById(R.id.driver_final_setting_btn);

        txtName = findViewById(R.id.cab_name_cust);
        txtPhone = findViewById(R.id.cab_phone_cust);
        profilePic = (CircleImageView)findViewById(R.id.cab_profile_image_cust);
        relativeLayout = findViewById(R.id.rel2);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       // CustomerFinalActivity custPickupObject = new CustomerFinalActivity();
        //CustPickupLocation = custPickupObject.CustPickupLocation;
        //pickupMarker = new LatLng(CustPickupLocation,)


        LogoutDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLogoutDriverStatus = true;
                DisconnectTheDriver();
                Intent logout = new Intent(DriverFinalActivity.this,WelcomeCabActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                finish();
            }
        });

        GetAssignedCustomerRequest();
        SettingsDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(DriverFinalActivity.this,CabSettingsActivity.class);
                settings.putExtra("type","Drivers");
                startActivity(settings);
            }
        });

    }//on create END

    private void SetPickUpMarker() {

    }

    private void GetAssignedCustomerRequest() {
        AssignedCustRef = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Drivers").child(DriverID).child("CustomerRideID"); //

        AssignedCustRef.addValueEventListener(new ValueEventListener() {//
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {//
                if(snapshot.exists()){
                    CustID = snapshot.getValue().toString(); //
                    GetAssignedCustomerPickupLocation(); //

                    relativeLayout.setVisibility(View.VISIBLE);
                    getAssignedCustInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetAssignedCustomerPickupLocation() {
        AssignedCustPickupRef = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Customer Request").child(CustID).child("l");//

    /* AssignedCustPickupRefListner = AssignedCustPickupRef.addValueEventListener(new ValueEventListener() { //

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if( snapshot.exists()) {  //
                    List<Object> customerLocationMap  = (List<Object>)snapshot.getValue(); //
                    double LocationLat = 0; //
                    double LocationLong = 0;//
                    if(customerLocationMap.get(0)!=null){ //
                        LocationLat = Double.parseDouble(customerLocationMap.get(0).toString());
                    }
                    if(customerLocationMap.get(1)!=null){ //
                        LocationLong = Double.parseDouble(customerLocationMap.get(1).toString());
                    }
                    String toast = "Lat:"+LocationLat+", Lon:"+LocationLong;

                    Toast.makeText(DriverFinalActivity.this,"CustID:  "+ toast,Toast.LENGTH_LONG).show();
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLong); //
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Pickup Location.")); //
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("PickUpLocation"));

                }else{
                    Toast.makeText(DriverFinalActivity.this,"Aaya hi nahi",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }); */


     /*   AssignedCustPickupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    double LocationLat = 0; //
                    double LocationLong =0;
                    LocationLat =Double.parseDouble(snapshot.child("0").getValue().toString());
                    LocationLong  =Double.parseDouble(snapshot.child("1").getValue().toString());
                    LatLng DriverLatLng = new LatLng(LocationLat,LocationLong); //
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Pickup Location."));
                }else{
                    double LocationLat1 = 0; //
                    double LocationLong1 =0;
                    LocationLat1 =Double.parseDouble(snapshot.child("0").getValue().toString());
                    LocationLong1  =Double.parseDouble(snapshot.child("1").getValue().toString());
                    LatLng DriverLatLng1 = new LatLng(LocationLat1,LocationLong1); //
                    mMap.addMarker(new MarkerOptions().position(DriverLatLng1).title("Pickup Location."));
                    Toast.makeText(DriverFinalActivity.this,"Idhar bhi nhi aaya.",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        DatabaseReference faltuRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available").child(CustID);
       // AssignedCustPickupRefListner =
        faltuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String lati =  snapshot.child("latitude").getValue().toString();
                    String longii = snapshot.child("longitude").getValue().toString();
                    Double lat = Double.parseDouble(lati);
                    Double longi = Double.parseDouble(longii);
                    //GeoFire geoFire = new GeoFire(CustomerDatabaseRef); //
                    //geoFire.setLocation(CustID,new GeoLocation(lat,longi)); //
                    PickupCustomer = new LatLng(lat,longi); //
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(PickupCustomer).title("Customer Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user))); //
                    // upladed till now
                    //now to find driver.


                }else {
                    Toast.makeText(DriverFinalActivity.this,"Ab nhi aa rha hai smj",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng delhi = new LatLng(28.644800, 77.216721);
        mMap.addMarker(new MarkerOptions().position(delhi).title("Delhi"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi,10f));
        enableMyLocation();


    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            getCurrentLocation();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
    }
// provides long and latitude.
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        //inititalize location Manager
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //Check Condition
        if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            //When Location Service is enabled.
            //Get Last Location.
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        //Checl condiitom
                    if(location!=null){
                        //set Latitude
                        CurrentLat = location.getLatitude();
                        CurrentLong = location.getLongitude();
   //-------------------    //marking loc -------------------------------------- idhar se location mil rhi hai
                         LatLng myloc = new LatLng(CurrentLat, CurrentLong);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,16f));
                        mMap.setMyLocationEnabled(true);

                   //     String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   //     DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available");
                   //     GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
                   //     geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));

                  //      DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working");
                 //       GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);
                 //       geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));

                        //------------------------------------------------------------------------------------------------------------------------------------------------------
                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //
                        DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available"); //
                        GeoFire geoFire = new GeoFire(DriverAvailabilityRef);   //

                        DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working");
                        GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);

                        switch (CustID){
                            case "":
                               Toast.makeText(DriverFinalActivity.this,"CustID null",Toast.LENGTH_LONG).show();
                                geoFireWorking.removeLocation(userID);
                                geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                break;
                            default:
                                Toast.makeText(DriverFinalActivity.this,"CustID:  "+ CustID,Toast.LENGTH_LONG).show();
                                geoFire.removeLocation(userID);
                                geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                break;

                        }
                        // -----------------------------------------------------------------------------------------------------------------------------------------------------

                        /*switch (CustID){
                            case "":
                                geoFireWorking.removeLocation(userID);
                                geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                break;
                            default:
                                geoFire.removeLocation(userID);
                                geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                break;
                        }*/

                    }else{
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(5000)
                                .setFastestInterval(5000)
                                .setNumUpdates(1);
                        //initialize location CallBack
                        LocationCallback locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location = locationResult.getLastLocation();
                                CurrentLat = location.getLatitude();
                                CurrentLong = location.getLongitude();
                                LatLng myloc = new LatLng(CurrentLat, CurrentLong);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,16f));
                                mMap.setMyLocationEnabled(true);

                                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available");
                                GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
                                //geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));

                                DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working");
                                GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);
                                //geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));

                                switch (CustID){
                                    case "":
                                        Toast.makeText(DriverFinalActivity.this,"CustID2 null",Toast.LENGTH_LONG).show();
                                        geoFireWorking.removeLocation(userID);
                                        geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                        break;
                                    default:
                                        Toast.makeText(DriverFinalActivity.this,"CustID2:  "+ CustID,Toast.LENGTH_LONG).show();
                                        geoFire.removeLocation(userID);
                                        geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                                        break;
                                }


                            }
                        };
                        //Request Location Updates
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }
                }
            });
        }else{
            // When location service is not enables
            Toast.makeText(this,"Location Off",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }else{
                    Toast.makeText(this,"Permission Denied.",Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){  //
            lastLocation = location;  //
            LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude()); //
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12)); //

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //
            DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available"); //
            GeoFire geoFire = new GeoFire(DriverAvailabilityRef);   //

            DatabaseReference DriverWorkingRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working");
            GeoFire geoFireWorking = new GeoFire(DriverWorkingRef);

            switch (CustID){
                case "":
                    geoFireWorking.removeLocation(userID);
                    geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
                default:
                    geoFire.removeLocation(userID);
                    geoFireWorking.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
            }
        }

    }

    @Override
    protected void onStop() {
        if(!currentLogoutDriverStatus){
            DisconnectTheDriver();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(!currentLogoutDriverStatus){
            DisconnectTheDriver();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if(!currentLogoutDriverStatus){
            DisconnectTheDriver();
        }
            super.onPause();
    }
    private void DisconnectTheDriver() {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available");
        GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
        geoFire.removeLocation(userID);
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

    private void getAssignedCustInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Customers").child(CustID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Name = snapshot.child("Name").getValue().toString();
                    String Phone = snapshot.child("Phone").getValue().toString();
                    String dp = snapshot.child("profileimage").getValue().toString();

                    txtName.setText(Name);
                    txtPhone.setText(Phone);
                    Glide.with(DriverFinalActivity.this).load(dp).into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}