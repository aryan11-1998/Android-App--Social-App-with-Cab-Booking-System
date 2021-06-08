package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView UserName, UserProfName, UserStatus, UserCountry, UserRealtion, UserDOB, UserGender;
    private CircleImageView userProfileImage;
    private Button MyPosts, MyFriends;
    private DatabaseReference profileUserRef, PostRef;
    private FirebaseAuth mAuth;
    private String currentUserID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UserName = (TextView) findViewById(R.id.my_username);
        UserProfName = (TextView) findViewById(R.id.my_profile_full_name);
        UserStatus = (TextView) findViewById(R.id.my_profile_status);
        UserCountry = (TextView) findViewById(R.id.my_country);
        UserGender = (TextView) findViewById(R.id.my_gender);
        UserRealtion = (TextView) findViewById(R.id.my_relationship_status);
        UserDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyFriends = (Button) findViewById(R.id.my_friends_button);
        MyPosts = (Button) findViewById(R.id.my_post_button);


        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent friendsIntent = new Intent(ProfileActivity.this,FriendsActivity.class);
                startActivity(friendsIntent);
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent postIntent = new Intent(ProfileActivity.this,MyPostActivity.class);
                startActivity(postIntent);
            }
        });



        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                    Glide.with(ProfileActivity.this).load(myProfileImage).into(userProfileImage);
                    UserName.setText("@"+myUserName);
                    UserProfName.setText(myProfileName);
                    UserStatus.setText(myProfileStatus);
                    UserDOB.setText("D.O.B : "+myDOB);
                    UserCountry.setText("Country : "+myCountry);
                    UserGender.setText("Gender : "+myGender);
                    UserRealtion.setText("Relationship Status : "+myRelationStatus);
                }else{
                    SendUserToMainActivity();
                    Toast.makeText(ProfileActivity.this,"pta nhi kiya hua.",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}