package com.example.myapplication.nowplaying;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Search.SearchItem;
import com.example.myapplication.adapters.SearchAdapter;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlaylistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";

    private ImageView btnBack, imgPlaylistCover, btnShuffle, btnAddToFavorites;
    private TextView txtTitle;
    private RecyclerView recyclerSongs;
    private SearchAdapter searchAdapter;
    private List<SearchItem.SongItem> searchItems = new ArrayList<>();
    private HashMap<String, Song> allSongs = new HashMap<>();
    private String playlistId;
    private boolean isFavorite = false;

    private DatabaseReference databaseRef, favoritesRef;
    private FirebaseAuth auth;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        favoritesRef = databaseRef.child("FavouritesPlaylist");

        btnBack = findViewById(R.id.btn_back);
        imgPlaylistCover = findViewById(R.id.img_playlist_cover);
        txtTitle = findViewById(R.id.txt_playlist_title);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnAddToFavorites = findViewById(R.id.btn_add_to_favorites);
        recyclerSongs = findViewById(R.id.recycler_playlist_detail);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        ImageView btnPlayIcon = findViewById(R.id.btn_play_icon);

        searchAdapter = new SearchAdapter(this);
        recyclerSongs.setAdapter(searchAdapter);

        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onSongClick(Song song) {
                ArrayList<Song> songList = new ArrayList<>();
                for (SearchItem.SongItem item : searchItems) {
                    songList.add(item.getSong());
                }

                Intent intent = new Intent(PlaylistDetailActivity.this, NowPlayingActivity.class);
                intent.putExtra("song_id", song.getSongId());
                intent.putParcelableArrayListExtra("playlist_songs", songList); // <-- đúng cách
                startActivity(intent);

            }

            @Override
            public void onPlaylistClick(Playlist playlist) {
                // Không xử lý ở đây
            }
        });

        playlistId = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);
        String playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);
        txtTitle.setText(playlistName != null ? playlistName : "Playlist");

        loadAllSongs(() -> loadPlaylistDetails(playlistId));

        btnBack.setOnClickListener(v -> onBackPressed());
        btnShuffle.setOnClickListener(v -> shuffleSongs());
        btnAddToFavorites.setOnClickListener(v -> toggleFavoritePlaylist());
        btnPlayIcon.setOnClickListener(v -> {
            if (!searchItems.isEmpty()) {
                Song firstSong = searchItems.get(0).getSong();
                ArrayList<Song> songList = new ArrayList<>();
                for (SearchItem.SongItem item : searchItems) {
                    songList.add(item.getSong());
                }

                Intent intent = new Intent(PlaylistDetailActivity.this, NowPlayingActivity.class);
                intent.putExtra("song_id", firstSong.getSongId());
                intent.putParcelableArrayListExtra("playlist_songs", songList);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Danh sách bài hát trống", Toast.LENGTH_SHORT).show();
            }
        });

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
                    }
                }
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PlaylistDetail", "Error loading songs: " + error.getMessage());
            }
        });
    }

    private void loadPlaylistDetails(String playlistId) {
        if (playlistId == null) return;

        databaseRef.child("Playlists").child(playlistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Playlist playlist = snapshot.getValue(Playlist.class);
                        if (playlist != null && playlist.getListOfSongIds() != null && !playlist.getListOfSongIds().isEmpty()) {
                            loadPlaylistSongs(playlist);
                            checkIfFavorite();
                        } else {
                            Toast.makeText(PlaylistDetailActivity.this, "Playlist không có bài hát", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PlaylistDetail", "Error loading playlist details: " + error.getMessage());
                    }
                });
    }

    private void loadPlaylistSongs(Playlist playlist) {
        List<SearchItem> newItems = new ArrayList<>();
        searchItems.clear();

        for (String songId : playlist.getListOfSongIds()) {
            Song song = allSongs.get(songId);
            if (song != null) {
                SearchItem.SongItem item = new SearchItem.SongItem(song);
                searchItems.add(item);
                newItems.add(item);
            }
        }
        searchAdapter.updateItems(newItems);
    }

    private void shuffleSongs() {
        if (searchItems.isEmpty()) return;

        // Trộn danh sách hiển thị
        Collections.shuffle(searchItems, new Random(System.currentTimeMillis()));
        searchAdapter.updateItems(new ArrayList<>(searchItems));
        Toast.makeText(this, "Bài hát đã được trộn", Toast.LENGTH_SHORT).show();

        // Cập nhật danh sách lên Firebase
        List<String> shuffledSongIds = new ArrayList<>();
        for (SearchItem.SongItem item : searchItems) {
            shuffledSongIds.add(item.getSong().getSongId());
        }

        databaseRef.child("Playlists").child(playlistId).child("listOfSongIds")
                .setValue(shuffledSongIds)
                .addOnSuccessListener(aVoid -> Log.d("PlaylistDetail", "Đã cập nhật danh sách bài hát đã trộn lên Firebase"))
                .addOnFailureListener(e -> Log.e("PlaylistDetail", "Lỗi khi cập nhật Firebase: " + e.getMessage()));
    }


    private void checkIfFavorite() {
        if (auth.getCurrentUser() == null || playlistId == null) return;

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");
        favoritesRef.child(userId).child(playlistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = snapshot.exists();
                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PlaylistDetail", "Error checking favorite: " + error.getMessage());
                    }
                });
    }

    private void updateFavoriteIcon() {
        btnAddToFavorites.setImageResource(
                isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection
        );
    }

    private void toggleFavoritePlaylist() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");

        favoritesRef.child(userId).child(playlistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            favoritesRef.child(userId).child(playlistId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        isFavorite = false;
                                        updateFavoriteIcon();
                                        Toast.makeText(PlaylistDetailActivity.this, "Đã xóa khỏi playlist yêu thích", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Map<String, Object> favorite = new HashMap<>();
                            favorite.put("playlistId", playlistId);
                            favorite.put("addedDate", System.currentTimeMillis());

                            favoritesRef.child(userId).child(playlistId).setValue(favorite)
                                    .addOnSuccessListener(aVoid -> {
                                        isFavorite = true;
                                        updateFavoriteIcon();
                                        Toast.makeText(PlaylistDetailActivity.this, "Đã thêm vào playlist yêu thích", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PlaylistDetail", "Error toggling favorite: " + error.getMessage());
                    }
                });
    }
}
