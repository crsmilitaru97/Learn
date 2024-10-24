package com.example.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment implements Friends_Adapter.OnUserClickListener {
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private String currentUserID;
    private RecyclerView friendsRecyclerView;
    private Friends_Adapter adapterFriends;
    private List<String> uids;

    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);


        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        currentUserID = mAuth.getCurrentUser().getUid();

        userList = new ArrayList<>();
        uids = new ArrayList<>();

        buildRecyclerView(view);

        getAllFriends();

        return view;
    }

    @Override
    public void onUserClick(int position) {

    }

    void buildRecyclerView(View view) {
        adapterFriends = new Friends_Adapter(userList, getContext(), this);

        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView);
        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsRecyclerView.setAdapter(adapterFriends);
    }

    void getAllFriends() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Followers").child(currentUserID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uids.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getValue().toString().equals("accepted"))
                        uids.add(ds.getKey());
                }

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            User user = ds.getValue(User.class);
                            if (uids.contains(user.getUid())) {
                                userList.add(user);
                            }
                        }
                        adapterFriends.notifyDataSetChanged();
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
    }
}