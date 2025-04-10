package com.example.myapplication.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.PlaylistAdapter;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FavouritePlaylistsFragment extends Fragment {
    private static final String TAG = "FavPlaylistsFrag"; // ✅ rút gọn < 23 ký tự
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouritePlaylists;
    private PlaylistAdapter playlistAdapter;
    private final HashSet<String> favouritePlaylistIds = new HashSet<>();
    private final HashMap<String, Song> songMap = new HashMap<>();
    private final List<Playlist> favouritePlaylists = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_playlist, container, false);

        rvFavouritePlaylists = view.findViewById(R.id.rvFavouritePlaylists);
        rvFavouritePlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistAdapter = new PlaylistAdapter(
                getContext(),
                favouritePlaylists,
                favouritePlaylistIds,
                songMap,
                playlist -> {
                }
        );
        rvFavouritePlaylists.setAdapter(playlistAdapter);

        // 🔄 Load dữ liệu bài hát trước, sau đó mới load danh sách playlist yêu thích
        loadSongsFromFirebase(this::loadFavouritePlaylists);

        return view;
    }

    private void loadSongsFromFirebase(Runnable onLoaded) {
        DatabaseReference songRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Songs");
        songRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songMap.clear();
                for (DataSnapshot songSnap : snapshot.getChildren()) {
                    Song song = songSnap.getValue(Song.class);
                    if (song != null && song.getSongId() != null) {
                        songMap.put(song.getSongId(), song);
                    }
                }
                Log.d(TAG, "✅ Loaded " + songMap.size() + " songs");
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Failed to load songs: " + error.getMessage());
            }
        });
    }

    private void loadFavouritePlaylists() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String encodedEmail = email.replace(".", "_");

        DatabaseReference userFavRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("FavouritesPlaylist").child(encodedEmail);

        userFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userFavSnapshot) {
                if (!userFavSnapshot.exists()) {
                    Toast.makeText(getContext(), "Chưa có playlist yêu thích", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference playlistsRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                        .getReference("Playlists");

                playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot playlistsSnapshot) {
                        favouritePlaylists.clear();
                        for (DataSnapshot playlistSnap : playlistsSnapshot.getChildren()) {
                            if (userFavSnapshot.hasChild(playlistSnap.getKey())) {
                                Playlist playlist = playlistSnap.getValue(Playlist.class);
                                if (playlist != null) {
                                    playlist.setPlaylistId(playlistSnap.getKey());

                                    // Ép listOfSongIds nếu null
                                    if (playlist.getListOfSongIds() == null || playlist.getListOfSongIds().isEmpty()) {
                                        List<String> songIds = new ArrayList<>();
                                        for (DataSnapshot songIdSnap : playlistSnap.child("listOfSongIds").getChildren()) {
                                            String songId = songIdSnap.getValue(String.class);
                                            if (songId != null) songIds.add(songId);
                                        }
                                        playlist.setListOfSongIds(songIds);
                                    }

                                    favouritePlaylistIds.add(playlist.getId());
                                    favouritePlaylists.add(playlist);
                                }
                            }
                        }
                        playlistAdapter.setFavouritePlaylistIds(favouritePlaylistIds);
                        playlistAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Load playlists failed: " + error.getMessage());
                        Toast.makeText(getContext(), "Lỗi khi tải playlist", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Load favourites failed: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi khi tải danh sách yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
