package com.example.myapplication.Search;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.myapplication.Search.SearchItem;

public class SearchItemDiffCallback extends DiffUtil.ItemCallback<SearchItem> {
    @Override
    public boolean areItemsTheSame(@NonNull SearchItem oldItem, @NonNull SearchItem newItem) {
        if (oldItem instanceof SearchItem.SongItem && newItem instanceof SearchItem.SongItem) {
            return ((SearchItem.SongItem) oldItem).getSong().getSongId()
                    .equals(((SearchItem.SongItem) newItem).getSong().getSongId());
        } else if (oldItem instanceof SearchItem.PlaylistItem && newItem instanceof SearchItem.PlaylistItem) {
            return ((SearchItem.PlaylistItem) oldItem).getPlaylist().getPlaylistId()
                    .equals(((SearchItem.PlaylistItem) newItem).getPlaylist().getPlaylistId());
        }
        return false;
    }

    @SuppressLint("DiffUtilEquals")
    @Override
    public boolean areContentsTheSame(@NonNull SearchItem oldItem, @NonNull SearchItem newItem) {
        return oldItem.equals(newItem);
    }
}
