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

                                // Chuyển fileUrl sang link Cloudinary hợp lệ
                                song.setFileUrl(getStreamableUrl(song.getFileUrl(), song.getTitle()));

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

    // ✅ Hàm chuyển link stream Cloudinary đúng định dạng public
    private String getStreamableUrl(String originalUrl, String title) {
        if (originalUrl != null && originalUrl.contains("res.cloudinary.com") && originalUrl.endsWith(".mp3")) {
            return originalUrl;
        }

        if (title != null && !title.isEmpty()) {
            String fileName = normalizeTitleToFilename(title);
            return "https://res.cloudinary.com/dvkypaemi/raw/upload/SongList/" + fileName;
        }

        return originalUrl;
    }

    // ✅ Chuyển tiêu đề thành tên file chuẩn không dấu
    private String normalizeTitleToFilename(String title) {
        String fileName = title.toLowerCase()
                .replace("đ", "d")
                .replaceAll("[^a-z0-9\\s]", "") // xóa ký tự đặc biệt
                .trim()
                .replaceAll("\\s+", "_");      // thay khoảng trắng bằng _
        return fileName + ".mp3";
    }
}
