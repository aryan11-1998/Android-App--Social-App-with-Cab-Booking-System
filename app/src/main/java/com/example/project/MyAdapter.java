package com.example.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyAdapter extends FirebaseRecyclerAdapter<Posts,MyAdapter.PostsViewHolder> {

    Boolean LikeChecker =false;
    private DatabaseReference LikesRef;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyAdapter(@NonNull FirebaseRecyclerOptions<Posts> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

        final String PostKey = getRef(position).getKey();
        holder.username.setText(model.getFullname());
        holder.date.setText(model.getDate());
        holder.time.setText(model.getTime());
        holder.description.setText(model.getDescription());
        Glide.with(holder.profileImage.getContext()).load(model.getProfileimage()).into(holder.profileImage);
        Glide.with(holder.postImage.getContext()).load(model.getPostimage()).into(holder.postImage);
        holder.setLikeButtonStatus(PostKey);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Intent ClickPostIntent = new Intent(MyAdapter.this,ClickPostActivity.class);
                ///startListening();
               // SendUserToClickPostActivity();
                Intent i = new Intent(v.getContext(),ClickPostActivity.class);
               i.putExtra("Postkey",PostKey);
                v.getContext().startActivity(i);
            }
        });

        holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(v.getContext(),CommentsActivity.class);
                commentIntent.putExtra("Postkey",PostKey);
                v.getContext().startActivity(commentIntent);

            }
        });

        holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LikeChecker = true;

                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                LikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(LikeChecker.equals(true)){
                            if(snapshot.child(PostKey).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid()))  {
                                LikesRef.child(PostKey).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                LikeChecker = false;
                            }else {
                                LikesRef.child(PostKey).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                                LikeChecker = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });



    }



    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
        return  new PostsViewHolder(view);
    }

    class PostsViewHolder extends RecyclerView.ViewHolder{

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference LikesRef;

        ImageView postImage;
        CircleImageView profileImage;
        TextView username,date,time,description;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            postImage = (ImageView)itemView.findViewById(R.id.post_image);
            username = (TextView)itemView.findViewById(R.id.post_user_name);
            date  = (TextView)itemView.findViewById(R.id.post_date);
            time= (TextView)itemView.findViewById(R.id.post_time);
            description = (TextView)itemView.findViewById(R.id.post_description);
            LikePostButton = (ImageButton)itemView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton)itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView) itemView.findViewById(R.id.display_no_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        public void setLikeButtonStatus(final String PostKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(PostKey).hasChild(currentUserID)){
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.love);
                       // DisplayNoOfLikes.setText(Integer.toString(countLikes));
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Love")));
                    }else{
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.nolove);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+(" Love")));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}

