package com.example.myapplication.adapters;

import android.content.Context;
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
    private final List<Song> songs;
    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.music_note)
                .error(R.drawable.music_note)
                .into(holder.imgCover);

        holder.ivFavourite.setImageResource(song.isFavourite() ?
                R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);

        holder.ivFavourite.setOnClickListener(v -> {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if (email == null) return;
            String encodedEmail = email.replace(".", "_");

            DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                    .getReference("FavouritesSongs").child(encodedEmail).child(song.getSongId());

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (song.isFavourite()) {
                // Xóa khỏi Firebase và danh sách
                favRef.removeValue().addOnSuccessListener(aVoid -> {
                    songs.remove(pos);
                    notifyItemRemoved(pos);
                    Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Thêm vào yêu thích
                favRef.setValue(true).addOnSuccessListener(aVoid -> {
                    song.setFavourite(true);
                    notifyItemChanged(pos);
                    Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
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
