package com.example.myapplication.models;

import java.util.List;

public class User {
    private String userId;
    private String username;
    private String email;
    private List<String> favouriteSongs; // List of songId
    private List<String> playlists; // List of playlistId

    public User() {
    }

    public User(String userId, String username, String email, List<String> favouriteSongs, List<String> playlists) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.favouriteSongs = favouriteSongs;
        this.playlists = playlists;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getFavouriteSongs() {
        return favouriteSongs;
    }

    public void setFavouriteSongs(List<String> favouriteSongs) {
        this.favouriteSongs = favouriteSongs;
    }

    public List<String> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<String> playlists) {
        this.playlists = playlists;
    }
}
