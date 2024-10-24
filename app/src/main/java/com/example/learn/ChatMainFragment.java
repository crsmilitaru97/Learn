package com.example.learn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;

public class ChatMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_chat, container, false);

        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, new ChatEntriesFragment()).commit();

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, new ChatEntriesFragment()).commit();
                        break;
                    case 1:
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.chat_fragment_container, new FriendsFragment()).commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;

    }
}