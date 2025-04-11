package com.example.myapplication.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {
    private String songId;
    private String title;
    private String artist;
    private String fileUrl;
    private String coverUrl;
    private String duration;
    private String lyrics;
    private boolean isFavourite;

    public Song() {
    }

    public Song(String songId, String title, String artist, String fileUrl, String coverUrl, String duration, String lyrics) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.fileUrl = fileUrl;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.lyrics = lyrics;
    }

    protected Song(Parcel in) {
        songId = in.readString();
        title = in.readString();
        artist = in.readString();
        fileUrl = in.readString();
        coverUrl = in.readString();
        duration = in.readString();
        lyrics = in.readString();
        isFavourite = in.readByte() != 0;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songId);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(fileUrl);
        dest.writeString(coverUrl);
        dest.writeString(duration);
        dest.writeString(lyrics);
        dest.writeByte((byte) (isFavourite ? 1 : 0));
    }
}