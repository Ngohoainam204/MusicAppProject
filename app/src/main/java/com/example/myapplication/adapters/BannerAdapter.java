package com.example.myapplication.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<String> bannerUrls;
    private RecyclerView recyclerView;
    private Handler handler = new Handler();
    private Runnable autoScrollRunnable;

    public BannerAdapter(Context context, List<String> bannerUrls) {
        this.context = context;
        this.bannerUrls = bannerUrls;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        if (recyclerView == null) {
            recyclerView = (RecyclerView) parent;
            startAutoScroll();
        }
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        String url = bannerUrls.get(position);
        String directUrl = convertGoogleDriveUrlToDirectImageUrl(url);
        Glide.with(context)
                .load(directUrl)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return bannerUrls.size();
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (recyclerView != null && bannerUrls.size() > 0) {
                    int nextItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() + 1;
                    if (nextItem >= bannerUrls.size()) {
                        nextItem = 0;
                    }
                    recyclerView.smoothScrollToPosition(nextItem);
                    handler.postDelayed(this, 2000);
                }
            }
        };
        handler.postDelayed(autoScrollRunnable, 2000);
    }

    public void stopAutoScroll() {
        handler.removeCallbacks(autoScrollRunnable);
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgBanner);
        }
    }

    private String convertGoogleDriveUrlToDirectImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        int idStart = url.indexOf("id=");
        if (idStart == -1) {
            return url;
        }
        String fileId = url.substring(idStart + 3);
        return "https://drive.google.com/uc?export=download&id=" + fileId;
    }
}
