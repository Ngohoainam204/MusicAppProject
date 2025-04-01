package com.example.myapplication.nowplaying;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

public class LyricsFragment extends Fragment {

    private static final String ARG_COVER_URL = "cover_url";
    private static final String ARG_BACKGROUND_URL = "background_url";
    private static final String ARG_SONG_TITLE = "song_title";
    private static final String ARG_LYRICS = "lyrics";

    private String coverUrl;
    private String backgroundUrl;
    private String songTitle;
    private String lyrics;

    public LyricsFragment() {
        // Required empty public constructor
    }

    public static LyricsFragment newInstance(String coverUrl, String backgroundUrl, String songTitle, String lyrics) {
        LyricsFragment fragment = new LyricsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COVER_URL, coverUrl);
        args.putString(ARG_BACKGROUND_URL, backgroundUrl);
        args.putString(ARG_SONG_TITLE, songTitle);
        args.putString(ARG_LYRICS, lyrics);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            coverUrl = getArguments().getString(ARG_COVER_URL);
            backgroundUrl = getArguments().getString(ARG_BACKGROUND_URL);
            songTitle = getArguments().getString(ARG_SONG_TITLE);
            lyrics = getArguments().getString(ARG_LYRICS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics, container, false);

        ImageView imvCover = view.findViewById(R.id.imvCover);
        ImageView imgLyricsBackground = view.findViewById(R.id.imgLyricsBackground);
        TextView tvLyricsSongTitle = view.findViewById(R.id.tvLyricsSongTitle);
        TextView tvLyricsContent = view.findViewById(R.id.tvLyricsContent);

        // Load ảnh bìa & background bằng Glide
        

        // Cập nhật tiêu đề bài hát và lyrics
        tvLyricsSongTitle.setText(songTitle);
        tvLyricsContent.setText(lyrics);

        return view;
    }
}
