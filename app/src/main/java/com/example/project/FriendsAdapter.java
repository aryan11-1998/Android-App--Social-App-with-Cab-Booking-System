package com.example.project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends FirebaseRecyclerAdapter<FriendsModel,FriendsAdapter.myViewholder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FriendsAdapter(@NonNull FirebaseRecyclerOptions<FriendsModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewholder holder, int position, @NonNull FriendsModel model) {
        final String usersIDs = getRef(position).getKey();
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    final String name = snapshot.child("fullname").getValue().toString();
                    final String dp = snapshot.child("profileimage").getValue().toString();
                    holder.date.setText("Friends since: "+model.getDate());
                    holder.fullname.setText(name);
                    Glide.with(holder.profileImage.getContext()).load(dp).into(holder.profileImage);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CharSequence options[] = new CharSequence[]{
                              name + "'s Profile",
                              "Send Message"
                            };
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setTitle("Select options");

                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(which  == 0){
                                        Intent profileIntent = new Intent(v.getContext(),PersonProfileActivity.class);
                                        profileIntent.putExtra("visit_user_id",usersIDs);
                                        v.getContext().startActivity(profileIntent);
                                    }
                                    if(which == 1){
                                        Intent chatIntent = new Intent(v.getContext(),ChatActivity.class);
                                        chatIntent.putExtra("visit_user_id",usersIDs);
                                        chatIntent.putExtra("userName",name);
                                        v.getContext().startActivity(chatIntent);
                                    }
                                }
                            });
                            builder.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @NonNull
    @Override
    public myViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
        return new myViewholder(view);
    }

    class myViewholder extends RecyclerView.ViewHolder{

        TextView date,fullname;
        CircleImageView profileImage;

        public myViewholder(@NonNull View itemView) {
            super(itemView);
            date = (TextView)itemView.findViewById(R.id.all_users_status);
            fullname = (TextView)itemView.findViewById(R.id.all_users_profile_name);
            profileImage = (CircleImageView)itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
