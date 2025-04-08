package com.example.myapplication.nowplaying;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class LyricsFragment extends Fragment {

    private ImageView imgLyricsBackground;
    private TextView tvLyricsSongTitle;
    private TextView tvArtist;
    private TextView tvLyricsContent;
    private TextView tvCurrentTime;
    private TextView tvDuration;
    private SeekBar seekBar;
    private ImageView btnPlayPause;
    private ImageView btnPrevious;
    private ImageView btnNext;
    private ImageView btnBack;

    private NowPlayingActivity nowPlayingActivity;
    private final Handler handler = new Handler();

    // Firebase references
    private FirebaseDatabase database;
    private DatabaseReference lyricsRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nowPlayingActivity = (NowPlayingActivity) getActivity();
        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance();
        lyricsRef = database.getReference("songs"); // Refer to songs node in Firebase
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics, container, false);

        initViews(view);
        updateUIFromNowPlayingActivity();
        setupListeners();
        startSeekBarUpdate();

        return view;
    }

    private void initViews(View view) {
        imgLyricsBackground = view.findViewById(R.id.imgLyricsBackground);
        tvLyricsSongTitle = view.findViewById(R.id.tvLyricsSongTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        tvLyricsContent = view.findViewById(R.id.tvLyricsContent);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        seekBar = view.findViewById(R.id.seekBar);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnNext = view.findViewById(R.id.btnNext);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void updateUIFromNowPlayingActivity() {
        if (nowPlayingActivity != null) {
            tvLyricsSongTitle.setText(nowPlayingActivity.tvSongTitle.getText());
            tvArtist.setText(nowPlayingActivity.tvArtist.getText());
            tvCurrentTime.setText(nowPlayingActivity.tvCurrentTime.getText());
            tvDuration.setText(nowPlayingActivity.tvDuration.getText());
            tvLyricsContent.setText(nowPlayingActivity.SongLyrics);
            // Chỉ load background từ NowPlayingActivity
            Glide.with(this)
                    .load(nowPlayingActivity.imgCover.getDrawable())
                    .into(imgLyricsBackground);

            if (nowPlayingActivity.exoPlayer != null) {
                seekBar.setMax((int) nowPlayingActivity.exoPlayer.getDuration());
                seekBar.setProgress((int) nowPlayingActivity.exoPlayer.getCurrentPosition());
                updatePlayPauseButton();
            }

            // Load lyrics từ Firebase
            loadLyricsFromFirebase(nowPlayingActivity.currentSongId);  // currentSongId là ID bài hát hiện tại
        }
    }

    private void loadLyricsFromFirebase(String songId) {
        // Kiểm tra nếu songId là null
        if (songId == null || songId.isEmpty()) {
            Toast.makeText(getContext(), "Song ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy dữ liệu lyrics từ Firebase
        lyricsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String lyrics = dataSnapshot.child("lyrics").getValue(String.class);
                    if (lyrics != null) {
                        tvLyricsContent.setText(lyrics);
                    } else {
                        tvLyricsContent.setText("Lyrics not available.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error (e.g., show a Toast)
                Toast.makeText(getContext(), "Failed to load lyrics.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (nowPlayingActivity != null) {
                nowPlayingActivity.togglePlayPause();
                updatePlayPauseButton();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            // TODO: Thêm chức năng previous song
        });

        btnNext.setOnClickListener(v -> {
            // TODO: Thêm chức năng next song
        });

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (nowPlayingActivity != null) {
                    nowPlayingActivity.isSeeking = true;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (nowPlayingActivity != null && nowPlayingActivity.exoPlayer != null) {
                    nowPlayingActivity.exoPlayer.seekTo(seekBar.getProgress());
                    nowPlayingActivity.isSeeking = false;
                }
            }
        });
    }

    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (nowPlayingActivity != null
                        && nowPlayingActivity.exoPlayer != null
                        && !nowPlayingActivity.isSeeking) {
                    int currentPosition = (int) nowPlayingActivity.exoPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void updatePlayPauseButton() {
        if (nowPlayingActivity != null && nowPlayingActivity.exoPlayer != null) {
            btnPlayPause.setImageResource(
                    nowPlayingActivity.exoPlayer.isPlaying()
                            ? R.drawable.ic_pause
                            : R.drawable.ic_play
            );
        }
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
