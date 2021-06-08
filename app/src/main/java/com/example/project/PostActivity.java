package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
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




public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private static final int Gallery_Pick =1;
    private Uri ImageUri;
    private String Description;

    private StorageReference PostImagesRefrence;
    private DatabaseReference usersRef, postRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String downloadUrl;
    private String saveCurrentDate, saveCurrentTime, postRandomName;
    private ProgressDialog loadingBar;

    private long countPosts = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostImagesRefrence = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

       //for app bar(part1 of 2) .....................................................................................................................
        mToolbar = (Toolbar) findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");
        //part1 ends ............................................................................................................................

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button)findViewById(R.id.update_post_button);
        PostDescription = (EditText)findViewById(R.id.post_description);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        Description = PostDescription.getText().toString();
        if(ImageUri == null){
            Toast.makeText(PostActivity.this,"Please Select Image.",Toast.LENGTH_SHORT);
        }else if(TextUtils.isEmpty(Description)){
            Toast.makeText(PostActivity.this,"Please Say Something about your Image.",Toast.LENGTH_SHORT);
        }else{
            StoringImageToFirebaseStorage();
        }
    }
    private void StoringImageToFirebaseStorage() {

        loadingBar.setTitle("Uploading...");
        loadingBar.setMessage("Please Wait, while we are uploading your Post. ");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate= new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        StorageReference filepath = PostImagesRefrence.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName +".jpg"); //first child is creating a folder and second is assigning a name(random)
        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    // code for getting url ............................................................................................

                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            downloadUrl = uri.toString();
                            //loadingBar.dismiss();
                            //Toast.makeText(PostActivity.this,"Uploaded.url:"+downloadUrl,Toast.LENGTH_SHORT).show();
                            // -----------------------------SAVING POST INFORMATION TO DATABASE -----------------------------------------------------------------------------------
                            postRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        countPosts = snapshot.getChildrenCount();
                                    }else{
                                        countPosts = 0;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        String userfullName = snapshot.child("fullname").getValue().toString();
                                        String userProfileImage =snapshot.child("profileimage").getValue().toString();

                                        HashMap postsMap = new HashMap();
                                        postsMap.put("uid",currentUserId);
                                        postsMap.put("date",saveCurrentDate);
                                        postsMap.put("time",saveCurrentTime);
                                        postsMap.put("description",Description);
                                        postsMap.put("postimage",downloadUrl); //downloadUrl se change kar dena please
                                        postsMap.put("profileimage",userProfileImage); // userProfileImage
                                        postsMap.put("fullname",userfullName);
                                        postsMap.put("counter",countPosts);

                                        postRef.child(currentUserId + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if(task.isSuccessful()){
                                                    sendUserToMainActivity();
                                                    //   Toast.makeText(PostActivity.this,"Post Updated."+downloadUrl,Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(PostActivity.this,"Error while updating post in database.",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        // ----------------------------------- END -----------------------------------------------------------------------------------
                        }
                    });// */
                    //end ..........................................................................................................

                //    SavingPostInformationToDatabase();

                    Toast.makeText(PostActivity.this,"Uploaded.url:"+downloadUrl,Toast.LENGTH_SHORT).show();

                }else{
                    loadingBar.dismiss();
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this,"Error: "+ message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }




    private void OpenGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode == RESULT_OK && data!= null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    // for app bar(part 2 of 2)...................................................................................................................
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }
    // part 2 ends .............................................................................................................................

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}