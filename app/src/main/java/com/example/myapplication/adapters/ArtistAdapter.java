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
import com.example.myapplication.models.Artist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private Context context;
    private List<Artist> artistList;
    private static final String DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OnArtistClickListener {
        void onArtistClick(Artist artist);
    }

    private OnArtistClickListener listener;

    public void setOnArtistClickListener(OnArtistClickListener listener) {
        this.listener = listener;
    }

    public ArtistAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_item, parent, false);
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        if (artist == null) return;

        holder.artistNameTextView.setText(artist.getArtistName());

        Glide.with(context)
                .load(artist.getAvatarUrl())
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.music_note)
                .into(holder.avatarImageView);

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_") : null;

        if (userId == null) {
            holder.ivFavourite.setOnClickListener(v ->
                    Toast.makeText(context, "Bạn cần đăng nhập để yêu thích nghệ sĩ", Toast.LENGTH_SHORT).show());
            return;
        }

        DatabaseReference favRef = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesArtists")
                .child(userId).child(artist.getArtistId());

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFav = snapshot.exists();
                updateFavoriteIcon(holder.ivFavourite, isFav);

                holder.ivFavourite.setOnClickListener(v -> toggleFavorite(artist, isFav, holder.ivFavourite));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ArtistAdapter", "Lỗi kiểm tra yêu thích nghệ sĩ: " + error.getMessage());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onArtistClick(artist);
        });
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);
    }

    private void toggleFavorite(Artist artist, boolean isFavorite, ImageView icon) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance(DB_URL).getReference("FavouritesArtists")
                .child(userId).child(artist.getArtistId());

        if (isFavorite) {
            ref.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Đã xóa khỏi nghệ sĩ yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteIcon(icon, false);
            });
        } else {
            ref.setValue(true).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Đã thêm vào nghệ sĩ yêu thích", Toast.LENGTH_SHORT).show();
                updateFavoriteIcon(icon, true);
            });
        }
    }

    @Override
    public int getItemCount() {
        return artistList != null ? artistList.size() : 0;
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImageView, ivFavourite;
        public TextView artistNameTextView;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.img_artist_avatar);
            artistNameTextView = itemView.findViewById(R.id.txt_artist_name);
            ivFavourite = itemView.findViewById(R.id.icon_artist_favourite); // bạn cần thêm trong layout
        }
    }

    public void updateArtists(List<Artist> newList) {
        this.artistList = newList;
        notifyDataSetChanged();
    }
}
