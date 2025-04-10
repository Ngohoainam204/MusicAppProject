package com.example.myapplication.models;

import java.util.List;

public class Playlist {
    private String id;
    private String playlistName;
    private String imageUrl;
    private List<String> listOfSongIds;
    private String description; // Thêm trường description

    public Playlist() {
    }

    public Playlist(String id, String playlistName, String imageUrl, List<String> listOfSongIds, String description) {
        this.id = id;
        this.playlistName = playlistName;
        this.imageUrl = imageUrl;
        this.listOfSongIds = listOfSongIds;
        this.description = description; // Thêm description vào constructor
    }

    public String getPlaylistId() {
        return id;
    }

    public void setPlaylistId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public String getCoverUrl() {
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

    public void setCoverUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setListOfSongIds(List<String> listOfSongIds) {
        this.listOfSongIds = listOfSongIds;
    }

    public String getDescription() { // Getter cho description
        return description;
    }

    public void setDescription(String description) { // Setter cho description
        this.description = description;
    }
}
