package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
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
import com.example.myapplication.models.Album;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final Context context;
    private List<Album> albumList;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    private OnAlbumClickListener listener;

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.listener = listener;
    }

    public AlbumAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);
        if (album == null) return;

        holder.albumNameTextView.setText(album.getAlbumName());
        holder.artistTextView.setText("Album - " + album.getArtist());

        Glide.with(context)
                .load(album.getCoverUrl())
                .placeholder(R.drawable.ic_home)
                .error(R.drawable.music)
                .into(holder.coverImageView);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

        if (userId == null) {
            holder.ivFavourite.setOnClickListener(v ->
                    Toast.makeText(context, "Bạn cần đăng nhập để yêu thích album", Toast.LENGTH_SHORT).show());
            return;
        }

        DatabaseReference favRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesAlbums")
                .child(userId).child(album.getAlbumId());

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFav = snapshot.exists();
                updateFavoriteIcon(holder.ivFavourite, isFav);

                holder.ivFavourite.setOnClickListener(v -> toggleFavorite(album, isFav, holder.ivFavourite));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AlbumAdapter", "Lỗi kiểm tra yêu thích album: " + error.getMessage());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAlbumClick(album);
        });
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);
    }

    private void toggleFavorite(Album album, boolean isFavorite, ImageView icon) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesAlbums")
                .child(userId).child(album.getAlbumId());

        if (isFavorite) {
            ref.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Đã xóa khỏi album yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteIcon(icon, false);
            });
        } else {
            ref.setValue(true).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Đã thêm vào album yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteIcon(icon, true);
            });
        }
    }

    @Override
    public int getItemCount() {
        return albumList != null ? albumList.size() : 0;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImageView, ivFavourite;
        public TextView albumNameTextView;
        public TextView artistTextView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.img_album);
            albumNameTextView = itemView.findViewById(R.id.txt_album_name);
            artistTextView = itemView.findViewById(R.id.txt_album_artist_name);
            ivFavourite = itemView.findViewById(R.id.icon_album_favourite);
        }
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albumList = newAlbums;
        notifyDataSetChanged();
    }
}
