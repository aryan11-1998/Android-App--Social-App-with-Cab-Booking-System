package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CabSettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText nameEditText,phoneEditText,driverCarNameEditText;
    private ImageView closeButton,saveButton;
    private TextView profileChangeBtn;
    private String getType;
    private String checker ="", myUrl,CurrentID;
    private StorageTask uploadTask;
   // private StorageReference storageProfilePicsRef;
   // Uri imageUri;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    DatabaseReference custRef,driverRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cab_settings);

        getType = getIntent().getStringExtra("type");
        mAuth =FirebaseAuth.getInstance();
     //   databaseReference = FirebaseDatabase.getInstance().getReference().child("Cab Info").child(getType);
     //   storageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Cab ProfilePics");
        CurrentID = mAuth.getCurrentUser().getUid();

        driverRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Drivers");
        custRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Customers");



        profileImageView = (CircleImageView)findViewById(R.id.cab_profile_image);
        nameEditText  = (EditText) findViewById(R.id.cab_name);
        phoneEditText = (EditText) findViewById(R.id.cab_phone_number);
        driverCarNameEditText= (EditText) findViewById(R.id.cab_driver_car_name);
        if(getType.equals("Drivers")){
            driverCarNameEditText.setVisibility(View.VISIBLE);
        }

        closeButton= (ImageView) findViewById(R.id.cab_close_button);
        saveButton= (ImageView) findViewById(R.id.cab_save_button);
       // profileChangeBtn= (TextView) findViewById(R.id.cab_change_picture_btn);


        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getType.equals("Drivers")){
                    startActivity(new Intent(CabSettingsActivity.this,DriverFinalActivity.class));
                }else{
                    startActivity(new Intent(CabSettingsActivity.this,CustomerFinalActivity.class));
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               validateControllers();
               SaveInformation();
            }
        });

      /*  profileChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1,1)
                        .start(CabSettingsActivity.this);
            }
        });*/

        getUserInfo();
    }

    private void SaveInformation() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Account Info");
        progressDialog.setMessage("Please wait for a moment.");
        progressDialog.show();
        String Car;
        String name, phone;
        name = nameEditText.getText().toString();
        phone = phoneEditText.getText().toString();
        if(getType.equals("Drivers")){
          //  DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Drivers");
            Car = driverCarNameEditText.getText().toString();

            driverRef.child(CurrentID).child("Name").setValue(name);
            driverRef.child(CurrentID).child("Phone").setValue(phone);
            driverRef.child(CurrentID).child("Car").setValue(Car);
            driverRef.child(CurrentID).child("Uid").setValue(CurrentID);

            progressDialog.dismiss();

            startActivity(new Intent(CabSettingsActivity.this,DriverFinalActivity.class));
            Toast.makeText(this,"Information Saved.",Toast.LENGTH_SHORT);

        }else{
           // DatabaseReference custRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Customers");

            custRef.child(CurrentID).child("Name").setValue(name);
            custRef.child(CurrentID).child("Phone").setValue(phone);
            custRef.child(CurrentID).child("Uid").setValue(CurrentID);
            progressDialog.dismiss();

            startActivity(new Intent(CabSettingsActivity.this,CustomerFinalActivity.class));
            Toast.makeText(this,"Information Saved.",Toast.LENGTH_SHORT);
        }
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data!=null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImageView.setImageURI(imageUri);
            uploadProfilePicture();
        }else {
            if(getType.equals("Drivers")){
                startActivity(new Intent(CabSettingsActivity.this,DriverFinalActivity.class));
            }else{
                startActivity(new Intent(CabSettingsActivity.this,CustomerFinalActivity.class));
            }

            Toast.makeText(this,"Error! try Again,",Toast.LENGTH_SHORT).show();
        }
    }*/

  /*  private void uploadProfilePicture() {
        if(imageUri!=null){
            final StorageReference fileRef = storageProfilePicsRef.child(mAuth.getCurrentUser().getUid()+".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                       if(getType.equals("Drivers")){
                        //
                           //  DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Cab Info").child("Drivers");
                           driverRef.child(CurrentID).child("profileimage").setValue(myUrl);
                       }else{

                           custRef.child(CurrentID).child("profileimage").setValue(myUrl);
                       }
                    }
                }
            });


        }else{
            Toast.makeText(this,"Image Not Selected.",Toast.LENGTH_SHORT).show();
        }
    }*/

    private void validateControllers(){
        if(TextUtils.isEmpty(nameEditText.getText().toString())){
            Toast.makeText(this,"Enter Your Name",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this,"Enter Your Contact Number,",Toast.LENGTH_SHORT).show();
        }else if(getType.equals("Drivers") && TextUtils.isEmpty(driverCarNameEditText.getText().toString())){
            Toast.makeText(this,"Enter Your Car Name",Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInfo(){

        if(getType.equals("Drivers")){
           // String dp;
            driverRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        nameEditText.setText(snapshot.child(CurrentID).child("Name").getValue().toString());
                        phoneEditText.setText(snapshot.child(CurrentID).child("Phone").getValue().toString());
                        driverCarNameEditText.setText(snapshot.child(CurrentID).child("Car").getValue().toString());
                        String dp = snapshot.child(CurrentID).child("profileimage").getValue().toString();
                        Glide.with(CabSettingsActivity.this).load(dp).into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{

            custRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        nameEditText.setText(snapshot.child(CurrentID).child("Name").getValue().toString());
                        phoneEditText.setText(snapshot.child(CurrentID).child("Phone").getValue().toString());
                        String dp = snapshot.child(CurrentID).child("profileimage").getValue().toString();
                        Glide.with(CabSettingsActivity.this).load(dp).into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

}