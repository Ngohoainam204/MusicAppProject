package com.example.myapplication.adapters;

import android.content.Context;
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
import com.example.myapplication.models.Album;
import com.example.myapplication.models.Artist;
import com.example.myapplication.models.Playlist;
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<SearchItem> originalItems;
    private final List<SearchItem> filteredItems;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OnItemClickListener {
        void onSongClick(Song song);

        void onPlaylistClick(Playlist playlist);

        void onAlbumClick(Album album);

        void onArtistClick(Artist artist);
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
        this(context, new ArrayList<>());
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
        switch (viewType) {
            case SearchItem.TYPE_SONG:
                return new SongViewHolder(inflater.inflate(R.layout.song_item, parent, false));
            case SearchItem.TYPE_PLAYLIST:
                return new PlaylistViewHolder(inflater.inflate(R.layout.playlist_item, parent, false));
            case SearchItem.TYPE_ALBUM:
                return new AlbumViewHolder(inflater.inflate(R.layout.album_item, parent, false));
            case SearchItem.TYPE_ARTIST:
                return new ArtistViewHolder(inflater.inflate(R.layout.artist_item, parent, false));
            default:
                throw new IllegalArgumentException("Unknown viewType: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchItem item = filteredItems.get(position);
        switch (item.getType()) {
            case SearchItem.TYPE_SONG:
                ((SongViewHolder) holder).bind(((SearchItem.SongItem) item).getSong());
                break;
            case SearchItem.TYPE_PLAYLIST:
                ((PlaylistViewHolder) holder).bind(((SearchItem.PlaylistItem) item).getPlaylist());
                break;
            case SearchItem.TYPE_ALBUM:
                ((AlbumViewHolder) holder).bind(((SearchItem.AlbumItem) item).getAlbum());
                break;
            case SearchItem.TYPE_ARTIST:
                ((ArtistViewHolder) holder).bind(((SearchItem.ArtistItem) item).getArtist());
                break;
        }
    }

    public void filter(String query) {
        filteredItems.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredItems.addAll(originalItems);
        } else {
            for (SearchItem item : originalItems) {
                if (item.matches(query)) {
                    filteredItems.add(item);
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

    // ========================= Helper Methods ==============================

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);
    }

    private void toggleFavorite(String userId, String itemId, String node, ImageView icon, String successAdd, String successRemove) {
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference("Favourites" + node)
                .child(userId).child(itemId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isCurrentlyFavorite = snapshot.exists();
                if (isCurrentlyFavorite) {
                    ref.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, successRemove, Toast.LENGTH_SHORT).show();
                        updateFavoriteIcon(icon, false);
                    });
                } else {
                    ref.setValue(true).addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, successAdd, Toast.LENGTH_SHORT).show();
                        updateFavoriteIcon(icon, true);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SearchAdapter", "Lỗi thao tác yêu thích " + node + ": " + error.getMessage());
            }
        });
    }

    private void checkFavoriteStatus(String userId, String itemId, String node, ImageView icon) {
        if (userId != null) {
            DatabaseReference favRef = FirebaseDatabase.getInstance(DB_URL).getReference("Favourites" + node)
                    .child(userId).child(itemId);
            favRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    updateFavoriteIcon(icon, snapshot.exists());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SearchAdapter", "Lỗi kiểm tra yêu thích " + node + ": " + error.getMessage());
                }
            });
        } else {
            icon.setOnClickListener(v -> Toast.makeText(context, "Bạn cần đăng nhập để yêu thích", Toast.LENGTH_SHORT).show());
        }
    }

    // ========================= ViewHolders ==============================

    class SongViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvArtist, tvDuration;
        private final ImageView ivCover, ivFavourite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            tvDuration = itemView.findViewById(R.id.tv_song_duration);
            ivCover = itemView.findViewById(R.id.img_song_cover);
            ivFavourite = itemView.findViewById(R.id.ivFavourite);
        }

        public void bind(Song song) {
            if (song == null) return;

            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            tvDuration.setText(song.getDuration());
            Glide.with(context).load(song.getCoverUrl()).placeholder(R.drawable.music).into(ivCover);

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

            checkFavoriteStatus(userId, song.getSongId(), "Songs", ivFavourite);

            ivFavourite.setOnClickListener(v -> {
                toggleFavorite(userId, song.getSongId(), "Songs", ivFavourite,
                        "Đã thêm vào yêu thích", "Đã xóa khỏi yêu thích");
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSongClick(song);
            });
        }
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPlaylistName;
        private final ImageView ivPlaylistCover, ivFavourite;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.txt_playlist_name);
            ivPlaylistCover = itemView.findViewById(R.id.img_playlist);
            ivFavourite = itemView.findViewById(R.id.icon_favourite_playlist);
        }

        public void bind(Playlist playlist) {
            Log.d("SearchAdapter", "Bind playlist: " + playlist.getPlaylistName());
            Log.d("SearchAdapter", "Playlist ID: " + playlist.getPlaylistId());

            if (playlist == null) return;

            tvPlaylistName.setText(playlist.getPlaylistName());
            Glide.with(context)
                    .load(playlist.getCoverUrl())
                    .placeholder(R.drawable.music)
                    .into(ivPlaylistCover);

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

            checkFavoriteStatus(userId, playlist.getPlaylistId(), "Playlist", ivFavourite);

            ivFavourite.setOnClickListener(v -> {
                if (playlist.getPlaylistId() != null && !playlist.getPlaylistId().equals("0")) {
                    toggleFavorite(userId, playlist.getPlaylistId(), "Playlist", ivFavourite,
                            "Đã thêm playlist vào yêu thích", "Đã xóa playlist khỏi yêu thích");
                } else {
                    Toast.makeText(context, "Playlist ID bị thiếu, không thể yêu thích!", Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (playlist.getPlaylistId() == null || playlist.getPlaylistId().equals("0")) {
                        Toast.makeText(context, "Playlist ID bị thiếu!", Toast.LENGTH_SHORT).show();
                    } else {
                        listener.onPlaylistClick(playlist);
                    }
                }
            });
        }
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAlbumName;
        private final ImageView ivAlbumCover, ivFavourite;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumName = itemView.findViewById(R.id.txt_album_name);
            ivAlbumCover = itemView.findViewById(R.id.img_album);
            ivFavourite = itemView.findViewById(R.id.icon_favourite_album);
        }

        public void bind(Album album) {
            if (album == null) return;

            tvAlbumName.setText(album.getAlbumName());
            Glide.with(context)
                    .load(album.getCoverUrl())
                    .placeholder(R.drawable.music)
                    .into(ivAlbumCover);

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

            checkFavoriteStatus(userId, album.getAlbumId(), "Albums", ivFavourite);

            ivFavourite.setOnClickListener(v -> {
                if (album.getAlbumId() != null && !album.getAlbumId().equals("0")) {
                    toggleFavorite(userId, album.getAlbumId(), "Albums", ivFavourite,
                            "Đã thêm album vào yêu thích", "Đã xóa album khỏi yêu thích");
                } else {
                    Toast.makeText(context, "Album ID bị thiếu, không thể yêu thích!", Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (album.getAlbumId() == null || album.getAlbumId().equals("0")) {
                        Toast.makeText(context, "Album ID bị thiếu!", Toast.LENGTH_SHORT).show();
                    } else {
                        listener.onAlbumClick(album);
                    }
                }
            });
        }
    }

    class ArtistViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvArtistName;
        private final ImageView ivArtistAvatar, ivFavourite;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArtistName = itemView.findViewById(R.id.txt_artist_name);
            ivArtistAvatar = itemView.findViewById(R.id.img_artist_avatar);
            ivFavourite = itemView.findViewById(R.id.icon_favourite_artist); // Assuming you have this ID in artist_item.xml
        }

        public void bind(Artist artist) {
            if (artist == null) return;

            tvArtistName.setText(artist.getArtistName());
            Glide.with(context)
                    .load(artist.getAvatarUrl())
                    .placeholder(R.drawable.music)
                    .circleCrop()
                    .into(ivArtistAvatar);

            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

            checkFavoriteStatus(userId, artist.getArtistId(), "Artists", ivFavourite);

            ivFavourite.setOnClickListener(v -> {
                if (artist.getArtistId() != null && !artist.getArtistId().equals("0")) {
                    toggleFavorite(userId, artist.getArtistId(), "Artists", ivFavourite,
                            "Đã thêm nghệ sĩ vào yêu thích", "Đã xóa nghệ sĩ khỏi yêu thích");
                } else {
                    Toast.makeText(context, "Artist ID bị thiếu, không thể yêu thích!", Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (artist.getArtistId() == null || artist.getArtistId().equals("0")) {
                        Toast.makeText(context, "Artist ID bị thiếu!", Toast.LENGTH_SHORT).show();
                    } else {
                        listener.onArtistClick(artist);
                    }
                }
            });
        }
    }

    public void updateData(List<Song> songs, List<Playlist> playlists, List<Album> albums, List<Artist> artists) {
        List<SearchItem> newItems = new ArrayList<>();
        if (songs != null) for (Song s : songs) newItems.add(new SearchItem.SongItem(s));
        if (playlists != null)
            for (Playlist p : playlists) newItems.add(new SearchItem.PlaylistItem(p));
        if (albums != null) for (Album a : albums) newItems.add(new SearchItem.AlbumItem(a));
        if (artists != null) for (Artist a : artists) newItems.add(new SearchItem.ArtistItem(a));
        updateItems(newItems);
    }
}