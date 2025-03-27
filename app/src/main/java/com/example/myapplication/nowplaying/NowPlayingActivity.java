package com.example.myapplication.nowplaying;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {

    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvDuration;
    private ImageView imgCover, btnPlayPause, btnBack;
    private SeekBar seekBar;
    private static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private String songUrl;
    private boolean isPlaying = false;
    private int lastPosition = 0;
    private boolean isNewMediaPlayer = false; // Kiểm tra xem MediaPlayer có phải là mới không

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
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("song_title");
            String artist = intent.getStringExtra("artist_name");
            String coverUrl = intent.getStringExtra("cover_url");
            songUrl = intent.getStringExtra("song_url");

            tvSongTitle.setText(title);
            tvArtist.setText(artist);
            Glide.with(this).load(coverUrl).into(imgCover);
        }

        // Lấy vị trí nhạc từ SharedPreferences
        lastPosition = getSharedPreferences("MusicPrefs", Context.MODE_PRIVATE).getInt("last_position", 0);

        // Nếu MediaPlayer chưa tồn tại, tạo mới
        if (mediaPlayer == null) {
            isNewMediaPlayer = true;
            playSong(songUrl);
        } else {
            resumeSong();
        }

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        btnBack.setOnClickListener(v -> {
            saveCurrentPosition();
            finish();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playSong(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                seekBar.setMax(mp.getDuration());
                tvDuration.setText(formatTime(mp.getDuration()));

                // Nếu là MediaPlayer mới, bắt đầu từ đầu
                if (isNewMediaPlayer) {
                    mp.start();
                    isPlaying = true;
                    btnPlayPause.setImageResource(R.drawable.ic_pause);
                    updateSeekBar();
                } else {
                    mp.seekTo(lastPosition);
                }
                Toast.makeText(this, "Đang phát nhạc...", Toast.LENGTH_SHORT).show();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "Lỗi phát nhạc!", Toast.LENGTH_SHORT).show();
                return true;
            });

        } catch (IOException e) {
            Toast.makeText(this, "Không thể phát nhạc!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeSong() {
        mediaPlayer.start();
        mediaPlayer.seekTo(lastPosition);
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        updateSeekBar();
    }

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            }
        }
    }

    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    private void saveCurrentPosition() {
        if (mediaPlayer != null) {
            getSharedPreferences("MusicPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("last_position", mediaPlayer.getCurrentPosition())
                    .apply();
        }
    }

    @Override
    protected void onDestroy() {
        saveCurrentPosition();
        super.onDestroy();
    }
}
