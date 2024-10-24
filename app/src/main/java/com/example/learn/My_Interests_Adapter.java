package com.example.learn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class My_Interests_Adapter extends RecyclerView.Adapter<My_Interests_Adapter.ViewHolder> {
    private OnInterestClick mListener;
    private List<String> listUsers;
    private Context context;

    public My_Interests_Adapter(List<String> listUsers, Context context, OnInterestClick listener) {
        this.listUsers = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_item_profile, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        public TextView interestText;

        private OnInterestClick onItemClickListener;

        public ViewHolder(@NonNull final View itemView, final OnInterestClick onInterestClick) {
            super(itemView);

            interestText = itemView.findViewById(R.id.interestText);

            this.onItemClickListener = onInterestClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onInterestClick(getAdapterPosition());
        }
    }
}

