package com.example.learn;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class ChatFragment extends Fragment implements Chat_Adapter.OnChatClickListener {
    public static String userUid;
    Chat_Adapter chatAdapter;
    RecyclerView chatRecyclerView;
    List<Chat> userMessages;
    Button newMessageSend;
    EditText newMessageEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, container, false);
        userMessages = new ArrayList<>();
        buildChatRecyclerView(view);
        getChats();

        newMessageSend = view.findViewById(R.id.newMessageSend);
        newMessageEditText = view.findViewById(R.id.newMessageEditText);

        newMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:sss");
                String date = format.format(new Date());

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Chats").child(MainActivity.currentUserID).child(userUid);

                HashMap hashMap = new HashMap();
                hashMap.put(date, newMessageEditText.getText().toString());

                reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                        } else
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    void getChats() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats").child(MainActivity.currentUserID).child(userUid);
        Query refo = ref.orderByKey();
        refo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userMessages.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    userMessages.add(new Chat(ds.getValue().toString(), 1));
                    Log.e("aaa", ds.getValue().toString());
                }
                chatAdapter.notifyDataSetChanged();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats").child(userUid).child(MainActivity.currentUserID);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            userMessages.add(new Chat(ds.getValue().toString(), 0));

                            Log.e("aaa", ds.getValue().toString());

                        }
                        chatAdapter.notifyDataSetChanged();

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

    void buildChatRecyclerView(View view) {
        chatAdapter = new Chat_Adapter(userMessages, getContext(), this);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    @Override
    public void onChatClick(int position) {

    }
}