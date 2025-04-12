package com.example.myapplication.detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Search.SearchItem;
import com.example.myapplication.adapters.SearchAdapter;
import com.example.myapplication.models.Album;
import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.example.myapplication.nowplaying.NowPlayingActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class AlbumDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_ID = "album_id";
    public static final String EXTRA_ALBUM_NAME = "album_name";
    public static final String EXTRA_ALBUM_COVER = "album_cover";

    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private ImageView btnBack, imgAlbumCover, btnShuffle, btnPlayIcon;
    private TextView txtTitle;
    private RecyclerView recyclerSongs;
    private TextView txtArtistName;

    private SearchAdapter searchAdapter;
    private final List<SearchItem.SongItem> searchItems = new ArrayList<>();
    private final HashMap<String, Song> allSongs = new HashMap<>();

    private DatabaseReference databaseRef;
    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_detail);

        initViews();
        setupRecyclerView();
        setupListeners();

        databaseRef = FirebaseDatabase.getInstance(DB_URL).getReference();

        // Get the data from Intent
        Intent intent = getIntent();
        albumId = intent.getStringExtra(EXTRA_ALBUM_ID);
        String albumName = intent.getStringExtra(EXTRA_ALBUM_NAME);
        String albumCover = intent.getStringExtra(EXTRA_ALBUM_COVER);

        // Display the album information
        txtTitle.setText(albumName != null ? albumName : "Album");
        if (albumCover != null) {
            Glide.with(this).load(albumCover).into(imgAlbumCover);
        }

        Log.d("AlbumDetail", "albumId received: " + albumId);

        // Load all songs and then the album's songs
        loadAllSongs(() -> loadAlbumSongs(albumId));
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        imgAlbumCover = findViewById(R.id.album_cover_detail);
        txtTitle = findViewById(R.id.txt_album_name);
        txtArtistName = findViewById(R.id.txt_artist_name);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnPlayIcon = findViewById(R.id.btn_play_icon);
        recyclerSongs = findViewById(R.id.recycler_album_detail);
    }

    private void setupRecyclerView() {
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this);
        recyclerSongs.setAdapter(searchAdapter);

        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onSongClick(Song song) {
                playSong(song);
            }

            @Override
            public void onPlaylistClick(Playlist playlist) {
                // Do nothing
            }

            @Override
            public void onAlbumClick(Album album) {
                // Do nothing
            }

            @Override
            public void onArtistClick(Artist artist) {
                // Do nothing
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnShuffle.setOnClickListener(v -> {
            if (!searchItems.isEmpty()) {
                Collections.shuffle(searchItems, new Random(System.currentTimeMillis()));
                searchAdapter.updateItems(new ArrayList<>(searchItems));
                Toast.makeText(this, "Bài hát đã được trộn", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlayIcon.setOnClickListener(v -> {
            if (!searchItems.isEmpty()) {
                playSong(searchItems.get(0).getSong());
            } else {
                Toast.makeText(this, "Danh sách bài hát trống", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playSong(Song song) {
        ArrayList<Song> songList = new ArrayList<>();
        for (SearchItem.SongItem item : searchItems) {
            songList.add(item.getSong());
        }

        Intent intent = new Intent(AlbumDetailActivity.this, NowPlayingActivity.class);
        intent.putExtra("song_id", song.getSongId());
        intent.putParcelableArrayListExtra("playlist_songs", songList);
        startActivity(intent);
    }

    private void loadAllSongs(Runnable onLoaded) {
        databaseRef.child("Songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot songSnap : snapshot.getChildren()) {
                    Song song = songSnap.getValue(Song.class);
                    if (song != null && song.getSongId() != null) {
                        allSongs.put(song.getSongId(), song);
                        Log.d("AlbumDetail", "Loaded song: " + song.getTitle() + " (" + song.getSongId() + ")");
                    }
                }
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AlbumDetail", "Error loading songs: " + error.getMessage());
            }
        });
    }

    private void loadAlbumSongs(String albumId) {
        databaseRef.child("Albums").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchItems.clear();
                List<SearchItem> newItems = new ArrayList<>();

                Log.d("AlbumDetail", "Looking for albumId: " + albumId);

                for (DataSnapshot albumSnap : snapshot.getChildren()) {
                    String currentId = albumSnap.child("albumId").getValue(String.class);
                    Log.d("AlbumDetail", "Checking album: " + currentId);

                    if (albumId.equals(currentId)) {
                        String artistName = albumSnap.child("artist").getValue(String.class);
                        Log.d("AlbumDetail", "Found album. Artist: " + artistName);
                        txtArtistName.setText(artistName != null ? artistName : "Unknown Artist");

                        DataSnapshot listSnap = albumSnap.child("listOfSongIds");
                        for (DataSnapshot idSnap : listSnap.getChildren()) {
                            String songId = idSnap.getValue(String.class);
                            Log.d("AlbumDetail", "Found songId in album: " + songId);
                            Song song = allSongs.get(songId);
                            if (song != null) {
                                SearchItem.SongItem item = new SearchItem.SongItem(song);
                                searchItems.add(item);
                                newItems.add(item);
                            } else {
                                Log.w("AlbumDetail", "Song ID " + songId + " not found in allSongs");
                            }
                        }

                        break; // Found album, stop searching
                    }
                }

                Log.d("AlbumDetail", "Loaded total " + newItems.size() + " songs for album");
                searchAdapter.updateItems(newItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AlbumDetail", "Error loading album songs: " + error.getMessage());
            }
        });
    }
}
