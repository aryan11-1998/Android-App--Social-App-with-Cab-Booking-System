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
import com.google.firebase.database.Query;

public class MyPostActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView myPostList;
    MyAdapter adapter;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar)findViewById(R.id.all_my_posts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myPostList =(RecyclerView)findViewById(R.id.my_all_post_list);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts() {

        Query myPostQuery = PostsRef.orderByChild("uid").startAt(currentUserId).endAt(currentUserId+"\uf8ff");
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostQuery, Posts.class)
                        .build();

        adapter = new MyAdapter(options);
        myPostList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}