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
import com.example.myapplication.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private List<Song> songs; // Changed to non-final for updateability
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";
    private static final String TAG = "SongAdapter";
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    public void setOnSongClickListener(OnSongClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        if (position < 0 || position >= songs.size()) {
            Log.e(TAG, "Invalid position in onBindViewHolder: " + position);
            return; // предотвращение  IndexOutOfBoundsException
        }
        Song song = songs.get(position);
        if (song == null) {
            Log.e(TAG, "Song is null at position: " + position);
            return;
        }

        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.music_note)
                .error(R.drawable.music_note)
                .into(holder.imgCover);

        updateFavoriteIcon(holder.ivFavourite, song.isFavourite()); // Set initial state

        holder.ivFavourite.setOnClickListener(v -> {
            handleFavoriteClick(holder, song);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSongClick(songs.get(adapterPosition), adapterPosition);
                }
            }
        });
    }

    private void handleFavoriteClick(SongViewHolder holder, Song song) {
        String email = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (email == null) {
            Toast.makeText(context, "Bạn cần đăng nhập để yêu thích bài hát", Toast.LENGTH_SHORT).show();
            return;
        }
        String encodedEmail = email.replace(".", "_");

        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("FavouritesSongs").child(encodedEmail).child(song.getSongId());

        int pos = holder.getAdapterPosition();
        if (pos == RecyclerView.NO_POSITION) {
            Log.w(TAG, "handleFavoriteClick: RecyclerView position is not valid");
            return;
        }

        if (song.isFavourite()) {
            // Remove from favorites
            favRef.removeValue().addOnSuccessListener(aVoid -> {
                song.setFavourite(false); // Update the song object
                updateFavoriteIcon(holder.ivFavourite, false); // Update the icon
                notifyItemChanged(pos); // Notify that the item has changed
                Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to remove from favorites: " + e.getMessage());
                Toast.makeText(context, "Lỗi xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Add to favorites
            favRef.setValue(true).addOnSuccessListener(aVoid -> {
                song.setFavourite(true);
                updateFavoriteIcon(holder.ivFavourite, true);
                notifyItemChanged(pos);
                Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to add to favorites: " + e.getMessage());
                Toast.makeText(context, "Lỗi thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateFavoriteIcon(ImageView ivFavourite, boolean isFavorite) {
        ivFavourite.setImageResource(isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist;
        ImageView imgCover, ivFavourite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.tv_song_title);
            txtArtist = itemView.findViewById(R.id.tv_song_artist);
            imgCover = itemView.findViewById(R.id.img_song_cover);
            ivFavourite = itemView.findViewById(R.id.ivFavourite);
        }
    }
}