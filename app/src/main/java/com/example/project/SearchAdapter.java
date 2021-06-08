package com.example.project;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends FirebaseRecyclerAdapter<FindFriends, SearchAdapter.myViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public SearchAdapter(@NonNull FirebaseRecyclerOptions<FindFriends> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull FindFriends model) {
        holder.fullname.setText(model.getFullname());
        holder.about.setText(model.getAbout());
        Glide.with(holder.img.getContext()).load(model.getProfileimage()).into(holder.img);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String visit_user_id = getRef(position).getKey();
                Intent profileIntent = new Intent(v.getContext(),PersonProfileActivity.class);
                profileIntent.putExtra("visit_user_id",visit_user_id);
                v.getContext().startActivity(profileIntent);
             }
        });
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{
        CircleImageView img;
        TextView fullname,about;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            img = (CircleImageView)itemView.findViewById(R.id.all_users_profile_image);
            fullname = (TextView)itemView.findViewById(R.id.all_users_profile_name);
            about = (TextView)itemView.findViewById(R.id.all_users_status);
        }
    }
}
