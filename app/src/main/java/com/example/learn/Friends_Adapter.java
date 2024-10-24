package com.example.learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Friends_Adapter extends RecyclerView.Adapter<Friends_Adapter.ViewHolder> {
    private OnUserClickListener mListener;
    private List<User> listUsers;
    private Context context;

    public Friends_Adapter(List<User> listUsers, Context context, OnUserClickListener listener) {
        this.listUsers = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_list_item, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        User listUser = listUsers.get(position);

        //set on layout
        holder.friendNameText.setText(listUser.getName());
        Picasso.get().load(listUser.getImage()).placeholder(R.drawable.avatar).into(holder.friendProfileImage);
    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    public interface OnUserClickListener {
        void onUserClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView friendNameText;
        public ImageView friendProfileImage;

        private OnUserClickListener onItemClickListener;

        public ViewHolder(@NonNull View itemView, final OnUserClickListener onUserClickListener) {
            super(itemView);

            friendNameText = itemView.findViewById(R.id.friendNameText);
            friendProfileImage = itemView.findViewById(R.id.friendProfileImage);

            this.onItemClickListener = onUserClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onUserClick(getAdapterPosition());
        }
    }
}

