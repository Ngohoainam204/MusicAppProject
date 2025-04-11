package com.example.myapplication.item_nav;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.AlbumAdapter;
import com.example.myapplication.adapters.ArtistAdapter;
import com.example.myapplication.adapters.BannerAdapter;
import com.example.myapplication.adapters.PlaylistAdapter;
import com.example.myapplication.models.Album;
import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private TabLayout tabIndicator;
    private FirebaseDatabase database;
    private DatabaseReference bannersRef;
    private List<String> bannerUrls;
    private BannerAdapter bannerAdapter;

    private RecyclerView recyclerPlaylistHome; // Đã đổi ID
    private PlaylistAdapter playlistAdapter;
    private List<Playlist> playlistList;
    private DatabaseReference playlistRef;

    private RecyclerView recyclerAlbumHome; // Đã đổi ID
    private AlbumAdapter albumAdapter;
    private List<Album> albumList;
    private DatabaseReference albumsRef;

    private RecyclerView recyclerArtistHome; // Đã đổi ID
    private ArtistAdapter artistAdapter;
    private List<Artist> artistList;
    private DatabaseReference artistsRef;

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
        albumsRef = database.getReference("Albums");
        artistsRef = database.getReference("Artists");
        favouritesRef = database.getReference("FavouritesPlaylist");

        // Banner
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        tabIndicator = view.findViewById(R.id.tabIndicator);
        bannerUrls = new ArrayList<>();
        bannerAdapter = new BannerAdapter(getContext(), bannerUrls);
        viewPagerBanner.setAdapter(bannerAdapter);
        // Thêm logic cho indicator nếu cần (ví dụ: auto scroll, dot indicator)
        loadBanners();

        // Playlist
        recyclerPlaylistHome = view.findViewById(R.id.recycler_playlist_home);
        recyclerPlaylistHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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
        recyclerPlaylistHome.setAdapter(playlistAdapter);

        // Album
        recyclerAlbumHome = view.findViewById(R.id.recycler_album_home);
        recyclerAlbumHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        albumList = new ArrayList<>();
        albumAdapter = new AlbumAdapter(getContext(), albumList);
        recyclerAlbumHome.setAdapter(albumAdapter);
        loadAlbums();

        // Artist
        recyclerArtistHome = view.findViewById(R.id.recycler_artist_home);
        recyclerArtistHome.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        artistList = new ArrayList<>();
        artistAdapter = new ArtistAdapter(getContext(), artistList);
        recyclerArtistHome.setAdapter(artistAdapter);
        loadArtists();

        // Load dữ liệu theo đúng thứ tự
        loadSongsFromFirebase(() -> {
            loadPlaylists();
            loadAlbums();
            loadArtists();
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
                // Cập nhật indicator sau khi load xong banner
                // updateBannerIndicator();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải banners: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải banners.", Toast.LENGTH_SHORT).show();
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
                    String songId = songSnap.getKey();
                    if (song != null && songId != null) {
                        song.setSongId(songId);
                        songMap.put(songId, song);
                    }
                }
                Log.d(TAG, "✅ SongMap loaded. Keys: " + songMap.keySet());
                playlistAdapter.setSongMap(songMap); // Cập nhật songMap cho playlistAdapter
                if (onLoaded != null) onLoaded.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải bài hát: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải bài hát.", Toast.LENGTH_SHORT).show();
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
                        // Lấy ảnh bìa của bài hát đầu tiên nếu có
                        if (playlist.getListOfSongIds() != null && !playlist.getListOfSongIds().isEmpty()) {
                            String firstSongId = playlist.getListOfSongIds().get(0);
                            if (songMap.containsKey(firstSongId) && songMap.get(firstSongId).getCoverUrl() != null) {
                                playlist.setImageUrl(songMap.get(firstSongId).getCoverUrl());
                            }
                        }
                        playlistList.add(playlist);
                    }
                }
                playlistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải playlists: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải playlists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAlbums() {
        albumsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                albumList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Album album = item.getValue(Album.class);
                    if (album != null) {
                        albumList.add(album);
                    }
                }
                albumAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải albums: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải albums.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadArtists() {
        artistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                artistList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    Artist artist = item.getValue(Artist.class);
                    if (artist != null) {
                        artistList.add(artist);
                    }
                }
                artistAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải artists: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải artists.", Toast.LENGTH_SHORT).show();
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
                Log.e(TAG, "Lỗi tải favourites playlist: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải favourites playlist.", Toast.LENGTH_SHORT).show();
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