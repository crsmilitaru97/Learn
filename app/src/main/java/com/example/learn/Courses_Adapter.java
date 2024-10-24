package com.example.learn;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

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
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.Result;

public class Courses_Adapter extends RecyclerView.Adapter<Courses_Adapter.ViewHolder> {
    String videoThumbnail2;
    private OnInterestClick mListener;
    private List<String> listUsers;
    private Context context;
    String videoTitle2;

    public Courses_Adapter(List<String> listUsers, Context context, OnInterestClick listener) {
        this.listUsers = listUsers;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String listUser = listUsers.get(position);

        holder.videoID = listUser;

        String url = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=" + listUser + "&key=" + CoursesFragment.youtube_api_key;

       // new Courses_Adapter.JsonTask().execute(url);

        Courses_Adapter.JsonTask task = new Courses_Adapter.JsonTask();
        //task.execute(url);
        try {
            String str_result= task.execute(url).get();

            try {
                JSONObject returnValue = new JSONObject(str_result);

                JSONArray jArr = returnValue.getJSONArray("items");
                for (int i = 0; i < jArr.length(); i++) {
                    // getting object from items array
                    JSONObject itemObj = jArr.getJSONObject(i);

                    // getting id object from item object
                    JSONObject idObj = itemObj.getJSONObject("snippet");

                    // getting videoId from idObject
                    if (!idObj.isNull("title")) {
                        //Log.e("aaa", idObj.getString("title"));

                        holder.videoTitle.setText(idObj.getString("title"));

                    }
                    if (!idObj.isNull("channelTitle")) {
                        Log.e("aaa", idObj.getString("channelTitle"));

                        holder.videoAuthor.setText(idObj.getString("channelTitle"));
                    }

                    JSONObject idObj2=idObj.getJSONObject("thumbnails").getJSONObject("medium");

                    if (!idObj2.isNull("url")) {
                        //Log.e("aaa", idObj2.getString("url"));
                        Picasso.get().load(idObj2.getString("url")).placeholder(R.drawable.avatar).into(holder.videoThumbnail);
                    }
                }
            }
             catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.e("aaa", videoTitle2);

            //set on layout
            //holder.videoTitle.setText(videoTitle2);
            //holder.videoAuthor.setText(videoAuthor);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return listUsers.size();
    }

    public interface OnInterestClick {
        void onInterestClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public String videoID;
        public TextView videoTitle;
        public TextView videoAuthor;
        public ImageView videoThumbnail;

        private OnInterestClick onItemClickListener;

        public ViewHolder(@NonNull final View itemView, final OnInterestClick onInterestClick) {
            super(itemView);

            videoTitle = itemView.findViewById(R.id.videoTitle);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            videoAuthor = itemView.findViewById(R.id.videoAuthor);


            this.onItemClickListener = onInterestClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            VideoFragment.videoID = listUsers.get(getAdapterPosition());


            onItemClickListener.onInterestClick(getAdapterPosition());
        }
    }
    public interface AsyncResponse {
        void processFinish(String output);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

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
                    // Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

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

            // result




        }
    }

}

