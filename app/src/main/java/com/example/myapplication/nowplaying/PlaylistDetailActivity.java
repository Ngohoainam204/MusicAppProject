package com.example.myapplication.nowplaying;

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

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.adapters.SearchAdapter;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.example.myapplication.Search.SearchItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class PlaylistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "playlist_id";
    public static final String EXTRA_PLAYLIST_NAME = "playlist_name";

    private ImageView btnBack, imgPlaylistCover, btnShuffle, btnAddToFavorites;
    private TextView txtTitle;
    private RecyclerView recyclerSongs;
    private SearchAdapter searchAdapter;
    private List<SearchItem> searchItems = new ArrayList<>();
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

        Log.d("PlaylistDetail", "onCreate: Activity started");

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        favoritesRef = databaseRef.child("FavouritesPlaylist");

        // Bind UI
        btnBack = findViewById(R.id.btn_back);
        imgPlaylistCover = findViewById(R.id.img_playlist_cover);
        txtTitle = findViewById(R.id.txt_playlist_title);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnAddToFavorites = findViewById(R.id.btn_add_to_favorites);
        recyclerSongs = findViewById(R.id.recycler_playlist_detail);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        searchAdapter = new SearchAdapter(this, searchItems);

        Log.d("PlaylistDetail", "Contents of searchItems before setting adapter:");
        for (SearchItem item : searchItems) {
            if (item instanceof SearchItem.SongItem) {
                Log.d("PlaylistDetail", "- Song: " + ((SearchItem.SongItem) item).getSong().getTitle());
            } else if (item instanceof SearchItem.PlaylistItem) {
                Log.d("PlaylistDetail", "- Playlist: " + ((SearchItem.PlaylistItem) item).getPlaylist().getPlaylistName());
            }
        }

        recyclerSongs.setAdapter(searchAdapter);

        playlistId = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);
        String playlistName = getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);
        txtTitle.setText(playlistName != null ? playlistName : "Playlist");

        Log.d("PlaylistDetail", "onCreate: Playlist ID: " + playlistId);
        loadAllSongs(() -> loadPlaylistDetails(playlistId));

        btnBack.setOnClickListener(v -> {
            Log.d("PlaylistDetail", "onClick: Back button clicked");
            onBackPressed();
        });
        btnShuffle.setOnClickListener(v -> {
            Log.d("PlaylistDetail", "onClick: Shuffle button clicked");
            shuffleSongs();
        });
        btnAddToFavorites.setOnClickListener(v -> {
            Log.d("PlaylistDetail", "onClick: Add to Favorites button clicked");
            toggleFavoritePlaylist();
        });
    }

    private void loadAllSongs(Runnable onLoaded) {
        Log.d("PlaylistDetail", "loadAllSongs: Loading all songs from database");
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
                Log.d("PlaylistDetail", "loadAllSongs: Songs loaded, total: " + allSongs.size());
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PlaylistDetail", "❌ Error loading songs: " + error.getMessage());
            }
        });
    }

    private void loadPlaylistDetails(String playlistId) {
        if (playlistId == null) {
            Log.e("PlaylistDetail", "loadPlaylistDetails: Playlist ID is null");
            return;
        }

        Log.d("PlaylistDetail", "loadPlaylistDetails: Loading details for playlist ID: " + playlistId);

        databaseRef.child("Playlists").child(playlistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Playlist playlist = snapshot.getValue(Playlist.class);
                        if (playlist != null && playlist.getListOfSongIds() != null && !playlist.getListOfSongIds().isEmpty()) {
                            Log.d("PlaylistDetail", "loadPlaylistDetails: Playlist loaded: " + playlist.getPlaylistName());
                            loadPlaylistSongs(playlist); // Gọi loadPlaylistSongs() trước khi thiết lập adapter
                            checkIfFavorite();
                        } else {
                            Log.w("PlaylistDetail", "⚠️ Playlist is empty or listOfSongIds is empty");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PlaylistDetail", "❌ Error loading playlist details: " + error.getMessage());
                    }
                });
    }


    private void loadPlaylistSongs(Playlist playlist) {
        if (playlist == null || playlist.getListOfSongIds() == null || playlist.getListOfSongIds().isEmpty()) {
            Log.w("PlaylistDetail", "loadPlaylistSongs: Playlist or song list is empty");
            searchItems.clear();
            searchAdapter.notifyDataSetChanged();
            return;
        }

        Log.d("PlaylistDetail", "loadPlaylistSongs: Loading songs for playlist");

        searchItems.clear(); // Clear the current list of items
        List<SearchItem> tempSearchItems = new ArrayList<>(); // Tạo một danh sách tạm thời

        for (String songId : playlist.getListOfSongIds()) {
            Song song = allSongs.get(songId);
            if (song != null) {
                tempSearchItems.add(new SearchItem.SongItem(song)); // Thêm vào danh sách tạm thời
                Log.d("PlaylistDetail", "Song added to temp list: " + song.getSongId() + ", Title: " + song.getTitle());
            } else {
                Log.w("PlaylistDetail", "⚠️ Song not found: " + songId);
            }
        }

        searchItems.addAll(tempSearchItems); // Thêm tất cả item từ danh sách tạm thời vào danh sách chính
        Log.d("PlaylistDetail", "loadPlaylistSongs: Size of searchItems before notify: " + searchItems.size());
        searchAdapter.notifyDataSetChanged(); // Gọi notify sau khi đã thêm hết

        // Tạo một adapter mới và thiết lập lại cho RecyclerView
        searchAdapter = new SearchAdapter(PlaylistDetailActivity.this, searchItems);
        recyclerSongs.setAdapter(searchAdapter);

        Log.d("PlaylistDetail", "New adapter set after loading songs");
    }


    private void shuffleSongs() {
        if (!searchItems.isEmpty()) {
            Collections.shuffle(searchItems, new Random(System.currentTimeMillis()));
            searchAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Bài hát đã được trộn", Toast.LENGTH_SHORT).show();
            Log.d("PlaylistDetail", "shuffleSongs: Songs shuffled");
        } else {
            Toast.makeText(this, "Không có bài hát để trộn", Toast.LENGTH_SHORT).show();
            Log.d("PlaylistDetail", "shuffleSongs: No songs to shuffle");
        }
    }

    private void checkIfFavorite() {
        if (auth.getCurrentUser() == null || playlistId == null) {
            Log.d("PlaylistDetail", "checkIfFavorite: User not logged in or Playlist ID is null");
            return;
        }

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");
        Log.d("PlaylistDetail", "checkIfFavorite: Checking if playlist is favorite for user: " + userId);

        favoritesRef.child(userId).child(playlistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = snapshot.exists();
                        Log.d("PlaylistDetail", "checkIfFavorite: Is favorite: " + isFavorite);
                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("PlaylistDetail", "Lỗi kiểm tra trạng thái yêu thích: " + error.getMessage());
                    }
                });
    }

    private void updateFavoriteIcon() {
        Log.d("PlaylistDetail", "updateFavoriteIcon: Updating favorite icon");
        btnAddToFavorites.setImageResource(
                isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection
        );
    }

    private void toggleFavoritePlaylist() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            Log.d("PlaylistDetail", "toggleFavoritePlaylist: User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");

        if (playlistId != null) {
            Log.d("PlaylistDetail", "toggleFavoritePlaylist: Toggling favorite status for playlist ID: " + playlistId);
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
                                            Log.d("PlaylistDetail", "toggleFavoritePlaylist: Playlist removed from favorites");
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(PlaylistDetailActivity.this, "Lỗi khi xóa khỏi yêu thích", Toast.LENGTH_SHORT).show());
                            } else {
                                Map<String, Object> favorite = new HashMap<>();
                                favorite.put("playlistId", playlistId);
                                favorite.put("addedDate", System.currentTimeMillis());

                                favoritesRef.child(userId).child(playlistId).setValue(favorite)
                                        .addOnSuccessListener(aVoid -> {
                                            isFavorite = true;
                                            updateFavoriteIcon();
                                            Toast.makeText(PlaylistDetailActivity.this, "Đã thêm vào playlist yêu thích", Toast.LENGTH_SHORT).show();
                                            Log.d("PlaylistDetail", "toggleFavoritePlaylist: Playlist added to favorites");
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(PlaylistDetailActivity.this, "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("PlaylistDetail", "❌ Error toggling favorite: " + error.getMessage());
                        }
                    });
        }
    }
}