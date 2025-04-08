package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.nowplaying.NowPlayingActivity;
import com.example.myapplication.models.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final List<Song> songList;
    private EditText etSearch;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(song.getDuration());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.ic_pause) // Ảnh tạm khi tải
                .error(R.drawable.ic_pause) // Ảnh lỗi nếu tải thất bại
                .centerCrop()
                .into(holder.imgSong);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NowPlayingActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("artist_name", song.getArtist());
            intent.putExtra("cover_url", song.getCoverUrl());
            intent.putExtra("song_url", song.getFileUrl());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_lyrics", song.getLyrics()); // Thêm lyrics vào Intent
            intent.putExtra("song_id", song.getSongId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView imgSong;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDuration = itemView.findViewById(R.id.tv_song_duration);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            imgSong = itemView.findViewById(R.id.img_song_cover);
        }
    }
}
