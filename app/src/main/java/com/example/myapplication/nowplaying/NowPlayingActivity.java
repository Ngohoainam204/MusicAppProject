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
import com.example.myapplication.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {
    private static final String TAG = "NowPlayingActivity";

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

    private List<Song> songList = new ArrayList<>();
    private int currentSongIndex = -1;


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

        Intent intent = getIntent();
        if (intent != null) {
            currentSongId = intent.getStringExtra("song_id");
            Log.d(TAG, "Intent received - song_id: " + currentSongId);

            Object songsObj = intent.getSerializableExtra("playlist_songs");
            if (songsObj == null) {
                Log.e(TAG, "playlist_songs is null in intent");
            } else {
                Log.d(TAG, "playlist_songs class: " + songsObj.getClass().getName());
                if (songsObj instanceof ArrayList<?>) {
                    ArrayList<?> tempList = (ArrayList<?>) songsObj;
                    Log.d(TAG, "playlist_songs size: " + tempList.size());

                    if (!tempList.isEmpty() && tempList.get(0) instanceof Song) {
                        songList = (ArrayList<Song>) songsObj;
                        Log.d(TAG, "playlist_songs contains Song instances");
                    } else {
                        Log.e(TAG, "playlist_songs does not contain Song instances.");
                    }
                } else {
                    Log.e(TAG, "playlist_songs is not an ArrayList.");
                }
            }
        }

        if (songList != null && !songList.isEmpty() && currentSongId != null) {
            currentSongIndex = findSongIndexById(currentSongId);
            Log.d(TAG, "onCreate - Found current index: " + currentSongIndex);
            loadCurrentSongData();
        } else {
            Log.e(TAG, "Failed to load songs - songList null/empty or currentSongId null");
            Toast.makeText(this, "Không tìm thấy danh sách bài hát.", Toast.LENGTH_SHORT).show();
            finish();
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


    }


    private void loadCurrentSongData() {
        if (currentSongIndex != -1 && currentSongIndex < songList.size()) {
            Song currentSong = songList.get(currentSongIndex);
            tvSongTitle.setText(currentSong.getTitle());
            tvArtist.setText(currentSong.getArtist());
            Glide.with(this).load(currentSong.getCoverUrl()).into(imgCover);
            initializePlayer(currentSong.getFileUrl());
            SongLyrics = currentSong.getLyrics();
        }
    }

    private int findSongIndexById(String songId) {
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getSongId().equals(songId)) {
                return i;
            }
        }
        return -1;
    }

    void playNextSong() {
        if (songList.isEmpty() || currentSongIndex == -1) return;
        currentSongIndex = (currentSongIndex + 1) % songList.size();
        playSongAtIndex(currentSongIndex);
    }

    void playPreviousSong() {
        if (songList.isEmpty() || currentSongIndex == -1) return;
        currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
        playSongAtIndex(currentSongIndex);
    }

    private void playSongAtIndex(int index) {
        if (index < 0 || index >= songList.size()) return;
        currentSongIndex = index;
        Song song = songList.get(index);
        tvSongTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
        Glide.with(this).load(song.getCoverUrl()).into(imgCover);
        currentSongId = song.getSongId();
        SongLyrics = song.getLyrics();

        if (exoPlayer != null) {
            exoPlayer.release();
        }
        initializePlayer(song.getFileUrl());
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initializePlayer(String url) {
        if (url == null || url.isEmpty()) return;
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
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY && exoPlayer != null) {
                    seekBar.setMax((int) exoPlayer.getDuration());
                    tvDuration.setText(formatTime((int) exoPlayer.getDuration()));
                    updateSeekBar();
                }
            }

            @Override
            public void onPlayerError(@NonNull androidx.media3.common.PlaybackException error) {
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
        bundle.putString("song_lyrics", SongLyrics);
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
        }
        super.onDestroy();
    }
}
