package com.example.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostFragment extends Fragment implements Comments_Adapter.OnCommentClickListener {
    public static String selectedPostUid;
    Comments_Adapter comments_adapter;
    List<Comment> commentList;
    RecyclerView commentsRecyclerView;
    TextView noCommentsTextView;
    ImageView newCommentProfileImage;
    Button newCommentSendButton;
    EditText newCommentEditText;

    public PostFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        noCommentsTextView = view.findViewById(R.id.noCommentsTextView);
        newCommentProfileImage = view.findViewById(R.id.newCommentProfileImage);
        newCommentSendButton = view.findViewById(R.id.newCommentSendButton);
        newCommentEditText = view.findViewById(R.id.newCommentEditText);

        Picasso.get().load(MainActivity.image).placeholder(R.drawable.avatar).into(newCommentProfileImage);

        commentList = new ArrayList<>();


        //Show back button
        Toolbar mToolbar = (Toolbar) view.findViewById(R.id.postToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        buildCommentsRecyclerView(view);
        LoadComments();

        newCommentSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewComment();
                MainActivity.hideKeyboard(getActivity());
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedFragment()).addToBackStack("Feed").commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void buildCommentsRecyclerView(View view) {
        comments_adapter = new Comments_Adapter(commentList, getContext(), this);

        commentsRecyclerView = view.findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setHasFixedSize(true);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentsRecyclerView.setAdapter(comments_adapter);
    }

    void LoadComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getValue(Comment.class).getPostUid().equals(selectedPostUid)) {
                        Comment comment = ds.getValue(Comment.class);
                        commentList.add(comment);
                    }
                }
                comments_adapter.notifyDataSetChanged();
                if (commentList.isEmpty()) {
                    commentsRecyclerView.setVisibility(View.GONE);
                    noCommentsTextView.setVisibility(View.VISIBLE);
                } else {
                    commentsRecyclerView.setVisibility(View.VISIBLE);
                    noCommentsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCommentClick(int position) {

    }

    void addNewComment() {
        String date = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss").format(new Date());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(MainActivity.currentUserID + "_" + date);
        HashMap hashMap = new HashMap();
        hashMap.put("postUid", selectedPostUid);
        hashMap.put("text", newCommentEditText.getText().toString());
        hashMap.put("date", date);
        hashMap.put("userUid", MainActivity.currentUserID);
        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(selectedPostUid);
                    HashMap hashMap = new HashMap();
                    hashMap.put("hasComments", true);
                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                newCommentEditText.getText().clear();
                                commentsRecyclerView.requestFocus();
                            } else
                                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}