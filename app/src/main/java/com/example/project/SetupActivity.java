package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    String dwnUrl;
    String currentUserID;
    private ProgressDialog loadingBar;
    private Uri resultUri;
    private Uri ImageData;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        //UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile images");  //  ye thik haai? karo try we are using storageRef for this
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button) findViewById(R.id.setup_informaion_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);
        storage = FirebaseStorage.getInstance();
        storageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");


        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });

     UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){// if it has user
                    if (snapshot.hasChild("profileimage")){
                        String image = snapshot.child("profileimage").getValue().toString();
                    }
                    else{
                        Toast.makeText(SetupActivity.this,"Please Select Image first",Toast.LENGTH_SHORT);
                    }

                   // Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(ProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== RESULT_OK && data.getData()!=null){
            //resultUri =data.getData();   //selected image is assigned to Variable imageUri
            ImageData = data.getData();
            SendUserToCropActivity();
             //pta nhi maine likha hai

             //uploadpicture(resultUri);
            //start ........................................................................................................................................................


            //end ............................................................................................................................................................
          }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                Uri resultUriCropped = result.getUri();
                ProfileImage.setImageURI(resultUriCropped);

                loadingBar.setTitle("Uploading...");
                loadingBar.setMessage("Please Wait, while we are uploading your Image. ");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                StorageReference Imagename = storageRef.child(currentUserID+".jpg");
                Imagename.putFile(resultUriCropped).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                Toast.makeText(SetupActivity.this,"Image Uploaded.",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                //SendUserToMainActivity();
                                //................................................. PICASSO ...........................................................................................

                                //   Picasso.with(SetupActivity.this).load(dwnUrl).placeholder(R.drawable.profile).into(ProfileImage);
                                //..................................................END ......................................................................................

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                String message = exception.getMessage();
                                Toast.makeText(SetupActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                        });
                    }
                });
            }else{
                Toast.makeText(SetupActivity.this,"Error, Try Again.",Toast.LENGTH_SHORT).show();

            }
        }
  }

    private void SendUserToCropActivity() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);
    }

    private void SaveAccountSetupInformation() {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        //profilepic = imageUri;
        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"Enter Your Username",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this,"Enter Your Fullname",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(country)){
            Toast.makeText(this,"Enter Your Country Name",Toast.LENGTH_SHORT).show();
        }else{

            loadingBar.setTitle("Saving");
            loadingBar.setMessage("Please Wait, while we are saving your Information. ");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("about","default for time being");
            userMap.put("gender","None");
            userMap.put("dob","");
            userMap.put("relationshipstatus","");
            userMap.put("profileimage",dwnUrl);


            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();

                        Toast.makeText(SetupActivity.this,"Account Updated",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

  /*  private void uploadpicture(Uri ImageData) {
        //private StorageReference storageRef;
        //storageRef= FirebaseStorage.getInstance().getReference().child("imagefolder");
        loadingBar.setTitle("Uploading...");
        loadingBar.setMessage("Please Wait, while we are uploading your Image. ");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);



        StorageReference Imagename = storageRef.child(currentUserID+".jpg");
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
                        Toast.makeText(SetupActivity.this,"Uploaded from function to storage",Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        //SendUserToMainActivity();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        String message = exception.getMessage();
                        Toast.makeText(SetupActivity.this,"Error : "+message,Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();


                    }
                });
            }
        });


    }*/
}