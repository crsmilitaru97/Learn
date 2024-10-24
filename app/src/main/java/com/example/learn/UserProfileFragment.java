package com.example.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UserProfileFragment extends Fragment implements Posts_Adapter.OnPostClickListener, My_Interests_Adapter.OnInterestClick {
    public static String userUid, userName, userImage, userEmail, userCountry, userProfession;
    public static boolean isPrivate, hasImage;
    public RecyclerView userPostsRecyclerView, userProfileInterestsRecyclerView;
    public TextView privateAccountText, noPostsAccountText, textViewUserEmail;
    private boolean isFollower;
    private View view;
    private String currentUserID;
    private Button followButton, chatButton;
    private Posts_Adapter postsAdapter;
    private List<Post> postsList;
    private My_Interests_Adapter my_Interest_Adapter;
    private List<String> interestsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        super.onCreate(savedInstanceState);

        //Show back button
        Toolbar mToolbar = (Toolbar) view.findViewById(R.id.userProfileToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        currentUserID = MainActivity.currentUserID;
        userPostsRecyclerView = view.findViewById(R.id.userPostsRecyclerView);
        userProfileInterestsRecyclerView = view.findViewById(R.id.userProfileInterestsRecyclerView);

        postsList = new ArrayList<>();
        interestsList = new ArrayList<>();

        this.view = view;
        Toolbar topBar = view.findViewById(R.id.userProfileToolbar);

        followButton = view.findViewById(R.id.followButton);
        chatButton = view.findViewById(R.id.chatButton);
        privateAccountText = view.findViewById(R.id.privateAccountText);
        noPostsAccountText = view.findViewById(R.id.noPostsAccountText);

        ImageView userProfileImage = view.findViewById(R.id.userProfileImage);
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail);

        TextView textViewUserCountry = view.findViewById(R.id.textViewUserCountry);
        TextView textViewUserProfession = view.findViewById(R.id.textViewUserProfession);

        userProfileInterestsRecyclerView();


        noPostsAccountText.setVisibility(View.GONE);

        if (hasImage)
            Picasso.get().load(userImage).placeholder(R.drawable.avatar).into(userProfileImage);
        topBar.setTitle(userName);


        textViewUserCountry.setText(userCountry);
        textViewUserProfession.setText(userProfession);

        //Schimba textul butonului in functie daca utilizatorul e/nu e urmaritor
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Followers").child(currentUserID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.child(userUid).getValue().toString().equals("accepted")) {
                        isFollower = true;
                        followButton.setText("Unfollow");
                    } else {
                        isFollower = false;
                        if (dataSnapshot.child(userUid).getValue().toString().equals("pending"))
                            followButton.setText("Pending");
                        else if (dataSnapshot.child(userUid).getValue().toString().equals("request"))
                            followButton.setText("Accept?");
                    }
                } catch (Exception e) {
                    followButton.setText("Follow");
                    e.printStackTrace();
                }

                checkAndShowData(isPrivate, isFollower);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Followers");

                if (followButton.getText().equals("Follow")) {
                    HashMap map = new HashMap();

                    if (isPrivate)
                        map.put(userUid, "pending");
                    else
                        map.put(userUid, "accepted");

                    reference.child(currentUserID).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                if (isPrivate) {
                                    isFollower = false;
                                    Toast.makeText(getActivity(), "Follow request has been sent", Toast.LENGTH_SHORT).show();
                                    followButton.setText("Pending");

                                } else {
                                    isFollower = true;
                                    Toast.makeText(getActivity(), "Following", Toast.LENGTH_SHORT).show();
                                    followButton.setText("Unfollow");
                                }
                            } else
                                Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (isPrivate) {
                        HashMap map2 = new HashMap();
                        map2.put(currentUserID, "request");
                        reference.child(userUid).updateChildren(map2).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {

                                } else
                                    Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } else if (followButton.getText().equals("Unfollow")) {
                    isFollower = false;
                    Toast.makeText(getActivity(), "Unfollowing", Toast.LENGTH_SHORT).show();

                    reference.child(currentUserID).child(userUid).removeValue();
                    reference.child(userUid).child(currentUserID).removeValue();

                    followButton.setText("Follow");

                } else {

                    isFollower = true;
                    Toast.makeText(getActivity(), "Follow request accepted", Toast.LENGTH_SHORT).show();

                    reference.child(userUid).child(currentUserID).setValue("accepted");
                    reference.child(currentUserID).child(userUid).removeValue();
                    followButton.setText("Follow");
                }
                checkAndShowData(isPrivate, isFollower);
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss");
                    String date = format.format(new Date());

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(MainActivity.currentUserID).child(userUid);

                    HashMap hashMap = new HashMap();
                    hashMap.put(date, "");

                    reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                            } else
                                Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        return view;
    }

    void checkAndShowData(boolean isPrivate, boolean isFollower) {
        if (isPrivate && !isFollower) {
            textViewUserEmail.setText("Private");
            privateAccountText.setVisibility(View.VISIBLE);
            userPostsRecyclerView.setVisibility(View.GONE);
            postsList.clear();
        }
        if (isFollower || !isPrivate) {
            //interest
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userUid).child("interests");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    interestsList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        interestsList.add(ds.getValue().toString());
                    }
                    my_Interest_Adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            userPostsRecyclerView.setVisibility(View.VISIBLE);
            textViewUserEmail.setText(userEmail);
            LoadUserPosts();
            privateAccountText.setVisibility(View.GONE);
        }

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

    void LoadUserPosts() {
        buildUserPostsRecyclerView();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.orderByChild("date");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("userUid").getValue().equals(userUid)) {
                        Post myPost = ds.getValue(Post.class);
                        postsList.add(myPost);
                        java.util.Collections.sort(postsList);
                        java.util.Collections.reverse(postsList);
                    }
                }
                if (postsList.isEmpty())
                    noPostsAccountText.setVisibility(View.VISIBLE);
                else
                    noPostsAccountText.setVisibility(View.GONE);
                postsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void buildUserPostsRecyclerView() {
        postsAdapter = new Posts_Adapter(postsList, getContext(), this);

        userPostsRecyclerView.setHasFixedSize(true);
        userPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userPostsRecyclerView.setAdapter(postsAdapter);
    }

    void userProfileInterestsRecyclerView() {
        my_Interest_Adapter = new My_Interests_Adapter(interestsList, getContext(), this);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        userProfileInterestsRecyclerView.setHasFixedSize(true);
        userProfileInterestsRecyclerView.setLayoutManager(layoutManager);
        userProfileInterestsRecyclerView.setAdapter(my_Interest_Adapter);
    }

    @Override
    public void onPostClick(int position) {

    }

    @Override
    public void onInterestClick(int position) {

    }
}