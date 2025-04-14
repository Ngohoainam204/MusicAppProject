package com.example.myapplication.library;

import android.content.Context;
import android.content.Intent;
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

import com.example.myapplication.nowplaying.NowPlayingActivity; // Import NowPlayingActivity
import com.example.myapplication.R;
import com.example.myapplication.adapters.SongAdapter;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavouriteSongsFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private static final String TAG = "FavouriteSongsFragment";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouriteSongs;
    private SongAdapter songAdapter;
    private List<Song> favouriteSongs;
    private OnSongPlayClickListener playClickListener;

    public interface OnSongPlayClickListener {
        void onSongPlayClicked(Song song);
    }

    public FavouriteSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSongPlayClickListener) {
            playClickListener = (OnSongPlayClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement FavouriteSongsFragment.OnSongPlayClickListener");
        }
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
        songAdapter.setOnSongClickListener(this); // Set the listener to this fragment
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
                Log.d(TAG, "fetchFavouriteSongIds: Retrieved favourite song IDs: " + favouriteSongIds);
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
                Log.d(TAG, "getFavouriteSongIds: Added favourite song ID: " + songId);
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
        Log.d(TAG, "handleEmptyList: Favourite songs list is empty");
    }

    private DatabaseReference getSongsRef() {
        return FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Songs");
    }

    private void fetchSongDetails(DatabaseReference songsRef, List<String> songIds) {
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Song> songs = getSongs(dataSnapshot, songIds);
                Log.d(TAG, "fetchSongDetails: Retrieved song details. Size: " + songs.size());
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
        Log.d(TAG, "getSongs: songIds to fetch: " + songIds);
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            //  songId is a *field* within the song data, not the key
            String songIdFromDB = snapshot.child("songId").getValue(String.class);
            if (songIdFromDB != null) {
                String lowerCaseSongId = songIdFromDB.toLowerCase(); // Convert to lowercase for comparison
                Log.d(TAG, "getSongs: Checking songId from DB: " + lowerCaseSongId);
                if (songIds.contains(lowerCaseSongId)) {
                    Log.d(TAG, "getSongs: Found matching songId: " + lowerCaseSongId);
                    Song song = snapshot.getValue(Song.class);
                    if (song != null) {
                        song.setSongId(lowerCaseSongId); // Use the lowercase songId
                        String fileUrl = song.getFileUrl();
                        song.setFileUrl(fileUrl);
                        song.setFavourite(true);
                        songs.add(song);
                        Log.d(TAG, "getSongs: Added song: " + song.getTitle() + ", songId: " + song.getSongId() + ", fileUrl: " + song.getFileUrl());
                    } else {
                        Log.w(TAG, "getSongs: Song data is null for songId: " + lowerCaseSongId);
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
        Log.d(TAG, "updateUI: favouriteSongs size before updating adapter: " + favouriteSongs.size());
        for (Song song : favouriteSongs) {
            Log.d(TAG, "updateUI: Song in favouriteSongs: " + song.getTitle() + ", songId: " + song.getSongId() + ", fileUrl: " + song.getFileUrl());
        }
        songAdapter.updateSongs(favouriteSongs);
    }

    private void handleDatabaseError(DatabaseError databaseError) {
        Log.e(TAG, "Database Error: " + databaseError.getMessage());
        // Consider showing a user-friendly error message here
    }

    @Override
    public void onSongClick(Song song, int position) {
        Log.d(TAG, "onSongClick: Song clicked: " + song.getTitle() + ", songId: " + song.getSongId() + ", fileUrl: " + song.getFileUrl() + ", position: " + position);
        Intent nowPlayingIntent = new Intent(getActivity(), NowPlayingActivity.class);
        nowPlayingIntent.putExtra("song_id", song.getSongId());
        // Truyền danh sách yêu thích hiện tại (favouriteSongs)
        if (favouriteSongs != null && !favouriteSongs.isEmpty()) {
            Log.d(TAG, "onSongClick: передача playlist_songs. Size: " + favouriteSongs.size());
            for (Song favSong : favouriteSongs) {
                Log.d(TAG, "onSongClick: playlist_songs item: " + favSong.getTitle() + ", songId: " + favSong.getSongId() + ", fileUrl: " + favSong.getFileUrl());
            }
            nowPlayingIntent.putExtra("playlist_songs", new ArrayList<>(favouriteSongs));
        }
        startActivity(nowPlayingIntent);

        if (playClickListener != null) {
            playClickListener.onSongPlayClicked(song); // Vẫn gọi callback nếu cần
        } else {
            Log.e(TAG, "playClickListener is null. Activity must implement OnSongPlayClickListener");
        }
    }
}