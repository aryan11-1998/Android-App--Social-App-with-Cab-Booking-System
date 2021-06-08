package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText UserName, UserProfName, UserStatus, UserCountry, UserRealtion, UserDOB, UserGender;
    private Button UserAccountSettingsButton;
    private CircleImageView userProfImage;

    private DatabaseReference SettingsUsersRef;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private ProgressDialog loadingBar;
    private StorageReference UserProfileImageRef;

    private String dwnUrl;
    final static int Gallery_Pick =1;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        SettingsUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mToolbar = (Toolbar)findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //video tut 32

        UserName = (EditText) findViewById(R.id.settings_username);
        UserProfName = (EditText) findViewById(R.id.settings_profile_full_name);
        UserStatus = (EditText) findViewById(R.id.settings_status);
        UserCountry = (EditText) findViewById(R.id.settings_country);
        UserGender = (EditText) findViewById(R.id.settings_gender);
        UserRealtion = (EditText) findViewById(R.id.settings_relationship_status);
        UserDOB = (EditText) findViewById(R.id.settings_dob);
        UserAccountSettingsButton = (Button) findViewById(R.id.update_account_settings);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);


        SettingsUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String myProfileImage = snapshot.child("profileimage").getValue().toString();
                    String myUserName = snapshot.child("username").getValue().toString();
                    String myProfileName = snapshot.child("fullname").getValue().toString();
                    String myProfileStatus = snapshot.child("about").getValue().toString();
                    String myDOB = snapshot.child("dob").getValue().toString();
                    String myCountry = snapshot.child("country").getValue().toString();
                    String myGender = snapshot.child("gender").getValue().toString();
                    String myRelationStatus = snapshot.child("relationshipstatus").getValue().toString();

                    Glide.with(SettingsActivity.this).load(myProfileImage).into(userProfImage);
                    UserName.setText(myUserName);
                    UserProfName.setText(myProfileName);
                    UserStatus.setText(myProfileStatus);
                    UserDOB.setText(myDOB);
                    UserCountry.setText(myCountry);
                    UserGender.setText(myGender);
                    UserRealtion.setText(myRelationStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        UserAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
          //      StoringImageToFirebaseStorage();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
    }



    private void OpenGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,Gallery_Pick);
    }

    //Setup activity vala start ...................................................................................................
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== RESULT_OK && data.getData()!=null){
            //resultUri =data.getData();   //selected image is assigned to Variable imageUri
            Uri ImageData = data.getData();
            userProfImage.setImageURI(ImageData); //pta nhi maine likha hai

            //uploadpicture(resultUri);
            //start ........................................................................................................................................................
            loadingBar.setTitle("Uploading...");
            loadingBar.setMessage("Please Wait, while we are uploading your Image. ");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StorageReference Imagename = UserProfileImageRef.child(currentUserId+".jpg");
            Imagename.putFile(ImageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(SetupActivity.this,"Uploaded from function to storage",Toast.LENGTH_SHORT).show();
                    //loadingBar.dismiss();
                    Imagename.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            //Toast.makeText(SetupActivity.this,"t"+uri.toString(),Toast.LENGTH_LONG).show();
                            dwnUrl = uri.toString();
                           // Toast.makeText(SettingsActivity.this,"Image Uploaded.",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            //SendUserToMainActivity();
                            //................................................. PICASSO ...........................................................................................

                            //   Picasso.with(SetupActivity.this).load(dwnUrl).placeholder(R.drawable.profile).into(ProfileImage);
                            //..................................................END ......................................................................................

                            SettingsUsersRef.child("profileimage").setValue(dwnUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent selfIntent = new Intent(SettingsActivity.this,SettingsActivity.class);
                                                startActivity(selfIntent);
                                                Toast.makeText(SettingsActivity.this,"Profile Pic Updated.",Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(SettingsActivity.this,"Error in Updating Profile Pic.",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String message = exception.getMessage();
                            Toast.makeText(SettingsActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();


                        }
                    });
                }
            });

            //end ............................................................................................................................................................
        }
    }
    //Setup activity vala end ...................................................................................................
    private void ValidateAccountInfo() {
        String username = UserName.getText().toString();
        String profileName = UserProfName.getText().toString();
        String status = UserStatus.getText().toString();
        String dob = UserDOB.getText().toString();
        String country = UserCountry.getText().toString();
        String gender = UserGender.getText().toString();
        String Relation = UserRealtion.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Write Your Username.",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(profileName)){
            Toast.makeText(this,"Write Your Full Name.",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(status)){
            Toast.makeText(this,"Write Your Status.",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this,"Write Your Date of Birth.",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Write Your Country.",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this,"Write Your Gender",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(Relation)){
            Toast.makeText(this,"Write Your Relationship Status.",Toast.LENGTH_SHORT).show();

        }else{
            loadingBar.setTitle("Uploading...");
            loadingBar.setMessage("Please Wait, while we are uploading your Image. ");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            UpdateAccountInfo(username,profileName,status,dob,country,gender,Relation);
        }

    }

    private void UpdateAccountInfo(String username, String profileName, String status, String dob, String country, String gender, String relation) {
        HashMap userMap = new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profileName);
        userMap.put("country",country);
        userMap.put("about",status);
        userMap.put("gender",gender);
        userMap.put("dob",dob);
        userMap.put("relationshipstatus",relation);
        //userMap.put("profileimage",dwnUrl);
        SettingsUsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    SendUserToMainActivity();
                    loadingBar.dismiss();
                    Toast.makeText(SettingsActivity.this,"Settings Updated",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(SettingsActivity.this,"Error. ",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }


    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
/*
    private void StoringImageToFirebaseStorage() {
        StorageReference filepath = UserProfileImageRef.child("Profile Image").child(currentUserId+".jpg");
        filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dwnUrl = uri.toString();
                            SettingsUsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        String username = UserName.getText().toString();
                                        String profileName = UserProfName.getText().toString();
                                        String status = UserStatus.getText().toString();
                                        String dob = UserDOB.getText().toString();
                                        String country = UserCountry.getText().toString();
                                        String gender = UserGender.getText().toString();
                                        String Relation = UserRealtion.getText().toString();

                                        if(TextUtils.isEmpty(username)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Username.",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(profileName)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Full Name.",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(status)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Status.",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(dob)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Date of Birth.",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(country)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Country.",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(gender)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Gender",Toast.LENGTH_SHORT).show();

                                        }else if(TextUtils.isEmpty(Relation)){
                                            Toast.makeText(SettingsActivity.this,"Write Your Relationship Status.",Toast.LENGTH_SHORT).show();

                                        }else{
                                            loadingBar.setTitle("Updating...");
                                            loadingBar.setMessage("Please Wait for a moment.");
                                            loadingBar.setCanceledOnTouchOutside(true);
                                            loadingBar.show();

                                            HashMap userMap = new HashMap();
                                            userMap.put("username",username);
                                            userMap.put("fullname",profileName);
                                            userMap.put("country",country);
                                            userMap.put("about",status);
                                            userMap.put("gender",gender);
                                            userMap.put("dob",dob);
                                            userMap.put("relationshipstatus",Relation);
                                            userMap.put("profileimage",dwnUrl);

                                            SettingsUsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if(task.isSuccessful()){
                                                        SendUserToMainActivity();
                                                        loadingBar.dismiss();
                                                        Toast.makeText(SettingsActivity.this,"Settings Updated",Toast.LENGTH_SHORT).show();

                                                    }else{
                                                        Toast.makeText(SettingsActivity.this,"Error. ",Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });

                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }
            }
        });

    }*/
}