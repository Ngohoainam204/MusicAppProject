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
import com.example.myapplication.nowplaying.NowPlayingActivity;
import com.example.myapplication.nowplaying.PlaylistDetailActivity;
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

    public SearchAdapter(Context context, List<SearchItem> items) {
        this.context = context;
        this.originalItems = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
        Log.d("SearchAdapter", "Constructor with items: " + filteredItems.size() + " items");
    }

    public SearchAdapter(Context context) {
        this.context = context;
        this.originalItems = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
        Log.d("SearchAdapter", "Empty constructor");
    }

    @Override
    public int getItemViewType(int position) {
        int type = filteredItems.get(position).getType();
        Log.d("SearchAdapter", "getItemViewType for position " + position + ": " + (type == SearchItem.TYPE_SONG ? "SONG" : "PLAYLIST"));
        return type;
    }

    @Override
    public int getItemCount() {
        int count = filteredItems.size();
        Log.d("SearchAdapter", "getItemCount: " + count);
        return count;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("SearchAdapter", "onCreateViewHolder for viewType: " + (viewType == SearchItem.TYPE_SONG ? "SONG" : "PLAYLIST"));
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
        Log.d("SearchAdapter", "onBindViewHolder called for position: " + position);
        SearchItem item = filteredItems.get(position);
        if (item.getType() == SearchItem.TYPE_SONG) {
            SearchItem.SongItem songItem = (SearchItem.SongItem) item;
            Song song = songItem.getSong();
            String title = song != null ? song.getTitle() : "null";
            String artist = song != null ? song.getArtist() : "null";
            String coverUrl = song != null ? song.getCoverUrl() : "null";
            Log.d("SearchAdapter", "Binding SONG - Position: " + position + ", Title: " + title + ", Artist: " + artist + ", Cover URL: " + coverUrl);
            ((SongViewHolder) holder).bind(song);
        } else {
            SearchItem.PlaylistItem playlistItem = (SearchItem.PlaylistItem) item;
            Playlist playlist = playlistItem.getPlaylist();
            String name = playlist != null ? playlist.getPlaylistName() : "null";
            String cover = playlist != null ? playlist.getCoverUrl() : "null";
            Log.d("SearchAdapter", "Binding PLAYLIST - Position: " + position + ", Name: " + name + ", Cover URL: " + cover);
            ((PlaylistViewHolder) holder).bind(playlist);
        }
    }

    public void filter(String query) {
        Log.d("SearchAdapter", "filter called with query: " + query);
        filteredItems.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredItems.addAll(originalItems);
            Log.d("SearchAdapter", "filter: query is empty, added " + originalItems.size() + " original items");
        } else {
            String lower = query.toLowerCase();
            for (SearchItem item : originalItems) {
                if (item.getType() == SearchItem.TYPE_SONG) {
                    Song song = ((SearchItem.SongItem) item).getSong();
                    if (song != null && (song.getTitle().toLowerCase().contains(lower) ||
                            song.getArtist().toLowerCase().contains(lower))) {
                        filteredItems.add(item);
                        Log.d("SearchAdapter", "filter: added song - " + song.getTitle());
                    }
                } else {
                    Playlist playlist = ((SearchItem.PlaylistItem) item).getPlaylist();
                    if (playlist != null && playlist.getPlaylistName().toLowerCase().contains(lower)) {
                        filteredItems.add(item);
                        Log.d("SearchAdapter", "filter: added playlist - " + playlist.getPlaylistName());
                    }
                }
            }
            Log.d("SearchAdapter", "filter: added " + filteredItems.size() + " items matching query");
        }
        notifyDataSetChanged();
        Log.d("SearchAdapter", "filter: notifyDataSetChanged called");
    }

    public void updateItems(List<SearchItem> newItems) {
        Log.d("SearchAdapter", "updateItems called with " + newItems.size() + " new items");
        originalItems.clear();
        originalItems.addAll(newItems);
        filter("");
    }

    // -----------------------
    // ViewHolder for Song
    // -----------------------
    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvArtist;
        private final ImageView ivCover, ivFavourite; // Đổi tên btnFavoriteSong thành ivFavourite

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            ivCover = itemView.findViewById(R.id.img_song_cover);
            ivFavourite = itemView.findViewById(R.id.ivFavourite); // Ánh xạ view với ID chính xác
            Log.d("SongViewHolder", "Constructor called");
        }

        public void bind(Song song) {
            Log.d("SongViewHolder", "bind called with song: " + (song != null ? song.getTitle() : "null"));
            if (song != null) {
                tvTitle.setText(song.getTitle());
                tvArtist.setText(song.getArtist());
                Glide.with(context)
                        .load(song.getCoverUrl())
                        .placeholder(R.drawable.music)
                        .into(ivCover);

                // Logic yêu thích bài hát
                FirebaseAuth auth = FirebaseAuth.getInstance();
                DatabaseReference favoritesRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesSongs");
                String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail().replace(".", "_") : null;

                if (userId != null) {
                    favoritesRef.child(userId).child(song.getSongId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean isSongFavorite = snapshot.exists();
                                    updateFavoriteSongIcon(isSongFavorite);

                                    ivFavourite.setOnClickListener(v -> {
                                        toggleFavoriteSong(song, isSongFavorite);
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("SongViewHolder", "Lỗi kiểm tra trạng thái yêu thích bài hát: " + error.getMessage());
                                }
                            });
                } else {
                    ivFavourite.setOnClickListener(v -> {
                        Toast.makeText(context, "Bạn cần đăng nhập để yêu thích bài hát", Toast.LENGTH_SHORT).show();
                    });
                }

                itemView.setOnClickListener(v -> {
                    Log.d("SongViewHolder", "itemView clicked, starting NowPlayingActivity for song ID: " + song.getSongId());
                    Intent intent = new Intent(context, NowPlayingActivity.class);
                    intent.putExtra("songId", song.getSongId());
                    context.startActivity(intent);
                });
            }
        }

        private void updateFavoriteSongIcon(boolean isFavorite) {
            ivFavourite.setImageResource(
                    isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection
            );
        }

        private void toggleFavoriteSong(Song song, boolean currentFavoriteStatus) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            DatabaseReference favoritesRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesSongs");
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getEmail().replace(".", "_");
                DatabaseReference songFavoriteRef = favoritesRef.child(userId).child(song.getSongId());

                if (currentFavoriteStatus) {
                    songFavoriteRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                updateFavoriteSongIcon(false);
                                Toast.makeText(context, "Đã xóa khỏi bài hát yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi khi xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                Log.e("SongViewHolder", "Lỗi xóa yêu thích bài hát: " + e.getMessage());
                            });
                } else {
                    songFavoriteRef.setValue(true) // Bạn có thể lưu thêm thông tin nếu cần
                            .addOnSuccessListener(aVoid -> {
                                updateFavoriteSongIcon(true);
                                Toast.makeText(context, "Đã thêm vào bài hát yêu thích", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Lỗi khi thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                                Log.e("SongViewHolder", "Lỗi thêm yêu thích bài hát: " + e.getMessage());
                            });
                }
            } else {
                Toast.makeText(context, "Bạn cần đăng nhập để yêu thích bài hát", Toast.LENGTH_SHORT).show();
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
            Log.d("PlaylistViewHolder", "Constructor called");
        }

        public void bind(Playlist playlist) {
            Log.d("PlaylistViewHolder", "bind called with playlist: " + (playlist != null ? playlist.getPlaylistName() : "null"));
            if (playlist != null) {
                tvPlaylistName.setText(playlist.getPlaylistName());

                String coverUrl = playlist.getCoverUrl();
                Log.d("PlaylistViewHolder", "bind - Cover URL: " + coverUrl);
                if (coverUrl != null && !coverUrl.isEmpty()) {
                    Glide.with(context)
                            .load(coverUrl)
                            .placeholder(R.drawable.ic_play)
                            .into(ivPlaylistCover);
                } else {
                    ivPlaylistCover.setImageResource(R.drawable.ic_play); // Ảnh mặc định
                }

                itemView.setOnClickListener(v -> {
                    Log.d("PlaylistViewHolder", "itemView clicked, starting PlaylistDetailActivity for playlist ID: " + playlist.getPlaylistId());
                    Intent intent = new Intent(context, PlaylistDetailActivity.class);
                    intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_ID, playlist.getPlaylistId());
                    intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST_NAME, playlist.getPlaylistName());
                    context.startActivity(intent);
                });
            }
        }
    }

    public void updateData(List<Song> songs, List<Playlist> playlists) {
        Log.d("SearchAdapter", "updateData called with " + (songs != null ? songs.size() : 0) + " songs and " + (playlists != null ? playlists.size() : 0) + " playlists");
        List<SearchItem> newItems = new ArrayList<>();

        if (songs != null) {
            for (Song song : songs) {
                newItems.add(new SearchItem.SongItem(song));
                Log.d("SearchAdapter", "updateData: added song - " + song.getTitle());
            }
        }

        if (playlists != null) {
            for (Playlist playlist : playlists) {
                newItems.add(new SearchItem.PlaylistItem(playlist));
                Log.d("SearchAdapter", "updateData: added playlist - " + playlist.getPlaylistName());
            }
        }

        updateItems(newItems);
    }
}