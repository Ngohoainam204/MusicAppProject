package com.example.myapplication.Search;

import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;

public abstract class SearchItem {
    public static final int TYPE_SONG = 0;
    public static final int TYPE_PLAYLIST = 1;

    public abstract int getType();

    // ---------------------------
    // Song Item
    // ---------------------------
    public static class SongItem extends SearchItem {
        private final Song song;

        public SongItem(Song song) {
            this.song = song;
        }

        public Song getSong() {
            return song;
        }

        @Override
        public int getType() {
            return TYPE_SONG;
        }
    }

    // ---------------------------
    // Playlist Item
    // ---------------------------
    public static class PlaylistItem extends SearchItem {
        private final Playlist playlist;

        public PlaylistItem(Playlist playlist) {
            this.playlist = playlist;
        }

        public Playlist getPlaylist() {
            return playlist;
        }

        @Override
        public int getType() {
            return TYPE_PLAYLIST;
        }
    }
}
