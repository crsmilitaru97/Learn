package com.example.learn;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatEntriesFragment extends Fragment implements Chat_Entries_Adapter.OnChatClickListener {
    List<String> chatEntryUidList;
    List<String> chatLastMessageList;
    Chat_Entries_Adapter chatEntriesAdapter;
    RecyclerView chatEntriesRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_entries, container, false);
        chatEntryUidList = new ArrayList<>();

        buildChatEntriesRecyclerView(view);
        getChats();

        return view;
    }

    @Override
    public void onChatClick(int position) {
        ChatFragment.userUid = chatEntryUidList.get(position);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatFragment()).addToBackStack("Chat").commit();
    }

    void getChats() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats").child(MainActivity.currentUserID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    chatEntryUidList.add(ds.getKey());
                    Log.e("aaa", ds.getKey());
                }
                chatEntriesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    void buildChatEntriesRecyclerView(View view) {
        chatEntriesAdapter = new Chat_Entries_Adapter(chatEntryUidList, getContext(), this);

        chatEntriesRecyclerView = view.findViewById(R.id.chatEntriesRecyclerView);
        chatEntriesRecyclerView.setHasFixedSize(true);
        chatEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatEntriesRecyclerView.setAdapter(chatEntriesAdapter);
    }
}