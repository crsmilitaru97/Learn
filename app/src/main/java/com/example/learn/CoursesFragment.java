package com.example.learn;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoursesFragment extends Fragment implements Courses_Adapter.OnInterestClick{
    List<String> coursesList, continueCoursesList, recommendedCoursesList;
    Courses_Adapter coursesAdapter, continueCoursesAdapter, recommendedCoursesAdapter;
    RecyclerView searchCoursesRecyclerView;
    public static String youtube_api_key = "AIzaSyDE4gD-S7IfsMCVmu9wv2xnVYAHcIYUw6A";
    RecyclerView continueWatchingRecyclerView;
    RecyclerView recommendedRecyclerView;
    ProgressDialog pd;
ProgressBar progressContinueWatching, progressRecommendedVideos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        progressContinueWatching = view.findViewById(R.id.progressContinueWatching);
        progressRecommendedVideos = view.findViewById(R.id.progressRecommendedVideos);

        coursesList = new ArrayList<>();
        continueCoursesList = new ArrayList<>();
        recommendedCoursesList = new ArrayList<>();

        buildSearchCoursesRecyclerView(view);
        buildContinueRecyclerView(view);
        buildRecommendedRecyclerView(view);

        getContinueWatchingVideos();

        progressRecommendedVideos.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(MainActivity.currentUserID).child("interests");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    searchVideos( ds.getValue().toString() , 1, 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Toolbar toolbar = getView().findViewById(R.id.coursesToolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.courses_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.app_bar_search_courses);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query.trim())) {
                    //usersRecyclerView.setVisibility(View.VISIBLE);
                    searchVideos( query , 10, 0);
                } else {
                }//usersRecyclerView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getContinueWatchingVideos() {
        progressContinueWatching.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child(MainActivity.currentUserID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                continueCoursesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Log.e("aaa", ds.getKey());
                    continueCoursesList.add(ds.getKey());
                }
                continueCoursesAdapter.notifyDataSetChanged();
                progressContinueWatching.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void searchVideos(String query, int maxResults, int type) {
        String search = "https://www.googleapis.com/youtube/v3/search?part=snippet&q=" + query + "&maxResults=" + maxResults + "&key=" + youtube_api_key;
        if(type==0)
        new JsonTask().execute(search);
        else
            new JsonTask2().execute(search);


    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(getActivity());
            pd.setMessage("Searching videos");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject returnValue = new JSONObject(result);


                JSONArray jArr = returnValue.getJSONArray("items");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject itemObj = jArr.getJSONObject(i);
                    JSONObject idObj = itemObj.getJSONObject("id");
                    if (!idObj.isNull("videoId")) {
                        coursesList.add(idObj.getString("videoId"));
                    }

                }
                coursesAdapter.notifyDataSetChanged();

                if (pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class JsonTask2 extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject returnValue = new JSONObject(result);


                JSONArray jArr = returnValue.getJSONArray("items");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject itemObj = jArr.getJSONObject(i);
                    JSONObject idObj = itemObj.getJSONObject("id");
                    if (!idObj.isNull("videoId")) {
                        recommendedCoursesList.add(idObj.getString("videoId"));
                    }

                }
                recommendedCoursesAdapter.notifyDataSetChanged();

                progressRecommendedVideos.setVisibility(View.GONE);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    void buildSearchCoursesRecyclerView(View view) {
        coursesAdapter = new Courses_Adapter(coursesList, getContext(), this);

        searchCoursesRecyclerView = view.findViewById(R.id.searchCoursesRecyclerView);
        searchCoursesRecyclerView.setHasFixedSize(true);
        searchCoursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchCoursesRecyclerView.setAdapter(coursesAdapter);
    }

    void buildRecommendedRecyclerView(View view) {
        recommendedCoursesAdapter = new Courses_Adapter(recommendedCoursesList, getContext(), this);

        recommendedRecyclerView = view.findViewById(R.id.recommendedRecyclerView);
        recommendedRecyclerView.setHasFixedSize(true);
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendedRecyclerView.setAdapter(recommendedCoursesAdapter);
    }

    void buildContinueRecyclerView(View view) {
        continueCoursesAdapter = new Courses_Adapter(continueCoursesList, getContext(), this);

        continueWatchingRecyclerView = view.findViewById(R.id.continueWatchingRecyclerView);
        continueWatchingRecyclerView.setHasFixedSize(true);
        continueWatchingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        continueWatchingRecyclerView.setAdapter(continueCoursesAdapter);
    }

    @Override
    public void onInterestClick(int position) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Courses").child(MainActivity.currentUserID);

        HashMap hashMap = new HashMap();
        hashMap.put(VideoFragment.videoID, 0);

        reference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                } else
                    Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new VideoFragment()).addToBackStack("Video").commit();
    }
}
