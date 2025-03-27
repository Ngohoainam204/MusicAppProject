package com.example.myapplication.item_nav;

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
    private List<Song> songList;
    private SongAdapter songAdapter;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        Log.d(TAG, "onCreateView: Bắt đầu khởi tạo Fragment");

        recyclerView = view.findViewById(R.id.recycler_view_songs);
        if (recyclerView == null) {
            Log.e(TAG, "onCreateView: RecyclerView không tìm thấy!");
            Toast.makeText(getContext(), "RecyclerView không tìm thấy!", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerView.setAdapter(songAdapter);

        // Lấy dữ liệu từ Firebase
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
                songAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải danh sách bài hát: " + error.getMessage());
            }
        });


        return view;
    }
}
