package com.example.myapplication.models;

import java.util.List;

public class Artist {
    private String artistId;
    private String artistName; // Đã đổi 'name' thành 'artistName'
    private String avatarUrl; // Đã thêm
    private String bio;       // Đã thêm
    private List<String> listOfSongIds; // Đã thêm

    public Artist() {
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getListOfSongIds() {
        return listOfSongIds;
    }

    public void setListOfSongIds(List<String> listOfSongIds) {
        this.listOfSongIds = listOfSongIds;
    }
}