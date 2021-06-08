package com.example.project;

import android.graphics.Color;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<ChatModel> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    public ChatAdapter(List<ChatModel> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView SenderMessageText, ReceiverMessageText;
        public CircleImageView ReceiverProfileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            ReceiverMessageText = (TextView)itemView.findViewById(R.id.receiver_message_text);
            ReceiverProfileImage = (CircleImageView)itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View V = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout_of_user,parent,false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        //Message message = userMessagesList.get(position);
        ChatModel messages = userMessagesList.get(position);
        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String imaage = snapshot.child("profileimage").getValue().toString();
                    Glide.with(holder.ReceiverProfileImage.getContext()).load(imaage).into(holder.ReceiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (fromMessageType.equals("text")){
            holder.ReceiverMessageText.setVisibility(View.INVISIBLE);
            holder.ReceiverProfileImage.setVisibility(View.INVISIBLE);

            if(fromUserId.equals(messageSenderID)){
                holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                holder.SenderMessageText.setTextColor(Color.BLACK);
                holder.SenderMessageText.setGravity(Gravity.LEFT);
                holder.SenderMessageText.setText(messages.getMessage());
            }else{
                holder.SenderMessageText.setVisibility(View.INVISIBLE);

                holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                holder.ReceiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                holder.ReceiverMessageText.setTextColor(Color.BLACK);
                holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                holder.ReceiverMessageText.setText(messages.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
