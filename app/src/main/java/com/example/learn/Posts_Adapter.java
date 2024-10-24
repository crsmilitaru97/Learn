package com.example.learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Posts_Adapter extends RecyclerView.Adapter<Posts_Adapter.ViewHolder> {
    String topCommentUserUid;
    private OnPostClickListener mListener;
    private List<Post> listPosts;
    int score;

    public Posts_Adapter(List<Post> listPosts, Context context, OnPostClickListener listener) {
        this.listPosts = listPosts;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Post post = listPosts.get(position);

        Picasso.get().load(MainActivity.image).placeholder(R.drawable.avatar).into(holder.newCommentProfileImage);

        score = (int) post.getScore();

        //set on layout
        holder.upVoteButton.setText(String.valueOf(score));
        holder.postTextTextView.setText(post.getText());

        try {
            Date date1 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss").parse(post.getDate());
            SimpleDateFormat format = new SimpleDateFormat("dd MMMM\nHH:mm");

            String date = format.format(date1);
            holder.postDateTextView.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
            holder.postDateTextView.setText("error");
        }

        if (post.getHasImage()) {
            holder.postImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(post.getImage()).placeholder(R.drawable.avatar).into(holder.postImageView);
        } else
            holder.postImageView.setVisibility(View.GONE);
        if (post.getHasComments()) {
            holder.topCommentLayout.setVisibility(View.VISIBLE);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Comments");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                    for (DataSnapshot ds : dataSnapshot1.getChildren()) {

                        if (ds.child("postUid").getValue().equals(post.getUserUid() + "_" + post.getDate())) {
                            holder.commentText.setText(ds.child("text").getValue().toString());

                            try {
                                Date date1 = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss").parse(ds.child("date").getValue().toString());
                                SimpleDateFormat format = new SimpleDateFormat("dd MMMM\nHH:mm");

                                String date = format.format(date1);
                                holder.commentDate.setText(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                holder.commentDate.setText("error");
                            }
                            topCommentUserUid = ds.child("userUid").getValue().toString();
                            break;
                        }
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                if (topCommentUserUid.equals(ds.child("uid").getValue().toString())) {
                                    holder.commentName.setText(ds.child("name").getValue().toString());
                                    if ((boolean) ds.child("hasImage").getValue())
                                        Picasso.get().load(ds.child("image").getValue().toString()).placeholder(R.drawable.avatar).into(holder.commentProfileImage);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else
            holder.topCommentLayout.setVisibility(View.GONE);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    holder.postUserNameTextView.setText(dataSnapshot.child("name").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.avatar).into(holder.postUserImageProfile);
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
        return listPosts.size();
    }

    void addNewComment(final View view, final int position, final EditText newCommentEditText) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss");
        String date = format.format(new Date());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(MainActivity.currentUserID + "_" + date);

        HashMap hashMap = new HashMap();
        hashMap.put("postUid", listPosts.get(position).getUserUid() + "_" + listPosts.get(position).getDate());
        hashMap.put("text", newCommentEditText.getText().toString());
        hashMap.put("date", date);
        hashMap.put("userUid", MainActivity.currentUserID);

        //save comment and add has comments true
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(view.getContext(), "Added", Toast.LENGTH_SHORT).show();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(listPosts.get(position).getUserUid() + "_" + listPosts.get(position).getDate());
                    HashMap hashMap = new HashMap();
                    hashMap.put("hasComments", true);
                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                newCommentEditText.getText().clear();
                            } else
                                Toast.makeText(view.getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    Toast.makeText(view.getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface OnPostClickListener {
        void onPostClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView postTextTextView;
        public TextView postDateTextView;
        public CircleImageView postUserImageProfile;
        public TextView postUserNameTextView;
        public ImageView postImageView;
        public View topCommentLayout;
        public Button newCommentSendButton;
        public EditText newCommentEditText;
        public Button postCommentsButton;
        public ImageView newCommentProfileImage;
        public Button upVoteButton;
        public Button downVoteButton;

        public ImageView commentProfileImage;
        public TextView commentDate;
        public TextView commentText;
        public TextView commentName;

        private OnPostClickListener onItemClickListener;

        public ViewHolder(@NonNull final View itemView, final OnPostClickListener onPostClickListener) {
            super(itemView);

            //post
            postCommentsButton = itemView.findViewById(R.id.postCommentsButton);
            postTextTextView = itemView.findViewById(R.id.postTextTextView);
            postDateTextView = itemView.findViewById(R.id.postDateTextView);
            postUserImageProfile = itemView.findViewById(R.id.postUserImageProfile);
            postUserNameTextView = itemView.findViewById(R.id.postUserNameTextView);
            postImageView = itemView.findViewById(R.id.postImageView);
            upVoteButton = itemView.findViewById(R.id.upVoteButton);
            downVoteButton = itemView.findViewById(R.id.downVoteButton);

            //new comment
            newCommentSendButton = itemView.findViewById(R.id.newCommentSendButton);
            newCommentEditText = itemView.findViewById(R.id.newCommentEditText);
            newCommentProfileImage = itemView.findViewById(R.id.newCommentProfileImage);

            //comment
            topCommentLayout = itemView.findViewById(R.id.topCommentLayout);
            commentDate = itemView.findViewById(R.id.commentDate);
            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);
            commentText = itemView.findViewById(R.id.commentText);
            commentName = itemView.findViewById(R.id.commentName);


            postCommentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PostFragment.selectedPostUid = listPosts.get(getAdapterPosition()).getUserUid() + "_" + listPosts.get(getAdapterPosition()).getDate();
                    ((FragmentActivity) view.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostFragment()).addToBackStack("Feed").commit();
                }
            });

            upVoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    score++;
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(listPosts.get(getAdapterPosition()).getUserUid() + "_" + listPosts.get(getAdapterPosition()).getDate());
                    reference.child("score").setValue(score);

                    //score=(int) listPosts.get(getAdapterPosition()).getScore();
                    upVoteButton.setText(String.valueOf(score));
                    upVoteButton.setSelected(true);
                    downVoteButton.setSelected(false);
                }
            });
            downVoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    score--;
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(listPosts.get(getAdapterPosition()).getUserUid() + "_" + listPosts.get(getAdapterPosition()).getDate());
                    reference.child("score").setValue(score);

                    //score=(int) listPosts.get(getAdapterPosition()).getScore();
                    upVoteButton.setText(String.valueOf(score));
                    downVoteButton.setSelected(true);
                    upVoteButton.setSelected(false);
                }
            });
            newCommentSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewComment(view, getAdapterPosition(), newCommentEditText);
                }
            });

            this.onItemClickListener = onPostClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onPostClick(getAdapterPosition());
        }
    }
}

