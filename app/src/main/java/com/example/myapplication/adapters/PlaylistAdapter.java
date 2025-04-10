package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.PlaylistDetailActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private final Context context;
    private final List<Playlist> playlistList;
    private HashSet<String> favouritePlaylistIds;
    private final HashMap<String, Song> songMap;
    private final OnFavouriteClickListener listener;

    public PlaylistAdapter(Context context,
                           List<Playlist> playlistList,
                           HashSet<String> favouritePlaylistIds,
                           HashMap<String, Song> songMap,
                           OnFavouriteClickListener listener) {
        this.context = context;
        this.playlistList = playlistList;
        this.favouritePlaylistIds = favouritePlaylistIds != null ? favouritePlaylistIds : new HashSet<>();
        this.songMap = songMap != null ? songMap : new HashMap<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);

        // Set tên playlist
        holder.tvTitle.setText(playlist.getPlaylistName());

        // Đếm số bài hát có tồn tại trong songMap
        int songCount = 0;
        List<String> songIds = playlist.getListOfSongIds();
        if (songIds != null) {
            for (String songId : songIds) {
                if (songMap.containsKey(songId)) {
                    songCount++;
                }
            }
        }

        Log.d("PlaylistAdapter", "Playlist: " + playlist.getPlaylistName() + " - Songs: " + songCount);
        holder.tvSongCount.setText(songCount + " songs");

        // Load ảnh bìa playlist
        if (playlist.getImageUrl() != null && !playlist.getImageUrl().isEmpty()) {
            Glide.with(context).load(playlist.getImageUrl()).into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.icon_song); // fallback
        }

        // Hiển thị trạng thái yêu thích
        if (favouritePlaylistIds.contains(playlist.getId())) {
            holder.btnFavourite.setImageResource(R.drawable.ic_heart_selection_true);
        } else {
            holder.btnFavourite.setImageResource(R.drawable.ic_heart_selection);
        }

        // Click trái tim để toggle yêu thích
        holder.btnFavourite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavouriteClick(playlist);
            }
        });

        // 👇 Bấm vào item để mở chi tiết Playlist
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlaylistDetailActivity.class);
            intent.putExtra("playlist_id", playlist.getId());
            intent.putExtra("playlist_name", playlist.getPlaylistName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public void setFavouritePlaylistIds(HashSet<String> favouritePlaylistIds) {
        this.favouritePlaylistIds = favouritePlaylistIds != null ? favouritePlaylistIds : new HashSet<>();
        notifyDataSetChanged();
    }

    public interface OnFavouriteClickListener {
        void onFavouriteClick(Playlist playlist);
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvSongCount;
        ImageView btnFavourite;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.img_playlist);
            tvTitle = itemView.findViewById(R.id.txt_playlist_name);
            tvSongCount = itemView.findViewById(R.id.txt_song_count);
            btnFavourite = itemView.findViewById(R.id.icon_favourite);
        }
    }
}
