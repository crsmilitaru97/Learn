package com.example.learn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InterestsActivity extends AppCompatActivity implements Interests_Adapter.OnInterestClick {
    List<String> listInterests;
    Interests_Adapter interestsAdapter;
    RecyclerView interestsRecyclerView;
    ProgressBar interestsProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        listInterests = new ArrayList<>();

        interestsProgressBar = findViewById(R.id.interestsProgressBar);

        buildInterestsRecyclerView();

        Button interestNextButton = findViewById(R.id.interestNextButton);
        interestNextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(InterestsActivity.this, MainActivity.class));
            }
        });


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Interests");
        Query refOrdered = ref.orderByValue();
        refOrdered.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                interestsProgressBar.setVisibility(View.VISIBLE);
                listInterests.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    listInterests.add(ds.getValue().toString());
                }
                interestsAdapter.notifyDataSetChanged();
                interestsProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onInterestClick(int position) {

    }

    void buildInterestsRecyclerView() {
        interestsAdapter = new Interests_Adapter(listInterests, getApplicationContext(), this);

        interestsRecyclerView = findViewById(R.id.interestsRecyclerView);
        interestsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        interestsRecyclerView.setLayoutManager(mLayoutManager);
        interestsRecyclerView.setAdapter(interestsAdapter);
    }
}