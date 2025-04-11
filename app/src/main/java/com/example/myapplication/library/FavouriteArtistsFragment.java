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
import com.example.myapplication.adapters.ArtistAdapter;
import com.example.myapplication.models.Artist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavouriteArtistsFragment extends Fragment {
    private static final String TAG = "FavouriteArtistsFragment";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView rvFavouriteArtists;
    private ArtistAdapter artistAdapter;
    private final List<Artist> favouriteArtists = new ArrayList<>();

    public FavouriteArtistsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite_artist, container, false);

        rvFavouriteArtists = view.findViewById(R.id.rvFavouriteArtists);
        rvFavouriteArtists.setLayoutManager(new LinearLayoutManager(getContext()));

        artistAdapter = new ArtistAdapter(getContext(), favouriteArtists);
        rvFavouriteArtists.setAdapter(artistAdapter);

        loadFavouriteArtists();

        return view;
    }

    private void loadFavouriteArtists() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) {
            Log.d(TAG, "loadFavouriteArtists: User email is null");
            return;
        }
        String encodedEmail = email.replace(".", "_");

        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("FavouritesArtists").child(encodedEmail);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                if (!favSnapshot.exists()) {
                    Toast.makeText(getContext(), "Bạn chưa có nghệ sĩ yêu thích nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 👉 Lấy danh sách artistId yêu thích
                List<String> favArtistIds = new ArrayList<>();
                for (DataSnapshot favArtistSnap : favSnapshot.getChildren()) {
                    favArtistIds.add(favArtistSnap.getKey());
                    Log.d(TAG, "Favourite artistId = " + favArtistSnap.getKey());
                }

                // 👉 Truy vấn dữ liệu từ Artists (là List nên duyệt toàn bộ)
                DatabaseReference artistRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                        .getReference("Artists");

                artistRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot artistSnapshot) {
                        favouriteArtists.clear();

                        for (DataSnapshot artistSnap : artistSnapshot.getChildren()) {
                            Artist artist = artistSnap.getValue(Artist.class);
                            if (artist != null && artist.getArtistId() != null &&
                                    favArtistIds.contains(artist.getArtistId())) {

                                favouriteArtists.add(artist);
                                Log.d(TAG, "Added favourite artist: " + artist.getArtistName());
                            }
                        }

                        if (favouriteArtists.isEmpty()) {
                            Toast.makeText(getContext(), "Không tìm thấy dữ liệu nghệ sĩ yêu thích", Toast.LENGTH_SHORT).show();
                        }

                        artistAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Load artists failed: " + error.getMessage());
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
