package com.example.learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Comments_Adapter extends RecyclerView.Adapter<Comments_Adapter.ViewHolder> {
    List<Comment> commentsList;
    Context context;
    private Comments_Adapter.OnCommentClickListener mListener;

    public Comments_Adapter(List<Comment> listComments, Context context, Comments_Adapter.OnCommentClickListener listener) {
        this.commentsList = listComments;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public Comments_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment, parent, false);

        return new Comments_Adapter.ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Comment comment = commentsList.get(position);

        //Text
        holder.commentText.setText(comment.getText());

        //Date
        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss").parse(comment.getDate());
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM\nHH:mm");
            String date = format.format(date1);
            holder.commentDate.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Profile // Name
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(comment.getUserUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    holder.commentName.setText(dataSnapshot.child("name").getValue().toString());
                    if ((boolean) dataSnapshot.child("hasImage").getValue())
                        Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.avatar).into(holder.commentProfileImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public interface OnCommentClickListener {
        void onCommentClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView commentDate;
        public TextView commentName;
        public TextView commentText;
        public ImageView commentProfileImage;

        private Comments_Adapter.OnCommentClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final Comments_Adapter.OnCommentClickListener onCommentClickListener) {
            super(itemView);

            commentDate = itemView.findViewById(R.id.commentDate);
            commentName = itemView.findViewById(R.id.commentName);
            commentText = itemView.findViewById(R.id.commentText);

            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);

            this.onItemClickListener = onCommentClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onCommentClick(getAdapterPosition());
        }
    }
}