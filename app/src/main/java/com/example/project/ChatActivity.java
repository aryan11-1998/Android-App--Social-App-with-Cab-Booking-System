package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.SQLTransactionRollbackException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton, SendImageFileButton;
    private EditText userMessageInput;
    private RecyclerView userMessageList;
    private String messageReceiverId, messageReceiverName, messageSenderId, saveCurrentDate,saveCurrentTime;
    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private final List<ChatModel> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private ChatAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        ChatToolBar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);
        getSupportActionBar().setTitle("");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        SendImageFileButton = (ImageButton)findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.inout_message);
        receiverName = (TextView) findViewById(R.id.custom_profile_name);
       // userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        messageAdapter = new ChatAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

        DisplayReceiverInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        FetchMessages();

    }//on create

    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.exists()){
                            ChatModel messages = snapshot.getValue(ChatModel.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendMessage() {
        String messageText = userMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this,"Type a message first",Toast.LENGTH_SHORT).show();
        }else {
            String message_sender_ref = "Messages/" + messageSenderId + "/"+messageReceiverId;
            String message_receiver_ref = "Messages/" + messageReceiverId + "/"+messageSenderId;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            String message_push_id = user_message_key.getKey();
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate= new SimpleDateFormat("dd-MMMM-YYYY");
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calForDate.getTime());

            Map messageTextBody = new HashMap();
                messageTextBody.put("message",messageText);
                messageTextBody.put("time",saveCurrentTime);
                messageTextBody.put("date",saveCurrentDate);
                messageTextBody.put("type","text");
                messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref+ "/"+message_push_id , messageTextBody);
                messageBodyDetails.put(message_receiver_ref+ "/"+message_push_id , messageTextBody);

             RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                 @Override
                 public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ChatActivity.this,"Message Sent",Toast.LENGTH_SHORT).show();
                            userMessageInput.setText("");
                        }else{
                            String msg = task.getException().getMessage();
                            userMessageInput.setText("");
                            Toast.makeText(ChatActivity.this,"Error: "+msg,Toast.LENGTH_SHORT).show();
                        }

                 }
             });
        }
    }

    private void DisplayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        RootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    final String profileImage = snapshot.child("profileimage").getValue().toString();
                    
                    Glide.with(ChatActivity.this).load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

  /*  public void UpdateUserStatus(String state){
        DatabaseReference lastSeenRef = FirebaseDatabase.getInstance().getReference().child("Users");
        String onlineUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        lastSeenRef.child(onlineUserID).child("userState")
                .updateChildren(currentStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("offline");
    }*/
}