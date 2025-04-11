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
        Log.d(TAG, "loadFavouriteArtists: encodedEmail=" + encodedEmail);

        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("FavouritesArtists").child(encodedEmail);
        Log.d(TAG, "loadFavouriteArtists: favRef=" + favRef.toString());
        Log.d("loadFavouriteArtists", "encodedEmail=" + encodedEmail);
        Log.d("loadFavouriteArtists", "favRef=" + favRef.toString());

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                Log.d(TAG, "loadFavouriteArtists: favSnapshot.exists()=" + favSnapshot.exists());
                if (!favSnapshot.exists()) {
                    Toast.makeText(getContext(), "Bạn chưa có nghệ sĩ yêu thích nào", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference artistRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                        .getReference("Artists");
                Log.d(TAG, "loadFavouriteArtists: artistRef=" + artistRef.toString());

                artistRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot artistSnapshot) {
                        favouriteArtists.clear();
                        Log.d(TAG, "loadFavouriteArtists: artistSnapshot.getChildrenCount()=" + artistSnapshot.getChildrenCount());
                        for (DataSnapshot artistSnap : artistSnapshot.getChildren()) {
                            Log.d(TAG, "loadFavouriteArtists: Checking artist with key=" + artistSnap.getKey());
                            if (favSnapshot.hasChild(artistSnap.getKey())) {
                                Artist artist = artistSnap.getValue(Artist.class);
                                if (artist != null) {
                                    artist.setArtistId(artistSnap.getKey());
                                    favouriteArtists.add(artist);
                                    Log.d(TAG, "loadFavouriteArtists: Added favourite artist=" + artist.getArtistName() + " with ID=" + artist.getArtistId());
                                } else {
                                    Log.w(TAG, "loadFavouriteArtists: Artist is null for key=" + artistSnap.getKey());
                                }
                            } else {
                                Log.d(TAG, "loadFavouriteArtists: Artist with key=" + artistSnap.getKey() + " is not in favourites for user=" + encodedEmail);
                            }
                        }
                        Log.d(TAG, "loadFavouriteArtists: favouriteArtists.size()=" + favouriteArtists.size());
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