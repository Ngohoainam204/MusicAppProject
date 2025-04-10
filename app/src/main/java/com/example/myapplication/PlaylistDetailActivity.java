package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PlaylistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        String playlistId = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);
        String playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);

        TextView title = findViewById(R.id.txt_playlist_title);
        title.setText(playlistName + " (" + playlistId + ")");

        // TODO: load list of songs từ Firebase theo playlistId và hiển thị bằng RecyclerView
    }
}
