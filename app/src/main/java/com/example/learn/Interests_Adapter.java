package com.example.learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Interests_Adapter extends RecyclerView.Adapter<Interests_Adapter.ViewHolder> {
    private OnInterestClick mListener;
    private List<String> listUsers;
    private Context context;

    public Interests_Adapter(List<String> listUsers, Context context, OnInterestClick listener) {
        this.listUsers = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_item, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String listUser = listUsers.get(position);

        //set on layout
        holder.interestText.setText(listUser);
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    public interface OnInterestClick {
        void onInterestClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button interestText;

        private OnInterestClick onItemClickListener;

        public ViewHolder(@NonNull final View itemView, final OnInterestClick onInterestClick) {
            super(itemView);

            final String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            interestText = itemView.findViewById(R.id.interestText);
            interestText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!interestText.isSelected()) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("interests");
                        reference.push().setValue(interestText.getText());

                        interestText.setSelected(true);
                    } else {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("interests");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getValue().equals(interestText.getText()))
                                        ds.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        interestText.setSelected(false);
                    }
                }
            });
            this.onItemClickListener = onInterestClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onInterestClick(getAdapterPosition());
        }
    }

    void AddInterest() {

    }
}

