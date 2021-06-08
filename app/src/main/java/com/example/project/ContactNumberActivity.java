package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactNumberActivity extends AppCompatActivity {

    private EditText name,phone;
    private Button driver,cust;
    FirebaseAuth mAuth;
    private DatabaseReference driverRef,custRef,userRef , CustomerDatabaseRef, DriverDatabaseRef;
    private String CurrentID,profileimage;
    private ProgressDialog loadingBar;
  //  TextView headerName;
    //CircleImageView headerPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_number);

        mAuth = FirebaseAuth.getInstance();
        CurrentID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentID);
        custRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Customers");
        driverRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Drivers");

        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Customers").child(CurrentID);
        DriverDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Drivers").child(CurrentID);

        phone = (EditText)findViewById(R.id.Contact_Phone);
        name = (EditText)findViewById(R.id.Contact_name);
        driver = (Button) findViewById(R.id.contactDriverBtn);
        cust = (Button) findViewById(R.id.contactCustomerBtn);
     //   headerName = (TextView)findViewById(R.id.nav_user_full_name);
       // headerPic = (CircleImageView)findViewById(R.id.nav_profile_image);






        cust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = name.getText().toString();
                String Phone = phone.getText().toString();
                //String image = profileimage;
                if (TextUtils.isEmpty(Name)){
                    Toast.makeText(ContactNumberActivity.this,"Enter Your Name",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Phone)){
                    Toast.makeText(ContactNumberActivity.this,"Enter Your Mobile No.",Toast.LENGTH_SHORT).show();
                }else {
                    custRef.child(CurrentID).child("Name").setValue(Name);
                    custRef.child(CurrentID).child("Phone").setValue(Phone);
                    Toast.makeText(ContactNumberActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();

                    CustomerDatabaseRef.setValue("true");
                    Intent CustIntent = new Intent(ContactNumberActivity.this,CustomerFinalActivity.class);
                    startActivity(CustIntent);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String image = snapshot.child("profileimage").getValue(String.class);
                            custRef.child(CurrentID).child("profileimage").setValue(image);
                            // Glide.with(ContactNumberActivity.this).load(image).into(headerPic);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }
        });


        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name = name.getText().toString();
                String Phone = phone.getText().toString();
                //String image = profileimage;
                if (TextUtils.isEmpty(Name)){
                    Toast.makeText(ContactNumberActivity.this,"Enter Your Name",Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Phone)){
                    Toast.makeText(ContactNumberActivity.this,"Enter Your Mobile No.",Toast.LENGTH_SHORT).show();
                }else {
                    driverRef.child(CurrentID).child("Name").setValue(Name);
                    driverRef.child(CurrentID).child("Phone").setValue(Phone);
                   // headerName.setText(Name);

                    DriverDatabaseRef.setValue(true);
                    Toast.makeText(ContactNumberActivity.this,"Uploaded.",Toast.LENGTH_SHORT).show();
                    Intent DriverIntent = new Intent(ContactNumberActivity.this,DriverFinalActivity.class);
                    startActivity(DriverIntent);

                    userRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String image = snapshot.child("profileimage").getValue(String.class);
                            driverRef.child(CurrentID).child("profileimage").setValue(image);
                           // Glide.with(ContactNumberActivity.this).load(image).into(headerPic);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

                }
        });

    }
}