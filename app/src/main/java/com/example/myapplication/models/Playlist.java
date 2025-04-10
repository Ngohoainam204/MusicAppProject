package com.example.myapplication.models;

import java.util.List;

public class Playlist {
    private String id;
    private String playlistName; // ⚠️ quan trọng!
    private String imageUrl;
    private List<String> listOfSongIds;

    public Playlist() {
    }

    public Playlist(String id, String playlistName, String imageUrl, List<String> listOfSongIds) {
        this.id = id;
        this.playlistName = playlistName;
        this.imageUrl = imageUrl;
        this.listOfSongIds = listOfSongIds;
    }

    public void setPlaylistId(String id) {
        this.id = id;
    }

    public void setFavourite(boolean fav) {
        // Optional: nếu bạn có biến isFavourite
    }


    public String getId() {
        return id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getListOfSongIds() {
        return listOfSongIds;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setListOfSongIds(List<String> listOfSongIds) {
        this.listOfSongIds = listOfSongIds;
    }
}
