package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CommentAdapter extends FirebaseRecyclerAdapter<CommentModel, CommentAdapter.myViewHolder> {


    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public CommentAdapter(@NonNull FirebaseRecyclerOptions<CommentModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull CommentModel model) {
        holder.name.setText(model.getFullname());
        holder.date.setText(model.getDate());
        holder.time.setText(model.getTime());
        holder.comment.setText(model.getComment());

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        TextView name,comment,date,time;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView)itemView.findViewById(R.id.comment_username);
            comment = (TextView)itemView.findViewById(R.id.comment_text);
            date = (TextView)itemView.findViewById(R.id.comment_date);
            time = (TextView)itemView.findViewById(R.id.comment_time);
        }
    }
}
