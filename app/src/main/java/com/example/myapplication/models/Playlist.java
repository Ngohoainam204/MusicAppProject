package com.example.myapplication.models;

import java.util.List;

public class Playlist {
    private String playlistId;
    private String name;
    private String userId; // owner
    private List<String> songIds; // songId list

    public Playlist() {
    }

    public Playlist(String playlistId, String name, String userId, List<String> songIds) {
        this.playlistId = playlistId;
        this.name = name;
        this.userId = userId;
        this.songIds = songIds;
    }

    // Getters and Setters
    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
