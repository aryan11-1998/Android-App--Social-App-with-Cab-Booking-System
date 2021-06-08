package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private RecyclerView CommentsList;
    private DatabaseReference UsersRef, PostsRef;
    private String Post_Key,currentUserID;
    private FirebaseAuth mAuth;
    CommentAdapter adapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("Postkey").toString();

        mToolbar = (Toolbar)findViewById(R.id.comment_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        CommentsList = (RecyclerView)findViewById(R.id.comment_lists);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText)findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_button);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String Fullname = snapshot.child("fullname").getValue().toString();
                            ValidateComment(Fullname);
                            CommentInputText.setText(" ");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                //ValidateComment();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<CommentModel> options =
                new FirebaseRecyclerOptions.Builder<CommentModel>()
                .setQuery(PostsRef,CommentModel.class)
                .build();

        adapter = new CommentAdapter(options);
        CommentsList.setAdapter(adapter);
        adapter.startListening();

    }

    private void ValidateComment(String fullname) {
        String commentText = CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this,"Write a comment.",Toast.LENGTH_SHORT).show();
        }else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate= new SimpleDateFormat("dd-MMMM-YYYY");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String RandomKey = currentUserID+saveCurrentDate+saveCurrentTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",currentUserID);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("fullname",fullname);
            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(CommentsActivity.this,"Comment Posted.",Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(CommentsActivity.this,"Error is posting a comment.",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }
}