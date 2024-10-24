package com.example.learn;

import android.content.Context;
import android.util.Log;
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

public class Chat_Adapter extends RecyclerView.Adapter<Chat_Adapter.ViewHolder> {
    private OnChatClickListener mListener;
    private List<Chat> listChatEntries;
    private Context context;

    public Chat_Adapter(List<Chat> listUsers, Context context, OnChatClickListener listener) {
        this.listChatEntries = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Chat chatEntry = listChatEntries.get(position);
        Log.e("aaa", chatEntry.getSide()+chatEntry.getText());

        if(!chatEntry.getText().equals("")) {
            if (chatEntry.getSide() == 0)
                holder.chatMessageRight.setText(chatEntry.getText());
            else
                holder.chatMessageLeft.setText(chatEntry.getText());
        }

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
        public TextView chatMessageRight;
        public TextView  chatMessageLeft;

        private OnChatClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnChatClickListener onUserClickListener) {
            super(itemView);

            chatMessageRight = itemView.findViewById(R.id.chatMessageRight);
            chatMessageLeft = itemView.findViewById(R.id.chatMessageLeft);

            this.onItemClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onChatClick(getAdapterPosition());
        }
    }
}

