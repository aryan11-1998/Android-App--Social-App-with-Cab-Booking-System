package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView myFriendsList;
    private DatabaseReference FriendsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar)findViewById(R.id.friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myFriendsList = (RecyclerView)findViewById(R.id.friends_list);
        myFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendsList.setLayoutManager(linearLayoutManager);

        FirebaseRecyclerOptions<FriendsModel> options =
                new FirebaseRecyclerOptions.Builder<FriendsModel>()
                        .setQuery(FriendsRef,FriendsModel.class)
                        .build();
        adapter = new FriendsAdapter(options);
        myFriendsList.setAdapter(adapter);
    }

  /*  public void UpdateUserStatus(String state){
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
        UsersRef.child(currentUserId).child("userState")
                .updateChildren(currentStateMap);
    }
*/
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
       // UpdateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
     //   UpdateUserStatus("offline");
    }

}