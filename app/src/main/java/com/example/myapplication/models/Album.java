package com.example.myapplication.models;

import java.util.List;

public class Album {
    private String albumId;
    private String name;
    private String artistId;
    private String coverUrl;
    private List<String> songIds;

    public Album() {
    }

    public Album(String albumId, String name, String artistId, String coverUrl, List<String> songIds) {
        this.albumId = albumId;
        this.name = name;
        this.artistId = artistId;
        this.coverUrl = coverUrl;
        this.songIds = songIds;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
