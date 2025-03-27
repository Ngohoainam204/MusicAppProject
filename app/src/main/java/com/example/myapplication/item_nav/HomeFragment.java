package com.example.myapplication.item_nav;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.SongAdapter;
import com.example.myapplication.models.Song;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String ARG_ACCESS_TOKEN = "access_token";
    private String accessToken;
    private RecyclerView recyclerView; // Khai báo recyclerView ở đây
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();

    public static HomeFragment newInstance(String accessToken) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ACCESS_TOKEN, accessToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            accessToken = getArguments().getString(ARG_ACCESS_TOKEN);
            Log.d("HomeFragment", "Received Access Token: " + accessToken);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);



        fetchRecentSongs();


        return view;
    }

    private void fetchRecentSongs() {
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Access token is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đây chỉ là giả lập gọi API, bạn cần thêm Retrofit hoặc thư viện HTTP

        // Sử dụng recyclerView ở đây để cập nhật adapter
        if (recyclerView != null && songAdapter != null) {
            songAdapter.notifyDataSetChanged();
        }
    }


    // Ví dụ về một phương thức khác có thể sử dụng recyclerView
    private void someOtherMethod() {
        if (recyclerView != null) {
            // Bạn có thể sử dụng recyclerView ở đây
            // Ví dụ: recyclerView.smoothScrollToPosition(0);
        }
    }
}