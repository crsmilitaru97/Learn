package com.example.learn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FeedFragment extends Fragment implements Users_Adapter.OnUserClickListener, Posts_Adapter.OnPostClickListener {
    public ImageView createPostImageView, createPostProfileImageView;
    public RecyclerView usersRecyclerView, postsRecyclerView;
    public EditText postEditText;

    private Boolean postHasImage = false;
    private Uri imageUri;
    private String downloadUrl;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ProgressDialog loadingBar;

    private Users_Adapter adapterUsers;
    private Posts_Adapter postsAdapter;

    private List<User> usersList;
    private List<Post> postsList;
    private List<String> friendsUids;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);


        loadingBar = new ProgressDialog(getContext());
        mAuth = FirebaseAuth.getInstance();

        MainActivity.currentUserID = mAuth.getCurrentUser().getUid();
        currentUserID = MainActivity.currentUserID;

        createPostImageView = view.findViewById(R.id.createPostImageView);
        createPostProfileImageView = view.findViewById(R.id.createPostProfileImageView);
        postEditText = view.findViewById(R.id.postEditText);

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    MainActivity.name = dataSnapshot.child("name").getValue().toString();
                    MainActivity.hasImage = (boolean) dataSnapshot.child("hasImage").getValue();
                    if (MainActivity.hasImage) {
                        MainActivity.image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(MainActivity.image).placeholder(R.drawable.avatar).into(createPostProfileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        usersList = new ArrayList<>();
        postsList = new ArrayList<>();
        friendsUids = new ArrayList<>();

        buildUsersRecyclerView(view);
        buildPostsRecyclerView(view);

        Button postButton = view.findViewById(R.id.postButton);
        Button addImageButton = view.findViewById(R.id.addImageButton);

        LoadPosts();

        //Add post image
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);
            }
        });

        //Post
        postButton.setOnClickListener(new View.OnClickListener() {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss");
            String date = format.format(new Date());

            @Override
            public void onClick(View view) {
                if (postHasImage) {
                    loadingBar.setTitle("Posting");
                    loadingBar.setMessage("Please wait till we finish uploading your post");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);

                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Post Images").child(currentUserID + "_" + date + ".jpg");

                    filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = uri.toString();
                                    CreatePost(date);
                                }
                            });
                        }
                    });
                } else
                    CreatePost(date);
            }
        });
        return view;
    }

    void CreatePost(String date) {
        String postText = postEditText.getText().toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserID + "_" + date);

        HashMap hashMap = new HashMap();
        hashMap.put("userUid", currentUserID);
        hashMap.put("date", date);
        hashMap.put("text", postText);
        hashMap.put("hasImage", postHasImage);
        if (postHasImage)
            hashMap.put("image", downloadUrl);
        hashMap.put("hasComments", false);
        hashMap.put("score", 0);

        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    postEditText.getText().clear();
                    createPostImageView.setVisibility(View.GONE);
                    postsRecyclerView.requestFocus();
                    MainActivity.hideKeyboard(getActivity());
                    loadingBar.dismiss();
                    Toast.makeText(getActivity(), "Posted", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).placeholder(R.drawable.avatar).into(createPostImageView);
            createPostImageView.setVisibility(View.VISIBLE);

            postHasImage = true;
        } else {

            postHasImage = false;
            createPostImageView.setVisibility(View.GONE);
        }

    }

    void buildUsersRecyclerView(View view) {
        adapterUsers = new Users_Adapter(usersList, getContext(), this);

        usersRecyclerView = view.findViewById(R.id.recyclerViewUsers);
        usersRecyclerView.setHasFixedSize(true);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(adapterUsers);
    }

    void buildPostsRecyclerView(View view) {
        postsAdapter = new Posts_Adapter(postsList, getContext(), this);

        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setHasFixedSize(true);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postsRecyclerView.setAdapter(postsAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Toolbar toolbar = getView().findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_notifications:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feed_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    usersRecyclerView.setVisibility(View.VISIBLE);
                    searchUsers(query);
                } else
                    usersRecyclerView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText.trim())) {
                    usersRecyclerView.setVisibility(View.VISIBLE);
                    searchUsers(newText);
                } else
                    usersRecyclerView.setVisibility(View.GONE);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    void searchUsers(final String query) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                    if (!user.getUid().equals(currentUserID))
                        if (user.getName().toLowerCase().contains(query) ||
                                user.getEmail().toLowerCase().substring(0, user.getEmail().indexOf('@')).contains(query))
                            usersList.add(user);
                }
                adapterUsers.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onUserClick(int position) {
        UserProfileFragment.userName = usersList.get(position).getName();
        UserProfileFragment.userImage = usersList.get(position).getImage();
        UserProfileFragment.userEmail = usersList.get(position).getEmail();
        UserProfileFragment.userCountry = usersList.get(position).getCountry();
        UserProfileFragment.userProfession = usersList.get(position).getProfession();
        UserProfileFragment.isPrivate = usersList.get(position).getIsPrivate();
        UserProfileFragment.userUid = usersList.get(position).getUid();
        UserProfileFragment.hasImage = usersList.get(position).getHasImage();

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new UserProfileFragment()).addToBackStack("UserProfile").commit();
    }

    @Override
    public void onPostClick(int position) {
        //PostFragment.selectedPostUid = postsList.get(position).getUserUid() + "_" + postsList.get(position).getDate();
        //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostFragment()).addToBackStack("Feed").commit();
    }

    void LoadPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Followers").child(currentUserID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsUids.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getValue().toString().equals("accepted"))
                        friendsUids.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        //reference.orderByChild("date");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (friendsUids.contains(ds.child("userUid").getValue()) || ds.child("userUid").getValue().equals(currentUserID)) {
                        Post postt = ds.getValue(Post.class);
                        postsList.add(postt);
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

