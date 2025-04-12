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
import com.example.myapplication.adapters.AlbumAdapter; // Import AlbumAdapter
import com.example.myapplication.models.Album; // Import Album
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavouriteAlbumsFragment extends Fragment {
    private static final String TAG = "FavouriteAlbumsFragment";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouriteAlbums;
    private AlbumAdapter albumAdapter; // Sử dụng AlbumAdapter
    private final List<Album> favouriteAlbums = new ArrayList<>();

    public FavouriteAlbumsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_album, container, false); // Tạo layout fragment_favourite_album

        rvFavouriteAlbums = view.findViewById(R.id.rvFavouriteAlbums); // Sử dụng ID mới
        rvFavouriteAlbums.setLayoutManager(new LinearLayoutManager(getContext()));

        albumAdapter = new AlbumAdapter(getContext(), favouriteAlbums); // Khởi tạo AlbumAdapter
        rvFavouriteAlbums.setAdapter(albumAdapter);

        loadFavouriteAlbums();

        return view;
    }

    private void loadFavouriteAlbums() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) {
            Log.d(TAG, "loadFavouriteAlbums: User email is null");
            return;
        }
        String encodedEmail = email.replace(".", "_");

        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("FavouritesAlbums").child(encodedEmail);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                if (!favSnapshot.exists()) {
                    Toast.makeText(getContext(), "Bạn chưa có album yêu thích nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                //  Lấy danh sách albumId yêu thích
                List<String> favAlbumIds = new ArrayList<>();
                for (DataSnapshot favAlbumSnap : favSnapshot.getChildren()) {
                    favAlbumIds.add(favAlbumSnap.getKey());
                    Log.d(TAG, "Favourite albumId = " + favAlbumSnap.getKey());
                }

                //  Truy vấn dữ liệu từ Albums (là List nên duyệt toàn bộ)
                DatabaseReference albumRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                        .getReference("Albums");

                albumRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot albumSnapshot) {
                        favouriteAlbums.clear();

                        for (DataSnapshot albumSnap : albumSnapshot.getChildren()) {
                            Album album = albumSnap.getValue(Album.class);
                            if (album != null && album.getAlbumId() != null &&
                                    favAlbumIds.contains(album.getAlbumId())) {
                                favouriteAlbums.add(album);
                                Log.d(TAG, "Added favourite album: " + album.getAlbumName());
                            }
                        }

                        if (favouriteAlbums.isEmpty()) {
                            Toast.makeText(getContext(), "Không tìm thấy dữ liệu album yêu thích", Toast.LENGTH_SHORT).show();
                        }
                        albumAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Load albums failed: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Load favourites failed: " + error.getMessage());
            }
        });
    }
}
