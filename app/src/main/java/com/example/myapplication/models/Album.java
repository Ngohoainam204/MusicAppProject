package com.example.myapplication.models;

public class Album {
    private String albumId;
    private String albumName;
    private String artist;
    private String coverUrl;
    private java.util.List<String> listOfSongIds; // Sử dụng java.util.List để đảm bảo khớp

    public Album() {
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public java.util.List<String> getListOfSongIds() {
        return listOfSongIds;
    }

    public void setListOfSongIds(java.util.List<String> listOfSongIds) {
        this.listOfSongIds = listOfSongIds;
    }
}