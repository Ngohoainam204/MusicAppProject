package com.example.myapplication.item_nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.LibraryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LibraryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvUsername;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        tabLayout = view.findViewById(R.id.tabLayoutLibrary);
        viewPager = view.findViewById(R.id.viewPagerLibrary);
        tvUsername = view.findViewById(R.id.tvUsername);

        // Load dữ liệu username từ Firebase
        loadUsername();

        LibraryPagerAdapter adapter = new LibraryPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Playlists");
                            break;
                        case 1:
                            tab.setText("Artists");
                            break;
                        case 2:
                            tab.setText("Albums");
                            break;
                        case 3:
                            tab.setText("Songs");
                            break;
                    }
                }).attach();

        // Ẩn/hiện thanh tìm kiếm
        View searchBar = view.findViewById(R.id.searchBar);
        ImageView btnSearchIcon = view.findViewById(R.id.btnSearchIcon);

        btnSearchIcon.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
            } else {
                searchBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

    private void loadUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users").child(uid).child("username");

            userRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String username = snapshot.getValue(String.class);
                    tvUsername.setText("Xin chào, " + username);
                } else {
                    tvUsername.setText("Xin chào, khách");
                }
            }).addOnFailureListener(e -> {
                tvUsername.setText("Xin chào, lỗi tải tên");
                Toast.makeText(getContext(), "Lỗi khi tải username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            tvUsername.setText("Xin chào, chưa đăng nhập");
        }
    }
}
