package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class List extends AppCompatActivity {
           RecyclerView list ;
           FirebaseFirestore firebaseFirestore;
           FirestoreRecyclerAdapter FirestoreRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
          list = (RecyclerView)findViewById(R.id.list);
          firebaseFirestore =FirebaseFirestore.getInstance();

        Query q = firebaseFirestore.collection("Videos");
        FirestoreRecyclerOptions<videosModel> options = new FirestoreRecyclerOptions.Builder<videosModel>()
                .setQuery(q,videosModel.class)
                .build();
        Log.e("Log",q.toString());
        FirestoreRecyclerAdapter = new FirestoreRecyclerAdapter<videosModel, videoViewHolde>(options) {
            @NonNull
            @Override
            public videoViewHolde onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new videoViewHolde(LayoutInflater.from(parent.getContext()).inflate(R.layout.feed,parent,false));
            }

            @Override
            protected void onBindViewHolder(@NonNull videoViewHolde holder, int position, @NonNull videosModel model) {
                    holder.textView.setText(model.getUsername().split("#")[0]);
                    holder.videoView.setVideoURI(Uri.parse(model.getVideo()));

                    holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                               holder.videoView.setVisibility(View.VISIBLE);
                               holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                Log.e("Model",model.getUsername()+" "+model.getVideo());

            }

            @Override
            public void onViewDetachedFromWindow(@NonNull videoViewHolde holder) {
                super.onViewDetachedFromWindow(holder);
                holder.videoView.seekTo(0);
            }

            @Override
            public void onViewAttachedToWindow(@NonNull videoViewHolde holder) {
                super.onViewAttachedToWindow(holder);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        holder.videoView.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                });

                holder.videoView.start();
            }
        };

        list.setLayoutManager(new LinearLayoutManager(this));
       list.setAdapter(FirestoreRecyclerAdapter);



    }

    private class videoViewHolde extends RecyclerView.ViewHolder {
         VideoView videoView;
         TextView textView;
         ProgressBar progressBar;

        public videoViewHolde(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.feedVideoview);
            textView =itemView.findViewById(R.id.feedUsername);
            progressBar =itemView.findViewById(R.id.progress_circular);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirestoreRecyclerAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.list,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                Intent intent = new Intent(List.this,Edit.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.signOut:
                Intent intent1 = new Intent(List.this,MainActivity.class);
                FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                finish();
                startActivity(intent1);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}