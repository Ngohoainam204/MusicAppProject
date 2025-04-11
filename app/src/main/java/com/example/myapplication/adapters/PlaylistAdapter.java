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
import com.example.myapplication.R;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.example.myapplication.detail.PlaylistDetailActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private static final String TAG = "PlaylistAdapter";
    private final Context context;
    private final List<Playlist> playlistList;
    private HashSet<String> favouritePlaylistIds;
    private HashMap<String, Song> songMap;
    private final OnFavouriteClickListener listener;

    public PlaylistAdapter(Context context,
                           List<Playlist> playlistList,
                           HashSet<String> favouritePlaylistIds,
                           HashMap<String, Song> songMap,
                           OnFavouriteClickListener listener) {
        this.context = context;
        this.playlistList = playlistList;
        this.favouritePlaylistIds = favouritePlaylistIds != null ? new HashSet<>(favouritePlaylistIds) : new HashSet<>();
        this.songMap = songMap != null ? new HashMap<>(songMap) : new HashMap<>();
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

        holder.tvTitle.setText(playlist.getPlaylistName());

        if (playlist.getCoverUrl() != null && !playlist.getCoverUrl().isEmpty()) {
            Glide.with(context)
                    .load(playlist.getCoverUrl())
                    .placeholder(R.drawable.music) // Add a placeholder image
                    .error(R.drawable.music)       // Show error image if loading fails
                    .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.music);
        }

        if (favouritePlaylistIds.contains(playlist.getId())) {
            holder.btnFavourite.setImageResource(R.drawable.ic_heart_selection_true);
        } else {
            holder.btnFavourite.setImageResource(R.drawable.ic_heart_selection);
        }

        holder.btnFavourite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavouriteClick(playlist);
            }
        });

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

    public void setSongMap(HashMap<String, Song> newSongMap) {
        if (newSongMap != null) {
            this.songMap.clear();
            this.songMap.putAll(newSongMap);
            notifyDataSetChanged();
            Log.d(TAG, "Song map updated. Size: " + this.songMap.size());
        } else {
            Log.w(TAG, "Attempted to set a null song map.");
        }
    }

    public void setFavouritePlaylistIds(HashSet<String> newFavouritePlaylistIds) {
        if (newFavouritePlaylistIds != null) {
            this.favouritePlaylistIds = new HashSet<>(newFavouritePlaylistIds);
            notifyDataSetChanged();
            Log.d(TAG, "Favourite playlist IDs updated. Size: " + this.favouritePlaylistIds.size());
        } else {
            this.favouritePlaylistIds = new HashSet<>();
            notifyDataSetChanged();
            Log.w(TAG, "Favourite playlist IDs set to null, now empty.");
        }
    }

    public interface OnFavouriteClickListener {
        void onFavouriteClick(Playlist playlist);
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        ImageView btnFavourite;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.img_playlist);
            tvTitle = itemView.findViewById(R.id.txt_playlist_name);
            btnFavourite = itemView.findViewById(R.id.icon_favourite);
        }
    }
}