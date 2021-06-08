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
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerFinalActivity<Public> extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    FusedLocationProviderClient fusedLocationProviderClient; // pending
    Double CurrentLat, CurrentLong; // pending
    Location lastLocation;
    private Button LogoutCustomerBtn, SettingsCustomerBtn,CallCabCarBtn;
    private String CustID;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference CustomerDatabaseRef, DriverAvailableReff, DriverRef, DriverLocationRef;
    public LatLng CustPickupLocation;
    Double lat,longi; // pending
    private int radius =1;
    private Boolean driverfound = false,request = false;//, requestType = false;
    private String driverfoundID;  // pending
    Marker DriverMarker, PickupMarker; // pending pickup
  //  private ValueEventListener DriverLocationRefListener; // pending
    GeoQuery geoQuery;  // pending

    private TextView txtName,txtPhone,textCarName;
    private CircleImageView profilePic;
    private RelativeLayout relativeLayout;
    private String Number;
    private ImageView callDriver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_final);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        CustID = currentUser.getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customers Request"); //correct
        DriverAvailableReff = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available"); //
        DriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working"); //


        LogoutCustomerBtn =(Button)findViewById(R.id.Cust_final_logout_btn);
        SettingsCustomerBtn =(Button)findViewById(R.id.Cust_final_setting_btn);
        CallCabCarBtn =(Button)findViewById(R.id.call_a_cab_btn_final);

        txtName = findViewById(R.id.cab_name_driver);
        txtPhone = findViewById(R.id.cab_phone_driver);
        textCarName = findViewById(R.id.cab_carname_driver);
        profilePic = (CircleImageView)findViewById(R.id.cab_profile_image_driver);
        relativeLayout = findViewById(R.id.rel1);
        callDriver = findViewById(R.id.call_driver);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LogoutCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logout = new Intent(CustomerFinalActivity.this,WelcomeCabActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(logout);
                finish();
            }
        });

        CallCabCarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    //Toast.makeText(CustomerFinalActivity.this,"Lastloc"+lastLocation,Toast.LENGTH_SHORT).show();
                    DatabaseReference faltuRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available").child(CustID);
                    faltuRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                String lati =  snapshot.child("latitude").getValue().toString();
                                String longii = snapshot.child("longitude").getValue().toString();
                                lat = Double.parseDouble(lati);
                                longi = Double.parseDouble(longii);
                                GeoFire geoFire = new GeoFire(CustomerDatabaseRef); //
                                geoFire.setLocation(CustID,new GeoLocation(lat,longi)); //
                                CustPickupLocation = new LatLng(lat,longi); //
                                mMap.addMarker(new MarkerOptions().position(CustPickupLocation).title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.user))); //
                                // upladed till now
                                //now to find driver.
                                CallCabCarBtn.setText("Searching..."); //
                                GetClosestDriver(); //


                            }else {
                                Toast.makeText(CustomerFinalActivity.this,"Customer Unavailable.",Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




                            }
        });

        SettingsCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent(CustomerFinalActivity.this,CabSettingsActivity.class);
                settings.putExtra("type","Customers");
                startActivity(settings);
            }
        });



    /*    callDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Number.isEmpty()){
                    Toast.makeText(CustomerFinalActivity.this,"Invalid Number",Toast.LENGTH_SHORT).show();
                }else{
                    String s = "tel:" + Number;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse(s));
                    startActivity(intent);
                }
            }
        }); */
    }//on create



    private void GetClosestDriver() {
        GeoFire geoFire = new GeoFire(DriverAvailableReff); //
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(CustPickupLocation.latitude,CustPickupLocation.longitude),radius); //
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {  //
            @Override
            public void onKeyEntered(String key, GeoLocation location) {  //
              //  if(!driverfound && requestType){  //
                    if(!driverfound ){
                    driverfound = true;//
                    driverfoundID = key; //
                    DriverRef = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Drivers").child(driverfoundID); //
                    HashMap driverMap = new HashMap();//
                    driverMap.put("CustomerRideID",CustID);//
                    DriverRef.updateChildren(driverMap); //

                    //-----------------------------------------------------------------------------------------------
                    //RemoveDriverAvailibility();

                    //DatabaseReference DriverAvailabilityRefDrive = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available"); //
                    //GeoFire geoFireAvailbilityDrive = new GeoFire(DriverAvailabilityRefDrive);   //

                    //DatabaseReference DriverWorkingRefDrive = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Working");
                    //GeoFire geoFireWorkingDrive = new GeoFire(DriverWorkingRefDrive);

                    //geoFireAvailbilityDrive.removeLocation(driverfoundID);

                    //-------------------------------------------------------------------------------------------------

                    GettingDriverLocation(); //
                    CallCabCarBtn.setText("Getting Driver Location..."); //
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {  //full
                if(!driverfound){
                    radius++;
                    GetClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void GettingDriverLocation() {

        //DriverLocationRefListener  = DriverLocationRef.child(driverfoundID).child("l") //
        DriverLocationRef.child(driverfoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //if(snapshot.exists() && requestType)
                        if(snapshot.exists() ){  //
                            List<Object> driverLocationMap = (List<Object>) snapshot.getValue();//
                            double LocationLat = 0; //
                            double LocationLong = 0;  //
                            CallCabCarBtn.setText("Driver Found."); //

                            relativeLayout.setVisibility(View.VISIBLE);
                            getAssignedDriverInfo();
                            Toast.makeText(CustomerFinalActivity.this,"Num: "+Number,Toast.LENGTH_SHORT).show();

                            if(driverLocationMap.get(0)!=null){  //
                                LocationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }
                            if(driverLocationMap.get(1)!=null){ //
                                LocationLong = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            LatLng DriverLatLng = new LatLng(LocationLat,LocationLong); //
                            if(DriverMarker!=null){  //
                                DriverMarker.remove(); //
                            }
                            Location location1 = new Location("");     //
                            location1.setLatitude(CustPickupLocation.latitude);//
                            location1.setLongitude(CustPickupLocation.longitude);//

                            Location location2 = new Location("");   //
                            location2.setLatitude(DriverLatLng.latitude);  //
                            location2.setLongitude(DriverLatLng.longitude);//

                            float distance = location1.distanceTo(location2); //



                            if(distance < 90){
                                CallCabCarBtn.setText("Driver is at your Location.");
                            }else{
                                CallCabCarBtn.setText("Your Driver is "+String.valueOf(distance)+"m away.");
                            }

                            DriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLatLng).title("Driver's Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.car))); //

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
        enableMyLocation();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
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

                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference CustomerLogin = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available");
                        CustomerLogin.child(userID).child("latitude").setValue(location.getLatitude());
                        CustomerLogin.child(userID).child("longitude").setValue(location.getLongitude());
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
                                DatabaseReference CustomerLogin = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available");
                                CustomerLogin.child(userID).child("latitude").setValue(location.getLatitude());
                                CustomerLogin.child(userID).child("longitude").setValue(location.getLongitude());
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
        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference CustomerLogin = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available");
        CustomerLogin.child(userID).child("latitude").setValue(location.getLatitude());
        CustomerLogin.child(userID).child("longitude").setValue(location.getLongitude());
    }



    @Override
    protected void onStop() {
        DestroyCustomerAvailable();
         super.onStop();
    }

    @Override
    protected void onDestroy() {DestroyCustomerAvailable();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        DestroyCustomerAvailable();
        super.onPause();
    }

    public void DestroyCustomerAvailable(){
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference CustomerLogin = FirebaseDatabase.getInstance().getReference().child("Cab").child("Customer Available");
        GeoFire geoFire = new GeoFire(CustomerLogin);
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


    private void CancelRequest() {


        //String userID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //
        DriverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String gValue,lValue,DriverId;
                    Double Lat,lng;
                    DriverId = snapshot.getValue().toString();
                    DatabaseReference DriverAvailabilityRef = FirebaseDatabase.getInstance().getReference().child("Cab").child("Drivers Available");
                    GeoFire geoFire = new GeoFire(DriverAvailabilityRef);
                     Lat = Double.parseDouble(snapshot.child("l").child("0").getValue().toString());
                    lng = Double.parseDouble(snapshot.child("l").child("1").getValue().toString());
                    geoFire.setLocation(DriverId,new GeoLocation(Lat,lng));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       // geoFire.setLocation(userID,new GeoLocation(location.getLatitude(),location.getLongitude()));





        CustomerDatabaseRef.child(CustID).removeValue(); //cust req
        DatabaseReference CabUserDriver = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Drivers").child(CustID);  //user ki id
        CabUserDriver.removeValue();
        CabUserDriver.setValue("true");
        DriverLocationRef.removeValue(); //driver working

        CallCabCarBtn.setText("Call a Cab");
    }

    private void getAssignedDriverInfo(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Drivers").child(driverfoundID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String Name = snapshot.child("Name").getValue().toString();
                    String Phone = snapshot.child("Phone").getValue().toString();
                    String CarName =  snapshot.child("Car").getValue().toString();
                    String dp = snapshot.child("profileimage").getValue().toString();

                    txtName.setText(Name);
                    txtPhone.setText(Phone);
                    textCarName.setText(CarName);
                    Glide.with(CustomerFinalActivity.this).load(dp).into(profilePic);

                    Number = Phone;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
