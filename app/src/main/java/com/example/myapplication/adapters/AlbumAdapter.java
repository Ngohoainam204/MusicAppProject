package com.example.myapplication.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.myapplication.models.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private Context context;
    private List<Album> albumList;

    public AlbumAdapter(Context context, List<Album> albumList) {
        this.context = context;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false); // Tạo layout cho mỗi item album
        return new AlbumViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albumList.get(position);

        // ✅ Thêm log để kiểm tra dữ liệu
        Log.d("AlbumAdapter", "onBindViewHolder: name=" + album.getAlbumName() + ", artist=" + album.getArtist());

        holder.albumNameTextView.setText(album.getAlbumName());
        holder.artistTextView.setText("Album - " + album.getArtist());

        if (album.getCoverUrl() != null) {
            Glide.with(context)
                    .load(album.getCoverUrl())
                    .placeholder(R.drawable.ic_home)
                    .error(R.drawable.music)
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setImageResource(R.drawable.ic_home);
        }
    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImageView;
        public TextView albumNameTextView;
        public TextView artistTextView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.img_album);
            albumNameTextView = itemView.findViewById(R.id.txt_album_name);
            artistTextView = itemView.findViewById(R.id.txt_album_artist_name);
        }
    }
}