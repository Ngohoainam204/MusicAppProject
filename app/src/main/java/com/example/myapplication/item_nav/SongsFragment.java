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

        Log.d(TAG, "onCreateView: Bắt đầu khởi tạo Fragment");

        recyclerView = view.findViewById(R.id.recycler_view_songs);
        etSearch = view.findViewById(R.id.et_search);

        if (recyclerView == null) {
            Log.e(TAG, "onCreateView: RecyclerView không tìm thấy!");
            Toast.makeText(getContext(), "RecyclerView không tìm thấy!", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = new ArrayList<>();
        filteredList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), filteredList);
        recyclerView.setAdapter(songAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("songs");

        Log.d(TAG, "onCreateView: Đọc dữ liệu từ Firebase");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Song song = dataSnapshot.getValue(Song.class);
                    if (song != null) {
                        songList.add(song);
                    }
                }
                filterSongs("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải danh sách bài hát: " + error.getMessage());
            }
        });

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

    @SuppressLint("NotifyDataSetChanged")
    private void filterSongs(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(songList);
        } else {
            for (Song song : songList) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(song);
                }
            }
        }
        songAdapter.notifyDataSetChanged();
    }
}
