package com.example.myapplication.item_nav;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.myapplication.detail.AlbumDetailActivity;
import com.example.myapplication.detail.ArtistDetailActivity;
import com.example.myapplication.detail.PlaylistDetailActivity;
import com.example.myapplication.models.Album;
import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.example.myapplication.nowplaying.NowPlayingActivity;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Search_Fragment extends Fragment {

    private RecyclerView rvSearch;
    private EditText edtSearch;
    private SearchAdapter searchAdapter;

    private final List<Song> allSongs = new ArrayList<>();
    private final List<Playlist> allPlaylists = new ArrayList<>();
    private final List<Album> allAlbums = new ArrayList<>();
    private final List<Artist> allArtists = new ArrayList<>();

    private boolean songsLoaded = false;
    private boolean playlistsLoaded = false;
    private boolean albumsLoaded = false;
    private boolean artistsLoaded = false;

    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String TAG = "SearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rvSearch = view.findViewById(R.id.recycler_view_search);
        edtSearch = view.findViewById(R.id.et_search);

        searchAdapter = new SearchAdapter(getContext());
        rvSearch.setAdapter(searchAdapter);
        rvSearch.setLayoutManager(new LinearLayoutManager(getContext()));

        searchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onSongClick(Song song) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), NowPlayingActivity.class);
                    intent.putExtra("song_id", song.getSongId());
                    intent.putExtra("playlist_songs", new ArrayList<>(allSongs)); // Truyền danh sách
                    startActivity(intent);
                }
            }

            @Override
            public void onPlaylistClick(Playlist playlist) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), PlaylistDetailActivity.class);
                    intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_ID, playlist.getPlaylistId());  // Chỉnh sửa để sử dụng EXTRA_PLAYLIST_ID
                    Log.d("SearchAdapter", "Click playlist: " + playlist.getPlaylistName());
                    Log.d("SearchAdapter", "Playlist ID: " + playlist.getPlaylistId());

                    startActivity(intent);
                }
            }


            @Override
            public void onAlbumClick(Album album) {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
                    intent.putExtra("album_id", album.getAlbumId());
                    startActivity(intent);
                }
            }

            @Override
            public void onArtistClick(Artist artist) {
                Log.d(TAG, "onArtistClick called for artist: " + artist.getArtistName() + " - ID: " + artist.getArtistId());
                if (getContext() != null) {
                    Log.d(TAG, "Context is NOT null");
                    Intent intent = new Intent(getContext(), ArtistDetailActivity.class);
                    intent.putExtra("artistId", artist.getArtistId());
                    Log.d(TAG, "Starting ArtistDetailActivity with ID: " + artist.getArtistId());
                    startActivity(intent);
                    Log.d(TAG, "startActivity() called");
                } else {
                    Log.e(TAG, "Context is null, cannot start ArtistDetailActivity");
                }
            }
        });

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

        loadAllData();
        return view;
    }

    private void loadAllData() {
        DatabaseReference db = FirebaseDatabase.getInstance(DB_URL).getReference();

        // Load Songs
        db.child("Songs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongs.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Song song = snap.getValue(Song.class);
                    if (song != null && song.getSongId() != null) {
                        allSongs.add(song);
                    }
                }
                songsLoaded = true;
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                songsLoaded = true;
                checkLoadComplete();
            }
        });

        // Load Playlists
        db.child("Playlists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allPlaylists.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Playlist playlist = snap.getValue(Playlist.class);
                    if (playlist != null && playlist.getPlaylistId() != null) {
                        allPlaylists.add(playlist);
                    } else {
                        Log.w(TAG, "Playlist missing ID or is null: " + snap.toString());
                    }
                }
                playlistsLoaded = true;
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                playlistsLoaded = true;
                checkLoadComplete();
            }
        });

        // Load Albums
        db.child("Albums").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAlbums.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Album album = snap.getValue(Album.class);
                    if (album != null && album.getAlbumId() != null) {
                        allAlbums.add(album);
                    }
                }
                albumsLoaded = true;
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                albumsLoaded = true;
                checkLoadComplete();
            }
        });

        // Load Artists
        db.child("Artists").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allArtists.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Artist artist = snap.getValue(Artist.class);
                    if (artist != null && artist.getArtistId() != null) {
                        allArtists.add(artist);
                    }
                }
                artistsLoaded = true;
                checkLoadComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                artistsLoaded = true;
                checkLoadComplete();
            }
        });
    }

    private void checkLoadComplete() {
        if (songsLoaded && playlistsLoaded && albumsLoaded && artistsLoaded) {
            searchAdapter.updateData(allSongs, allPlaylists, allAlbums, allArtists);
        }
    }
}
