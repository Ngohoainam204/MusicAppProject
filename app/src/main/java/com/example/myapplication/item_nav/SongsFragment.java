package com.example.myapplication.item_nav;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongsFragment extends Fragment {
    private static final String TAG = "SongsFragment";
    private RecyclerView recyclerView;
    private EditText etSearch;
    private List<Song> songList, filteredList;
    private SongAdapter songAdapter;
    private DatabaseReference songsRef, favRef;

    // Firebase URL đúng region
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_songs);
        etSearch = view.findViewById(R.id.et_search);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = new ArrayList<>();
        filteredList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), filteredList);
        recyclerView.setAdapter(songAdapter);

        // Sử dụng đúng URL region
        songsRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("Songs");

        loadSongsWithFavourites();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void loadSongsWithFavourites() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail == null) return;
        String encodedEmail = userEmail.replace(".", "_");

        favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("Favourites").child(encodedEmail);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot songSnapshot) {
                        songList.clear();
                        for (DataSnapshot item : songSnapshot.getChildren()) {
                            Song song = item.getValue(Song.class);
                            if (song != null) {
                                song.setSongId(item.getKey());
                                song.setFileUrl(convertDriveUrl(song.getFileUrl()));
                                if (favSnapshot.hasChild(song.getSongId())) {
                                    song.setFavourite(true);
                                }
                                songList.add(song);
                            }
                        }
                        filterSongs(etSearch.getText().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi tải bài hát", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Firebase song load error: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi tải yêu thích", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Favourite load error: " + error.getMessage());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterSongs(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(songList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Song song : songList) {
                if (song.getTitle().toLowerCase().contains(lowerQuery) ||
                        song.getArtist().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(song);
                }
            }
        }
        songAdapter.notifyDataSetChanged();
    }

    private String convertDriveUrl(String url) {
        if (url == null) return url;
        Pattern pattern = Pattern.compile("[-\\w]{25,}");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? "https://drive.google.com/uc?export=download&id=" + matcher.group() : url;
    }
}
