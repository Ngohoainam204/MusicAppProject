package com.example.myapplication.item_nav;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SearchAdapter;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Search_Fragment extends Fragment {
    private RecyclerView rvSearch;
    private EditText edtSearch;
    private SearchAdapter searchAdapter;

    private final List<Song> allSongs = new ArrayList<>();
    private final List<Playlist> allPlaylists = new ArrayList<>();

    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rvSearch = view.findViewById(R.id.recycler_view_search);
        edtSearch = view.findViewById(R.id.et_search);
        searchAdapter = new SearchAdapter(getContext());
        rvSearch.setAdapter(searchAdapter);
        rvSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        loadAllData();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void loadAllData() {
        DatabaseReference songRef = FirebaseDatabase.getInstance(DB_URL).getReference("Songs");
        DatabaseReference playlistRef = FirebaseDatabase.getInstance(DB_URL).getReference("Playlists");

        songRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song song = snap.getValue(Song.class);
                    if (song != null) {
                        song.setSongId(snap.getKey());
                        allSongs.add(song);
                    }
                }
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        playlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaylists.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Playlist playlist = snap.getValue(Playlist.class);
                    if (playlist != null) {
                        playlist.setPlaylistId(snap.getKey());
                        allPlaylists.add(playlist);
                    }
                }
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkLoadComplete() {
        if (!allSongs.isEmpty() || !allPlaylists.isEmpty()) {
            searchAdapter.updateData(allSongs, allPlaylists);
        }
    }
}
