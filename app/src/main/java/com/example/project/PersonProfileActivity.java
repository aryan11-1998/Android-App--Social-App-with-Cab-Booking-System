package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView UserName, UserProfName, UserStatus, UserCountry, UserRealtion, UserDOB, UserGender;
    private CircleImageView userProfileImage;
    private Toolbar mToolbar;
    private Button SendFriendReqButton, DeclineFriendRequestButton;

    private DatabaseReference FriendReqRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        senderUserId = mAuth.getCurrentUser().getUid();
        FriendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        mToolbar = (Toolbar)findViewById(R.id.person_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        UserName = (TextView) findViewById(R.id.person_username);
        UserProfName = (TextView) findViewById(R.id.person_full_name);
        UserStatus = (TextView) findViewById(R.id.person_profile_status);
        UserCountry = (TextView) findViewById(R.id.person_country);
        UserGender = (TextView) findViewById(R.id.person_gender);
        UserRealtion = (TextView) findViewById(R.id.person_relationship_status);
        UserDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        SendFriendReqButton = (Button) findViewById(R.id.person_send_friend_request_button);
        DeclineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request_button);
        CURRENT_STATE = "not_friends";

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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

                    Glide.with(PersonProfileActivity.this).load(myProfileImage).into(userProfileImage);
                    UserName.setText("@"+myUserName);
                    UserProfName.setText(myProfileName);
                    UserStatus.setText(myProfileStatus);
                    UserDOB.setText("D.O.B : "+myDOB);
                    UserCountry.setText("Country : "+myCountry);
                    UserGender.setText("Gender : "+myGender);
                    UserRealtion.setText("Relationship Status : "+myRelationStatus);

                    MaintainanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){

            SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendReqButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnFriendAnExistingFriend();
                    }
                }
            });

        }else{
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendReqButton.setVisibility(View.INVISIBLE);
        }
    }

    private void UnFriendAnExistingFriend() {
        FriendsRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendsRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends" ;
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate= new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FriendReqRef.child(senderUserId).child(receiverUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    FriendReqRef.child(receiverUserId).child(senderUserId).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        SendFriendReqButton.setEnabled(true);
                                                                        CURRENT_STATE = "friends" ;
                                                                        SendFriendReqButton.setText("Unfriend");

                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }); //FriendReqRef

                            }//if andar vala
                        }// on complete
                    }); // FriendsREf.child addOnComplete
                }//if bahar vala
            }
        });
    }

    private void CancelFriendRequest() {
        FriendReqRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendReqRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends" ;
                                                SendFriendReqButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintainanceOfButtons() {
        FriendReqRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(receiverUserId)){
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                SendFriendReqButton.setText("Cancel Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }else if(request_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                SendFriendReqButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendRequest();
                                    }
                                });

                            }
                        }else{
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.hasChild(receiverUserId)){
                                                CURRENT_STATE ="friends";
                                                SendFriendReqButton.setText("Unfriend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendFriendRequestToAPerson() {
        FriendReqRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendReqRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                SendFriendReqButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent" ;
                                                SendFriendReqButton.setText("Cancel Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}