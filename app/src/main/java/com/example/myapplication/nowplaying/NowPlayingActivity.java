package com.example.myapplication.nowplaying;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Song; // Đảm bảo import đúng model Song của bạn
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {
    private static final String TAG = "NowPlayingActivity";
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";
    TextView tvSongTitle;
    TextView tvArtist;
    TextView tvCurrentTime;
    TextView tvDuration;
    ImageView imgCover;
    private ImageView btnPlayPause;
    private ImageView btnBack;
    private ImageView btnNext;
    private ImageView btnPrevious;
    private ImageView btnLyric;
    private SeekBar seekBar;
    ExoPlayer exoPlayer;
    final Handler handler = new Handler();
    boolean isPlaying = false;
    boolean isSeeking = false;
    String currentSongId;
    String SongLyrics;

    // Variables for song list management
    private List<Song> songList = new ArrayList<>();
    private int currentSongIndex = -1;
    private DatabaseReference songsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtistName);
        imgCover = findViewById(R.id.imgCover);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        btnLyric = findViewById(R.id.btnLyric);

        songsRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Songs");

        // Initialize song list (YOU MUST POPULATE THIS WITH YOUR ACTUAL DATA)
        initializeSongList();
        Log.d(TAG, "onCreate - Kích thước songList ban đầu: " + songList.size());
        for (Song song : songList) {
            Log.d(TAG, "onCreate - Bài hát trong danh sách ban đầu: " + song.toString());
        }

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("song_title");
            String artist = intent.getStringExtra("artist_name");
            String coverUrl = intent.getStringExtra("cover_url");
            String songUrl = intent.getStringExtra("song_url");
            currentSongId = intent.getStringExtra("song_id");
            SongLyrics = intent.getStringExtra("song_lyrics");

            Log.d(TAG, "onCreate - Intent - Song ID: " + currentSongId);

            // Find the current song index in the list
            // Wait for the songList to be populated before finding the index
            if (!songList.isEmpty() && currentSongId != null) {
                currentSongIndex = findSongIndexById(currentSongId);
                Log.d(TAG, "onCreate - Chỉ số ban đầu sau khi tìm: " + currentSongIndex);
                loadCurrentSongData();
            } else if (songList.isEmpty()) {
                Log.w(TAG, "onCreate - songList đang trống.");
            } else if (currentSongId == null) {
                Log.w(TAG, "onCreate - currentSongId từ Intent là null.");
                if (!songList.isEmpty()) {
                    playSongAtIndex(0); // Phát bài hát đầu tiên nếu không có ID
                }
            }
        }

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnBack.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnLyric.setOnClickListener(v -> {
            if (currentSongId == null || currentSongId.isEmpty()) {
                Toast.makeText(NowPlayingActivity.this, "Song ID không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            openLyricsFragment();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && exoPlayer != null) {
                    isSeeking = true;
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (exoPlayer != null) {
                    int progress = seekBar.getProgress();
                    exoPlayer.seekTo(progress);
                    isSeeking = false;
                }
            }
        });
    }

    private void initializeSongList() {
        songList.clear();
        songsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot item : snapshot.getChildren()) {
                    Song song = item.getValue(Song.class);
                    if (song != null) {
                        song.setSongId(item.getKey());
                        songList.add(song);
                        Log.d(TAG, "initializeSongList - Đã tải bài hát: " + song.getTitle() + ", ID: " + song.getSongId());
                    }
                }
                Log.d(TAG, "initializeSongList - Tổng số bài hát đã tải: " + songList.size());

                // Sau khi tải xong, nếu có currentSongId, tìm và phát
                if (currentSongId != null && !currentSongId.isEmpty()) {
                    currentSongIndex = findSongIndexById(currentSongId);
                    Log.d(TAG, "initializeSongList - Chỉ số bài hát hiện tại sau tải: " + currentSongIndex);
                    loadCurrentSongData();
                } else if (!songList.isEmpty() && currentSongIndex == -1) {
                    playSongAtIndex(0); // Phát bài hát đầu tiên nếu không có ID ban đầu
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "initializeSongList - Lỗi tải danh sách bài hát: " + error.getMessage());
                Toast.makeText(NowPlayingActivity.this, "Lỗi tải danh sách bài hát.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCurrentSongData() {
        if (currentSongIndex != -1 && currentSongIndex < songList.size()) {
            Song currentSong = songList.get(currentSongIndex);
            tvSongTitle.setText(currentSong.getTitle());
            tvArtist.setText(currentSong.getArtist());
            Glide.with(this).load(currentSong.getCoverUrl()).into(imgCover);
            initializePlayer(currentSong.getFileUrl());
            SongLyrics = currentSong.getLyrics();
            Log.d(TAG, "loadCurrentSongData - Đã tải dữ liệu cho bài hát: " + currentSong.getTitle());
        } else {
            Log.w(TAG, "loadCurrentSongData - currentSongIndex không hợp lệ hoặc songList trống.");
            if (!songList.isEmpty()) {
                playSongAtIndex(0); // Fallback to the first song if current is invalid
            }
        }
    }

    private int findSongIndexById(String songId) {
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getSongId().equals(songId)) { // Sử dụng getSongId()
                return i;
            }
        }
        return -1;
    }

    private void playNextSong() {
        if (songList.isEmpty() || currentSongIndex == -1) {
            Log.w(TAG, "playNextSong - Không thể next: danh sách bài hát trống hoặc chỉ số hiện tại không hợp lệ.");
            return;
        }

        int nextIndex = currentSongIndex + 1;
        if (nextIndex >= songList.size()) {
            nextIndex = 0; // Lặp lại bài hát đầu tiên
        }

        Log.d(TAG, "playNextSong - Chỉ số trước khi next: " + currentSongIndex);
        Log.d(TAG, "playNextSong - Chỉ số sau khi next: " + nextIndex);
        playSongAtIndex(nextIndex);
    }

    private void playPreviousSong() {
        if (songList.isEmpty() || currentSongIndex == -1) {
            Log.w(TAG, "playPreviousSong - Không thể previous: danh sách bài hát trống hoặc chỉ số hiện tại không hợp lệ.");
            return;
        }

        int prevIndex = currentSongIndex - 1;
        if (prevIndex < 0) {
            prevIndex = songList.size() - 1; // Lặp lại bài hát cuối cùng
        }

        Log.d(TAG, "playPreviousSong - Chỉ số trước khi previous: " + currentSongIndex);
        Log.d(TAG, "playPreviousSong - Chỉ số sau khi previous: " + prevIndex);
        playSongAtIndex(prevIndex);
    }

    private void playSongAtIndex(int index) {
        if (index < 0 || index >= songList.size()) {
            Log.e(TAG, "playSongAtIndex - Chỉ số bài hát không hợp lệ: " + index);
            return;
        }

        currentSongIndex = index;
        Song song = songList.get(index);

        Log.d(TAG, "playSongAtIndex - Đang phát bài hát ở chỉ số: " + index + ", ID: " + song.getSongId() + ", URL: " + song.getFileUrl());

        // Cập nhật giao diện
        tvSongTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        Glide.with(this).load(song.getCoverUrl()).into(imgCover);
        currentSongId = song.getSongId();
        SongLyrics = song.getLyrics();

        // Phát bài hát
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            Log.d(TAG, "playSongAtIndex - Đã giải phóng ExoPlayer.");
        }
        initializePlayer(song.getFileUrl());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL bài hát không hợp lệ!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "initializePlayer - URL bài hát không hợp lệ: " + url);
            return;
        }

        Log.d(TAG, "initializePlayer - Khởi tạo ExoPlayer với URL: " + url);
        exoPlayer = new ExoPlayer.Builder(this).setLoadControl(new DefaultLoadControl()).build();
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
        btnPlayPause.setImageResource(R.drawable.ic_pause);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlayingNow) {
                isPlaying = isPlayingNow;
                btnPlayPause.setImageResource(isPlayingNow ? R.drawable.ic_pause : R.drawable.ic_play);
                Log.d(TAG, "onIsPlayingChanged: " + isPlayingNow);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                String stateString;
                switch (state) {
                    case Player.STATE_IDLE:
                        stateString = "IDLE";
                        break;
                    case Player.STATE_BUFFERING:
                        stateString = "BUFFERING";
                        break;
                    case Player.STATE_READY:
                        stateString = "READY";
                        if (exoPlayer != null) {
                            seekBar.setMax((int) exoPlayer.getDuration());
                            tvDuration.setText(formatTime((int) exoPlayer.getDuration()));
                            updateSeekBar();
                        }
                        break;
                    case Player.STATE_ENDED:
                        stateString = "ENDED";
                        // Có thể tự động chuyển bài tiếp theo ở đây nếu muốn
                        break;
                    default:
                        stateString = "UNKNOWN";
                        break;
                }
                Log.d(TAG, "onPlaybackStateChanged: " + stateString);
            }

            @Override
            public void onPlayerError(@NonNull androidx.media3.common.PlaybackException error) {
                Log.e(TAG, "onPlayerError - Lỗi ExoPlayer: ", error);
                Toast.makeText(NowPlayingActivity.this, "Lỗi phát nhạc!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void togglePlayPause() {
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.pause();
            } else {
                exoPlayer.play();
            }
        }
    }

    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && !isSeeking) {
                    seekBar.setProgress((int) exoPlayer.getCurrentPosition());
                    tvCurrentTime.setText(formatTime((int) exoPlayer.getCurrentPosition()));
                }
                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    private void openLyricsFragment() {
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        LyricsFragment lyricsFragment = new LyricsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("song_lyrics", SongLyrics); // Truyền lyrics vào Bundle
        lyricsFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down, R.anim.slide_in_up, R.anim.slide_out_down);
        transaction.replace(R.id.fragment_container, lyricsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            findViewById(R.id.fragment_container).setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
            Log.d(TAG, "onDestroy: Đã giải phóng ExoPlayer.");
        }
        super.onDestroy();
    }
}