package com.example.myapplication.nowplaying;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.DefaultLoadControl;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.nowplaying.LyricsFragment;

import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {
    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvDuration;
    private ImageView imgCover, btnPlayPause, btnBack, btnLyric;
    private SeekBar seekBar;
    private ExoPlayer exoPlayer;
    private final Handler handler = new Handler();
    private boolean isPlaying = false;
    private boolean isSeeking = false; // Trạng thái tua

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
        btnLyric = findViewById(R.id.btnLyric);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("song_title");
            String artist = intent.getStringExtra("artist_name");
            String coverUrl = intent.getStringExtra("cover_url");
            String songUrl = intent.getStringExtra("song_url");

            tvSongTitle.setText(title);
            tvArtist.setText(artist);
            Glide.with(this).load(coverUrl).into(imgCover);
            initializePlayer(songUrl);
        }

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnBack.setOnClickListener(v -> finish());
        btnLyric.setOnClickListener(v -> openLyricsFragment());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && exoPlayer != null) {
                    isSeeking = true; // Bắt đầu tua
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
                    exoPlayer.seekTo(seekBar.getProgress());
                    isSeeking = false; // Kết thúc tua
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        exoPlayer = new ExoPlayer.Builder(this).setLoadControl(new DefaultLoadControl()).build();
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        exoPlayer.prepare();
        exoPlayer.setPlayWhenReady(true);
        btnPlayPause.setImageResource(R.drawable.ic_pause);

        exoPlayer.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlayingNow) {
                isPlaying = isPlayingNow;
                btnPlayPause.setImageResource(isPlayingNow ? R.drawable.ic_pause : R.drawable.ic_play);
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == ExoPlayer.STATE_READY) {
                    seekBar.setMax((int) exoPlayer.getDuration());
                    tvDuration.setText(formatTime((int) exoPlayer.getDuration()));
                    updateSeekBar();
                }
            }
        });
    }

    private void togglePlayPause() {
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
                handler.postDelayed(this, 100); // Giảm độ trễ xuống 100ms
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
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Áp dụng hiệu ứng trượt từ dưới lên
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
        }
        super.onDestroy();
    }
}
