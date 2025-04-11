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

public class ArtistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ARTIST_ID = "artistId";
    public static final String EXTRA_ARTIST_NAME = "artistName";

    private ImageView btnBack, imgArtistCover, btnShuffle, btnAddToFavorites, btnPlayIcon;
    private TextView txtTitle;
    private RecyclerView recyclerSongs;
    private SearchAdapter searchAdapter;
    private List<SearchItem.SongItem> searchItems = new ArrayList<>();
    private HashMap<String, Song> allSongs = new HashMap<>();
    private String artistId;
    private boolean isFavorite = false;

    private DatabaseReference databaseRef, favoritesRef;
    private FirebaseAuth auth;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String TAG = "ArtistDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_detail);

        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance(DB_URL).getReference();
        favoritesRef = databaseRef.child("FavouritesArtists");

        btnBack = findViewById(R.id.btn_back);
        imgArtistCover = findViewById(R.id.artist_detail_cover);
        txtTitle = findViewById(R.id.txt_artist_detail_name);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnAddToFavorites = findViewById(R.id.btn_add_to_favorites);
        recyclerSongs = findViewById(R.id.recycler_artist_detail);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));
        btnPlayIcon = findViewById(R.id.btn_play_icon);

        searchAdapter = new SearchAdapter(this);
        recyclerSongs.setAdapter(searchAdapter);
        artistId = getIntent().getStringExtra(EXTRA_ARTIST_ID);
        Log.d(TAG, "Received artistId: " + artistId);

        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onSongClick(Song song) {
                ArrayList<Song> songList = new ArrayList<>();
                for (SearchItem.SongItem item : searchItems) {
                    songList.add(item.getSong());
                }

                Intent intent = new Intent(ArtistDetailActivity.this, NowPlayingActivity.class);
                intent.putExtra("song_id", song.getSongId());
                intent.putParcelableArrayListExtra("playlist_songs", songList);
                startActivity(intent);
            }

            @Override
            public void onPlaylistClick(Playlist playlist) {
                // Không xử lý ở đây
            }

            @Override
            public void onAlbumClick(Album album) {
                // Không xử lý ở đây
            }

            @Override
            public void onArtistClick(Artist artist) {
                // Không xử lý ở đây
            }
        });


        loadAllSongs(() -> loadArtistDetails(artistId));

        btnBack.setOnClickListener(v -> onBackPressed());
        btnShuffle.setOnClickListener(v -> shuffleSongs());
        btnAddToFavorites.setOnClickListener(v -> toggleFavoriteArtist());
        btnPlayIcon.setOnClickListener(v -> playAllSongs());
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
                Log.e(TAG, "Lỗi tải tất cả bài hát: " + error.getMessage());
            }
        });
    }

    private void loadArtistDetails(String artistId) {
        if (artistId == null) return;

        databaseRef.child("Artists")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;
                        for (DataSnapshot artistSnap : snapshot.getChildren()) {
                            Artist artist = artistSnap.getValue(Artist.class);
                            if (artist != null && artistId.equals(artist.getArtistId())) {
                                Log.d(TAG, "Found artist: " + artist.getArtistName());

                                txtTitle.setText(artist.getArtistName());
                                if (artist.getAvatarUrl() != null && !artist.getAvatarUrl().isEmpty()) {
                                    Glide.with(ArtistDetailActivity.this)
                                            .load(artist.getAvatarUrl())
                                            .placeholder(R.drawable.music)
                                            .into(imgArtistCover);
                                }
                                loadArtistSongs(artist);
                                checkIfFavoriteArtist();
                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            Log.w(TAG, "Artist not found by artistId inside values");
                            Toast.makeText(ArtistDetailActivity.this, "Không tìm thấy nghệ sĩ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi tải danh sách nghệ sĩ: " + error.getMessage());
                    }
                });
    }


    private void loadArtistSongs(Artist artist) {
        List<SearchItem> newItems = new ArrayList<>();
        searchItems.clear();

        if (artist.getListOfSongIds() != null) {
            for (String songId : artist.getListOfSongIds()) {
                Song song = allSongs.get(songId);
                if (song != null) {
                    SearchItem.SongItem item = new SearchItem.SongItem(song);
                    searchItems.add(item);
                    newItems.add(item);
                }
            }
            searchAdapter.updateItems(newItems);
        } else {
            Toast.makeText(this, "Nghệ sĩ không có bài hát", Toast.LENGTH_SHORT).show();
        }
    }

    private void shuffleSongs() {
        if (searchItems.isEmpty()) return;

        Collections.shuffle(searchItems, new Random(System.currentTimeMillis()));
        searchAdapter.updateItems(new ArrayList<>(searchItems));
        Toast.makeText(this, "Bài hát đã được trộn", Toast.LENGTH_SHORT).show();

        List<String> shuffledSongIds = new ArrayList<>();
        for (SearchItem.SongItem item : searchItems) {
            shuffledSongIds.add(item.getSong().getSongId());
        }

        databaseRef.child("Artists").child(artistId).child("listOfSongIds")
                .setValue(shuffledSongIds)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Đã cập nhật danh sách bài hát đã trộn lên Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi cập nhật Firebase: " + e.getMessage()));
    }

    private void playAllSongs() {
        if (searchItems.isEmpty()) {
            Toast.makeText(this, "Danh sách bài hát trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Song> songList = new ArrayList<>();
        for (SearchItem.SongItem item : searchItems) {
            songList.add(item.getSong());
        }

        Intent intent = new Intent(ArtistDetailActivity.this, NowPlayingActivity.class);
        intent.putExtra("song_id", searchItems.get(0).getSong().getSongId());
        intent.putParcelableArrayListExtra("playlist_songs", songList);
        startActivity(intent);
    }


    private void checkIfFavoriteArtist() {
        if (auth.getCurrentUser() == null || artistId == null) return;

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");
        favoritesRef.child(userId).child(artistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavorite = snapshot.exists();
                        updateFavoriteIcon();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi kiểm tra yêu thích nghệ sĩ: " + error.getMessage());
                    }
                });
    }

    private void updateFavoriteIcon() {
        btnAddToFavorites.setImageResource(
                isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection
        );
    }

    private void toggleFavoriteArtist() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm nghệ sĩ vào yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getEmail().replace(".", "_");

        favoritesRef.child(userId).child(artistId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            favoritesRef.child(userId).child(artistId).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        isFavorite = false;
                                        updateFavoriteIcon();
                                        Toast.makeText(ArtistDetailActivity.this, "Đã xóa nghệ sĩ khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Map<String, Object> favorite = new HashMap<>();
                            favorite.put("artistId", artistId);
                            favorite.put("addedDate", System.currentTimeMillis());

                            favoritesRef.child(userId).child(artistId).setValue(favorite)
                                    .addOnSuccessListener(aVoid -> {
                                        isFavorite = true;
                                        updateFavoriteIcon();
                                        Toast.makeText(ArtistDetailActivity.this, "Đã thêm nghệ sĩ vào yêu thích", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Lỗi khi thao tác yêu thích nghệ sĩ: " + error.getMessage());
                    }
                });
    }
}