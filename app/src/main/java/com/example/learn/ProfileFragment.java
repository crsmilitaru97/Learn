package com.example.learn;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements Posts_Adapter.OnPostClickListener {
    public RecyclerView profilePostsRecyclerView;
    private String currentUserID;
    private CircleImageView profileImage;
    private Posts_Adapter postsAdapter;
    private List<Post> postsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        CollapsingToolbarLayout topBar = view.findViewById(R.id.topBar);

        currentUserID = MainActivity.currentUserID;

        postsList = new ArrayList<>();

        profilePostsRecyclerView = view.findViewById(R.id.profilePostsRecyclerView);
        profileImage = view.findViewById(R.id.profileImage);

        buildPostsRecyclerView(view);

        topBar.setTitle(MainActivity.name);

        //set profile
        getActivity().setTitle(MainActivity.name);
        if (MainActivity.hasImage)
            Picasso.get().load(MainActivity.image).placeholder(R.drawable.avatar).into(profileImage);

        LoadMyPosts();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.app_bar_logout:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();
                sendUserToLogin();
                return true;
            case R.id.app_bar_settings:
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack("Settings").commit();
                return true;
            case R.id.app_bar_interests:
                    Intent intent = new Intent(getActivity(), InterestsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendUserToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Toolbar toolbar = getView().findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    void buildPostsRecyclerView(View view) {
        postsAdapter = new Posts_Adapter(postsList, getContext(), this);

        profilePostsRecyclerView = view.findViewById(R.id.profilePostsRecyclerView);
        profilePostsRecyclerView.setHasFixedSize(true);
        profilePostsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        profilePostsRecyclerView.setAdapter(postsAdapter);
    }

    @Override
    public void onPostClick(int position) {

    }

    void LoadMyPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.orderByChild("date");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("userUid").getValue().equals(currentUserID)) {
                        Post myPost = ds.getValue(Post.class);
                        postsList.add(myPost);
                        java.util.Collections.sort(postsList);
                        java.util.Collections.reverse(postsList);
                    }
                }
                postsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
