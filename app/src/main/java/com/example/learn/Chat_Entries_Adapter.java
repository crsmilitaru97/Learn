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

import java.util.List;

public class Chat_Entries_Adapter extends RecyclerView.Adapter<Chat_Entries_Adapter.ViewHolder> {
    private OnChatClickListener mListener;
    private List<String> listChatEntries;
    private Context context;

    public Chat_Entries_Adapter(List<String> listUsers, Context context, OnChatClickListener listener) {
        this.listChatEntries = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_entry_item, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        String chatEntry = listChatEntries.get(position);

        //set on layout
       // holder.textViewName.setText(chatEntry);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(chatEntry);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    holder.chatEntryName.setText(dataSnapshot.child("name").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.avatar).into(holder.chatEntryImageProfile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });













       // holder.textViewLastConv.setText(chatEntry.getLastConv());
       // Picasso.get().load(chatEntry.getUser().getImage()).placeholder(R.drawable.avatar).into(holder.imageViewProfile);
    }

    @Override
    public int getItemCount() {
        return listChatEntries.size();
    }

    public interface OnChatClickListener {
        void onChatClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView chatEntryName;
        public TextView chatEntryLastMessage;
        public ImageView chatEntryImageProfile;

        private OnChatClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnChatClickListener onUserClickListener) {
            super(itemView);

            chatEntryName = itemView.findViewById(R.id.chatEntryName);
            chatEntryLastMessage = itemView.findViewById(R.id.chatEntryLastMessage);
            chatEntryImageProfile = itemView.findViewById(R.id.chatEntryImageProfile);

            this.onItemClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onChatClick(getAdapterPosition());
        }
    }
}

