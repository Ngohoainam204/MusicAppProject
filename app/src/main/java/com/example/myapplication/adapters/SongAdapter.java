package com.example.myapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Song;
import com.example.myapplication.nowplaying.NowPlayingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final List<Song> songList;

    private static final String FIREBASE_DB_URL = "https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app";

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(song.getDuration());

        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.ic_pause)
                .error(R.drawable.ic_pause)
                .centerCrop()
                .into(holder.imgSong);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NowPlayingActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("artist_name", song.getArtist());
            intent.putExtra("cover_url", song.getCoverUrl());
            intent.putExtra("song_url", song.getFileUrl());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_lyrics", song.getLyrics());
            intent.putExtra("song_id", song.getSongId());
            context.startActivity(intent);
        });

        updateFavouriteIcon(holder.imgFavorite, song.getSongId());

        holder.imgFavorite.setOnClickListener(v -> {
            toggleFavourite(song.getSongId(), holder.imgFavorite);
        });
    }

    @Override
    public int getItemCount() {
        return songList != null ? songList.size() : 0;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView imgSong, imgFavorite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDuration = itemView.findViewById(R.id.tv_song_duration);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_song_artist);
            imgSong = itemView.findViewById(R.id.img_song_cover);
            imgFavorite = itemView.findViewById(R.id.ivFavourite);
        }
    }

    private String encodeEmail(String email) {
        return email.replace(".", "_");
    }

    private void updateFavouriteIcon(ImageView imgFavorite, String songId) {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String encodedEmail = encodeEmail(email);
        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("Favourites").child(encodedEmail).child(songId);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFav = snapshot.exists();
                imgFavorite.setImageResource(isFav ? R.drawable.ic_heart_selection_true : R.drawable.ic_heart_selection);
                imgFavorite.setColorFilter(isFav ? Color.RED : Color.GRAY);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void toggleFavourite(String songId, ImageView imgFavorite) {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email == null) return;
        String encodedEmail = encodeEmail(email);
        DatabaseReference favRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
                .getReference("Favourites").child(encodedEmail).child(songId);

        favRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    favRef.removeValue().addOnCompleteListener(task -> {
                        updateFavouriteIcon(imgFavorite, songId);
                    });
                } else {
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    favRef.child("songId").setValue(songId);
                    favRef.child("addedDate").setValue(date).addOnCompleteListener(task -> {
                        updateFavouriteIcon(imgFavorite, songId);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
