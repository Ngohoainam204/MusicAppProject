package com.example.myapplication.models;

public class FavouriteSong {
    private String userId;
    private String songId;

    public FavouriteSong() {
    }

    public FavouriteSong(String userId, String songId) {
        this.userId = userId;
        this.songId = songId;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }
}
