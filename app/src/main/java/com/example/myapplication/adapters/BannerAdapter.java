package com.example.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<String> bannerUrls;

    public BannerAdapter(Context context, List<String> bannerUrls) {
        this.context = context;
        this.bannerUrls = bannerUrls;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
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

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgBanner);
        }
    }

    // Phương thức này chuyển đổi URL Google Drive sang URL tải trực tiếp
    private String convertGoogleDriveUrlToDirectImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Kiểm tra URL có chứa "id=" hay không
        int idStart = url.indexOf("id=");
        if (idStart == -1) {
            return url;  // Trả về URL gốc nếu không tìm thấy id=
        }

        // Trích xuất ID của tệp từ URL
        String fileId = url.substring(idStart + 3);
        return "https://drive.google.com/uc?export=download&id=" + fileId;
    }
}
