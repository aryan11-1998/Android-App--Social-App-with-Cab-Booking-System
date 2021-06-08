package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeCabActivity extends AppCompatActivity {

    Button DriverBtn,CustBtn;
    DatabaseReference DriverRef,CustRef;
    String CurrentDriverID,CurrentCustID;
    FirebaseFirestore firebaseFirestore;
    DatabaseReference custref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_cab);

        firebaseFirestore = FirebaseFirestore.getInstance();
        custref = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Customers");
        CurrentDriverID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DriverRef = FirebaseDatabase.getInstance().getReference().child("Cab Users").child("Drivers");
       // CurrentCustID = FirebaseAuth.getInstance().getCurrentUser().getUid();
       // reference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").child(currentUserId);

        DriverBtn =(Button) findViewById(R.id.welcome_driver_btn);
        CustBtn =(Button) findViewById(R.id.welcome_customer_btn);

        DriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  CreateDriverField();
                checkDriverDetails();

            }
        });

        CustBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCustomerDetials();
            }
        });

    }

    private void checkDriverDetails() {

        DriverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(CurrentDriverID)){
                    Intent intent = new Intent(WelcomeCabActivity.this,ContactNumberActivity.class);
                    startActivity(intent);
                    Toast.makeText(WelcomeCabActivity.this,"Deatils are not present",Toast.LENGTH_SHORT).show();
                }else{
                    Intent DriverIntent = new Intent(WelcomeCabActivity.this,DriverFinalActivity.class);
                    startActivity(DriverIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkCustomerDetials() {
        final String current_cust_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        custref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild(current_cust_id)){
                    Intent intent = new Intent(WelcomeCabActivity.this,ContactNumberActivity.class);
                    startActivity(intent);
                    Toast.makeText(WelcomeCabActivity.this,"Deatils are not present",Toast.LENGTH_SHORT).show();
                }else{
                    Intent CustIntent = new Intent(WelcomeCabActivity.this,CustomerFinalActivity.class);
                    startActivity(CustIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    @Override
    protected void onStart() {
        DatabaseReference reference,referencecab;
        String cUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        referencecab = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Drivers Available").child(cUser);
        referencecab.removeValue();
        reference = FirebaseDatabase.getInstance().getReference().child("Cabs").child("Customer").child(cUser);
        reference.removeValue();


        super.onStart();
    }
}