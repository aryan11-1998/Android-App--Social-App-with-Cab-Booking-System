package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
//import android.support.v4.app.*;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private CircleImageView NavProfileImage;
    TextView NavProfileUsername;
    String fullname;
    //TextView faltu;
    String image;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, PostsRef;
    String currentUserId;
    private ImageButton AddNewPostButton;
    MyAdapter adapter;

    private DatabaseReference LikesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
       // currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.naigation_view);


        postList = (RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setLayoutManager(new LinearLayoutManager(this)); //recyler vala program
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header); // navigation header vaps laane k liye
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);
        NavProfileUsername = (TextView) navView.findViewById(R.id.nav_user_full_name);

       UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    if(snapshot.hasChild("fullname")){
                        fullname = snapshot.child("fullname").getValue().toString();
                       // getSupportActionBar().setTitle("Home (Welcome:"+fullname+")");
                        NavProfileUsername.setText(fullname);
                    }
                    if(snapshot.hasChild("profileimage")){
                        image = snapshot.child("profileimage").getValue().toString();
                        Glide.with(MainActivity.this).load(image).into(NavProfileImage);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile Name do not exist.", Toast.LENGTH_LONG).show();
                    }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;

            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

       // DisplayAllUsersPost();
        Query SortPostInDescendingOrder = PostsRef.orderByChild("counter");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(SortPostInDescendingOrder, Posts.class)
                        .build();

        adapter = new MyAdapter(options);
        postList.setAdapter(adapter);
    }

    // ON CLICK LISTENER ENDS HERE ..................................................................................................
    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }


   /* public void UpdateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calforDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calforDate.getTime());
        Calendar calforTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm aa");
        saveCurrentTime = currentTime.format(calforTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);
        UserRef.child(currentUserId).child("userState")
                .updateChildren(currentStateMap);
    }*/

    protected void onStart(){
        super.onStart();
       // SendUserToLoginActivity();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser ==null){
            SendUserToLoginActivity();
        }//else{
           // CheckUserExistence();
           //SendUserToMainActivity();
        //}

        adapter.startListening();
    //    UpdateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
     //   UpdateUserStatus("offline");
    }

    private void CheckUserExistence() {
        final String current_user_Id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_Id)){
                   SendUserToSetupActivity();

                }else{
                    SendUserToMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent SetupIntent = new Intent(MainActivity.this, SetupActivity.class);
        SetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(SetupIntent);
        finish();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                Intent intentprofile = new Intent(MainActivity.this,ProfileActivity.class);
                startActivity(intentprofile);
               // Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Intent friendsIntent = new Intent(MainActivity.this,FriendsActivity.class);
                startActivity(friendsIntent);
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Intent findIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
                startActivity(findIntent);
               // Toast.makeText(this, "Find f", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_cab:
                Intent messageIntent = new Intent(MainActivity.this,SplashActivityCab.class);
                startActivity(messageIntent);
               // Toast.makeText(this, "messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Intent intentSetting = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intentSetting);
               // Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
               // UpdateUserStatus("offline");
                Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }
}