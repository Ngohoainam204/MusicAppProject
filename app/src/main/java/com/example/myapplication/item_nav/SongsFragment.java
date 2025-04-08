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
    private DatabaseReference databaseReference;

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

        databaseReference = FirebaseDatabase.getInstance("https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("music_library");

        loadSongs();

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

    private void loadSongs() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                    DataSnapshot metadataSnapshot = songSnapshot.child("metadata");
                    Song song = metadataSnapshot.getValue(Song.class);
                    if (song != null) {
                        song.setSongId(songSnapshot.getKey());
                        song.setFileUrl(convertDriveUrl(song.getFileUrl()));
                        songList.add(song);
                    }
                }
                filterSongs("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading songs", Toast.LENGTH_SHORT).show();
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