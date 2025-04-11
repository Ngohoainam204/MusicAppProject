package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.Search.SearchItem;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<SearchItem> originalItems;
    private final List<SearchItem> filteredItems;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    // Listener
    public interface OnItemClickListener {
        void onSongClick(Song song);

        void onPlaylistClick(Playlist playlist);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SearchAdapter(Context context, List<SearchItem> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
    }

    public SearchAdapter(Context context) {
        this.context = context;
        this.originalItems = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return filteredItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == SearchItem.TYPE_SONG) {
            View view = inflater.inflate(R.layout.song_item, parent, false);
            return new SongViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.playlist_item, parent, false);
            return new PlaylistViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchItem item = filteredItems.get(position);
        if (item.getType() == SearchItem.TYPE_SONG) {
            ((SongViewHolder) holder).bind(((SearchItem.SongItem) item).getSong());
        } else {
            ((PlaylistViewHolder) holder).bind(((SearchItem.PlaylistItem) item).getPlaylist());
        }
    }

    public void filter(String query) {
        filteredItems.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredItems.addAll(originalItems);
        } else {
            String lower = query.toLowerCase();
            for (SearchItem item : originalItems) {
                if (item.getType() == SearchItem.TYPE_SONG) {
                    Song song = ((SearchItem.SongItem) item).getSong();
                    if (song != null && (song.getTitle().toLowerCase().contains(lower) ||
                            song.getArtist().toLowerCase().contains(lower))) {
                        filteredItems.add(item);
                    }
                } else {
                    Playlist playlist = ((SearchItem.PlaylistItem) item).getPlaylist();
                    if (playlist != null && playlist.getPlaylistName().toLowerCase().contains(lower)) {
                        filteredItems.add(item);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateItems(List<SearchItem> newItems) {
        originalItems.clear();
        originalItems.addAll(newItems);
        filter("");
    }

    // -----------------------
    // ViewHolder for Song
    // -----------------------
    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvArtist;
        private final ImageView ivCover, ivFavourite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            ivCover = itemView.findViewById(R.id.img_song_cover);
            ivFavourite = itemView.findViewById(R.id.ivFavourite);
        }

        public void bind(Song song) {
            if (song == null) return;

            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            Glide.with(context)
                    .load(song.getCoverUrl())
                    .placeholder(R.drawable.music)
                    .into(ivCover);

            FirebaseAuth auth = FirebaseAuth.getInstance();
            DatabaseReference favoritesRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesSongs");
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail().replace(".", "_") : null;

            if (userId != null) {
                favoritesRef.child(userId).child(song.getSongId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean isFavorite = snapshot.exists();
                                updateFavoriteIcon(isFavorite);

                                ivFavourite.setOnClickListener(v -> {
                                    toggleFavorite(song, isFavorite);
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("SearchAdapter", "Favorite check failed: " + error.getMessage());
                            }
                        });
            } else {
                ivFavourite.setOnClickListener(v -> {
                    Toast.makeText(context, "Bạn cần đăng nhập để yêu thích bài hát", Toast.LENGTH_SHORT).show();
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song);
                }
            });
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            ivFavourite.setImageResource(
                    isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection
            );
        }

        private void toggleFavorite(Song song, boolean currentStatus) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail().replace(".", "_") : null;
            DatabaseReference songRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesSongs").child(userId).child(song.getSongId());

            if (currentStatus) {
                songRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            updateFavoriteIcon(false);
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi khi xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        });
            } else {
                songRef.setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            updateFavoriteIcon(true);
                            Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    // -----------------------
    // ViewHolder for Playlist
    // -----------------------
    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPlaylistName;
        private final ImageView ivPlaylistCover;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.txt_playlist_name);
            ivPlaylistCover = itemView.findViewById(R.id.img_playlist);
        }

        public void bind(Playlist playlist) {
            if (playlist == null) return;

            tvPlaylistName.setText(playlist.getPlaylistName());

            if (playlist.getCoverUrl() != null && !playlist.getCoverUrl().isEmpty()) {
                Glide.with(context)
                        .load(playlist.getCoverUrl())
                        .placeholder(R.drawable.music)
                        .into(ivPlaylistCover);
            } else {
                ivPlaylistCover.setImageResource(R.drawable.music);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaylistClick(playlist);
                }
            });
        }
    }

    public void updateData(List<Song> songs, List<Playlist> playlists) {
        List<SearchItem> newItems = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                newItems.add(new SearchItem.SongItem(song));
            }
        }
        if (playlists != null) {
            for (Playlist playlist : playlists) {
                newItems.add(new SearchItem.PlaylistItem(playlist));
            }
        }
        updateItems(newItems);
    }
}
