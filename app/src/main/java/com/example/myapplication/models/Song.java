package com.example.myapplication.models;

public class Song {
    private String id;       // ID bài hát (có thể không cần nếu không có trong Firebase)
    private String title;    // Tên bài hát
    private String artist;   // Nghệ sĩ
    private String fileUrl;  // URL nhạc (trùng với Firebase)
    private String coverUrl; // Ảnh bài hát (trùng với Firebase)
    private String duration; // Thời lượng bài hát

    // Constructor mặc định (bắt buộc cho Firebase)
    public Song() {}

    // Constructor đầy đủ
    public Song(String id, String title, String artist, String fileUrl, String coverUrl, String duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.fileUrl = fileUrl;
        this.coverUrl = coverUrl;
        this.duration = duration;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}
