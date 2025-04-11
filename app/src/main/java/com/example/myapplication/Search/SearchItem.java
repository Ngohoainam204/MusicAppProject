package com.example.myapplication.Search;

import com.example.myapplication.models.Album;
import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;

public abstract class SearchItem {
    public static final int TYPE_SONG = 0;
    public static final int TYPE_PLAYLIST = 1;
    public static final int TYPE_ALBUM = 2;
    public static final int TYPE_ARTIST = 3;

    public abstract int getType();

    public abstract boolean matches(String query);

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

        @Override
        public boolean matches(String query) {
            if (song == null || query == null) return false;
            String lowerQuery = query.toLowerCase();
            return (song.getTitle() != null && song.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (song.getArtist() != null && song.getArtist().toLowerCase().contains(lowerQuery));
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

        @Override
        public boolean matches(String query) {
            if (playlist == null || query == null) return false;
            String lowerQuery = query.toLowerCase();
            return playlist.getPlaylistName() != null && playlist.getPlaylistName().toLowerCase().contains(lowerQuery);
        }
    }

    // ---------------------------
    // Album Item
    // ---------------------------
    public static class AlbumItem extends SearchItem {
        private final Album album;

        public AlbumItem(Album album) {
            this.album = album;
        }

        public Album getAlbum() {
            return album;
        }

        @Override
        public int getType() {
            return TYPE_ALBUM;
        }

        @Override
        public boolean matches(String query) {
            if (album == null || query == null) return false;
            String lowerQuery = query.toLowerCase();
            return album.getAlbumName() != null && album.getAlbumName().toLowerCase().contains(lowerQuery);
        }
    }

    // ---------------------------
    // Artist Item
    // ---------------------------
    public static class ArtistItem extends SearchItem {
        private final Artist artist;

        public ArtistItem(Artist artist) {
            this.artist = artist;
        }

        public Artist getArtist() {
            return artist;
        }

        @Override
        public int getType() {
            return TYPE_ARTIST;
        }

        @Override
        public boolean matches(String query) {
            if (artist == null || query == null) return false;
            String lowerQuery = query.toLowerCase();
            return artist.getArtistName() != null && artist.getArtistName().toLowerCase().contains(lowerQuery);
        }
    }
}
