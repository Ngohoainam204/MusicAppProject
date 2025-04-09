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
import com.example.myapplication.adapters.SongAdapter;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FavouriteSongsFragment extends Fragment {
    private static final String TAG = "FavouriteSongsFragment";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouriteSongs;
    private SongAdapter songAdapter;
    private final List<Song> favouriteSongs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_songs, container, false);


        rvFavouriteSongs = view.findViewById(R.id.rvFavouriteSongs);
        rvFavouriteSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(getContext(), favouriteSongs);
        rvFavouriteSongs.setAdapter(songAdapter);

        loadFavouriteSongs();
        return view;
    }

    private void loadFavouriteSongs() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String encodedEmail = email.replace(".", "_");

        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("Favourites").child(encodedEmail);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                if (!favSnapshot.exists()) {
                    Toast.makeText(getContext(), "Bạn chưa có bài hát yêu thích nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference songRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Songs");

                songRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot songSnapshot) {
                        favouriteSongs.clear();
                        for (DataSnapshot songSnap : songSnapshot.getChildren()) {
                            if (favSnapshot.hasChild(songSnap.getKey())) {
                                Song song = songSnap.getValue(Song.class);
                                if (song != null) {
                                    song.setSongId(songSnap.getKey());
                                    song.setFileUrl(convertDriveUrl(song.getFileUrl()));
                                    song.setFavourite(true);
                                    favouriteSongs.add(song);
                                }
                            }
                        }
                        songAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Load songs failed: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Load favourites failed: " + error.getMessage());
            }
        });
    }

    private String convertDriveUrl(String url) {
        if (url == null) return url;
        Pattern pattern = Pattern.compile("[-\\w]{25,}");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? "https://drive.google.com/uc?export=download&id=" + matcher.group() : url;
    }
}
