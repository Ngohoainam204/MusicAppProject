package com.example.myapplication.models;

public class Artist {
    private String artistId;
    private String name;
    private String imageUrl;

    public Artist() {
    }

    public Artist(String artistId, String name, String imageUrl) {
        this.artistId = artistId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
