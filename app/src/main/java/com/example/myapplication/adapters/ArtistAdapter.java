package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Artist;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder> {

    private Context context;
    private List<Artist> artistList;

    public ArtistAdapter(Context context, List<Artist> artistList) {
        this.context = context;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist_item, parent, false); // Tạo layout cho mỗi item artist
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artistList.get(position);
        holder.artistNameTextView.setText(artist.getArtistName());

        if (artist.getAvatarUrl() != null && !artist.getAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(artist.getAvatarUrl())
                    .circleCrop() // Biến ảnh thành hình tròn
                    .placeholder(R.drawable.ic_profile) // Ảnh chờ
                    .error(R.drawable.music_note)       // Ảnh lỗi
                    .into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.ic_profile); // Ảnh mặc định
        }

        holder.itemView.setOnClickListener(v -> {
            // Xử lý khi artist được click
        });
    }


    @Override
    public int getItemCount() {
        return artistList.size();
    }

    public static class ArtistViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImageView;
        public TextView artistNameTextView;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.img_artist_avatar);
            artistNameTextView = itemView.findViewById(R.id.txt_artist_name);
            // Không cần ánh xạ bioTextView nữa
        }
    }
}