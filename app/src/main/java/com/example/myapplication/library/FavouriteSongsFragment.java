package com.example.myapplication.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SongAdapter;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavouriteSongsFragment extends Fragment {

    private static final String TAG = "FavouriteSongsFragment";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouriteSongs;
    private SongAdapter songAdapter;
    private List<Song> favouriteSongs;

    public FavouriteSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_songs, container, false);
        rvFavouriteSongs = view.findViewById(R.id.rvFavouriteSongs);
        setupRecyclerView();
        loadFavouriteSongs();
        return view;
    }

    private void setupRecyclerView() {
        favouriteSongs = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), favouriteSongs);
        rvFavouriteSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavouriteSongs.setAdapter(songAdapter);
    }

    private void loadFavouriteSongs() {
        String userId = getUserId();
        if (userId == null) {
            handleNoUser();
            return;
        }
        DatabaseReference favouritesRef = getFavouritesRef(userId);
        fetchFavouriteSongIds(favouritesRef);
    }

    private String getUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");
        }
        return null;
    }

    private void handleNoUser() {
        Log.d(TAG, "loadFavouriteSongs: User is not logged in");
        // Consider showing a message to the user here, e.g., using a TextView
    }

    private DatabaseReference getFavouritesRef(String userId) {
        return FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("FavouritesSongs").child(userId);
    }

    private void fetchFavouriteSongIds(DatabaseReference favouritesRef) {
        favouritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> favouriteSongIds = getFavouriteSongIds(dataSnapshot);
                loadSongDetails(favouriteSongIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private List<String> getFavouriteSongIds(DataSnapshot dataSnapshot) {
        List<String> favouriteSongIds = new ArrayList<>();
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            if (snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class)) {
                String songId = snapshot.getKey().toLowerCase(); // Convert to lowercase
                favouriteSongIds.add(songId);
                Log.d(TAG, "favouriteSongId: " + songId);
            }
        }
        return favouriteSongIds;
    }

    private void loadSongDetails(List<String> songIds) {
        if (songIds.isEmpty()) {
            handleEmptyList();
            return;
        }
        DatabaseReference songsRef = getSongsRef();
        fetchSongDetails(songsRef, songIds);
    }

    private void handleEmptyList() {
        favouriteSongs.clear();
        songAdapter.updateSongs(favouriteSongs); // Use the updateSongs method in adapter
    }

    private DatabaseReference getSongsRef() {
        return FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Songs");
    }

    private void fetchSongDetails(DatabaseReference songsRef, List<String> songIds) {
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Song> songs = getSongs(dataSnapshot, songIds);
                updateUI(songs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handleDatabaseError(databaseError);
            }
        });
    }

    private List<Song> getSongs(DataSnapshot dataSnapshot, List<String> songIds) {
        List<Song> songs = new ArrayList<>();
        Log.d(TAG, "getSongs: songIds: " + songIds);
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            //  songId is a *field* within the song data, not the key
            String songId = snapshot.child("songId").getValue(String.class);
            if (songId != null) {
                songId = songId.toLowerCase(); // Convert to lowercase for comparison
                Log.d(TAG, "getSongs: Checking songId: " + songId);
                if (songIds.contains(songId)) {
                    Log.d(TAG, "getSongs: SongId " + songId + " found in songIds");
                    Song song = snapshot.getValue(Song.class);
                    if (song != null) {
                        song.setSongId(songId); // Use the songId from the database
                        song.setFileUrl(convertDriveUrl(song.getFileUrl()));
                        song.setFavourite(true);
                        songs.add(song);
                        Log.d(TAG, "Song added: " + song.getTitle() + ", songId: " + song.getSongId());
                    } else {
                        Log.w(TAG, "getSongs: Song data is null for songId: " + songId);
                    }
                }
            } else {
                Log.w(TAG, "getSongs: SongId is null for this song entry");
            }
        }
        Log.d(TAG, "getSongs: Returning songs list with size: " + songs.size());
        return songs;
    }


    private void updateUI(List<Song> songs) {
        favouriteSongs.clear();
        favouriteSongs.addAll(songs);
        Log.d(TAG, "updateUI: favouriteSongs size: " + favouriteSongs.size());
        songAdapter.updateSongs(favouriteSongs);
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        Log.e(TAG, "Database Error: " + databaseError.getMessage());
        // Consider showing a user-friendly error message here
    }

    private String convertDriveUrl(String url) {
        if (url == null) return null; // Handle null URL
        String pattern = "[-\\w]{25,}";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(url);
        if (m.find()) {
            return "https://drive.google.com/uc?export=download&id=" + m.group();
        }
        return url;
    }
}

