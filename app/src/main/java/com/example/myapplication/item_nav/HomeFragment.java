package com.example.myapplication.item_nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.BannerAdapter;
import com.example.myapplication.adapters.PlaylistAdapter;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private FirebaseDatabase database;
    private DatabaseReference bannersRef;
    private List<String> bannerUrls;
    private BannerAdapter bannerAdapter;

    private RecyclerView recyclerPlaylist;
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> playlistList;
    private DatabaseReference playlistRef;

    private HashMap<String, Song> songMap;
    private HashSet<String> favouritePlaylistIds;
    private DatabaseReference favouritesRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase
        database = FirebaseDatabase.getInstance("https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app");
        bannersRef = database.getReference("Banners");
        playlistRef = database.getReference("Playlists");
        favouritesRef = database.getReference("FavouritesPlaylist");

        // Banner
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        bannerUrls = new ArrayList<>();
        bannerAdapter = new BannerAdapter(getContext(), bannerUrls);
        viewPagerBanner.setAdapter(bannerAdapter);
        loadBanners();

        // Playlist
        recyclerPlaylist = view.findViewById(R.id.recycler_playlist_home);
        recyclerPlaylist.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        playlistList = new ArrayList<>();
        songMap = new HashMap<>();
        favouritePlaylistIds = new HashSet<>();

        playlistAdapter = new PlaylistAdapter(
                getContext(),
                playlistList,
                favouritePlaylistIds,
                songMap,
                this::toggleFavouritePlaylist
        );
        recyclerPlaylist.setAdapter(playlistAdapter);

        // Load dữ liệu đúng thứ tự
        loadSongsFromFirebase(() -> {
            loadPlaylists();
            loadFavouritePlaylists();
        });

        return view;
    }

    private void loadBanners() {
        bannersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerUrls.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    String url = item.child("coverUrl").getValue(String.class);
                    if (url != null) {
                        bannerUrls.add(url);
                    }
                }
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadSongsFromFirebase(Runnable onLoaded) {
        DatabaseReference songRef = database.getReference("Songs");
        songRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songMap.clear();
                for (DataSnapshot songSnap : snapshot.getChildren()) {
                    Song song = songSnap.getValue(Song.class);
                    String songId = songSnap.getKey(); // LẤY ID TỪ KEY NODE
                    if (song != null && songId != null) {
                        song.setSongId(songId); // Gán thủ công
                        songMap.put(songId, song);
                    }
                }


                System.out.println("✅ SongMap loaded: " + songMap.keySet());

                // 🛠 FIX QUAN TRỌNG: cập nhật adapter sau khi có songMap
                playlistAdapter.notifyDataSetChanged();

                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void loadPlaylists() {
        playlistRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playlistList.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    Playlist playlist = item.getValue(Playlist.class);

                    if (playlist != null) {
                        playlist.setId(item.getKey());

                        // Ép listOfSongIds nếu bị null
                        if (playlist.getListOfSongIds() == null || playlist.getListOfSongIds().isEmpty()) {
                            List<String> songIds = new ArrayList<>();
                            for (DataSnapshot songIdSnap : item.child("listOfSongIds").getChildren()) {
                                String songId = songIdSnap.getValue(String.class);
                                if (songId != null) {
                                    songIds.add(songId);
                                }
                            }
                            playlist.setListOfSongIds(songIds);
                        }

                        playlistList.add(playlist);
                    }
                }

                playlistAdapter.notifyDataSetChanged();

                for (Playlist p : playlistList) {
                    System.out.println("🎧 Playlist: " + p.getPlaylistName() + " | Songs: " +
                            (p.getListOfSongIds() != null ? p.getListOfSongIds().size() : 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Firebase error: " + error.getMessage());
            }
        });
    }

    private void loadFavouritePlaylists() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String email = auth.getCurrentUser().getEmail();
        String encodedEmail = email.replace(".", "_");

        favouritesRef.child(encodedEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favouritePlaylistIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String playlistId = snap.getKey();
                    if (playlistId != null) {
                        favouritePlaylistIds.add(playlistId);
                    }
                }
                playlistAdapter.setFavouritePlaylistIds(favouritePlaylistIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void toggleFavouritePlaylist(Playlist playlist) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String email = auth.getCurrentUser().getEmail();
        String encodedEmail = email.replace(".", "_");

        String playlistId = playlist.getId();
        if (favouritePlaylistIds.contains(playlistId)) {
            favouritesRef.child(encodedEmail).child(playlistId).removeValue();
        } else {
            HashMap<String, Object> fav = new HashMap<>();
            fav.put("playlistId", playlistId);
            fav.put("addedDate", System.currentTimeMillis());
            favouritesRef.child(encodedEmail).child(playlistId).setValue(fav);
        }
    }
}
