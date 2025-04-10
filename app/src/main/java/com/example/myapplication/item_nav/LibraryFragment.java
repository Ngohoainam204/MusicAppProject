package com.example.myapplication.item_nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.LibraryPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class LibraryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        tabLayout = view.findViewById(R.id.tabLayoutLibrary);
        viewPager = view.findViewById(R.id.viewPagerLibrary);

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

        // 👉 Thêm logic ẩn/hiện thanh tìm kiếm
        View searchBar = view.findViewById(R.id.searchBar);
        View btnSearchIcon = view.findViewById(R.id.btnSearchIcon);

        btnSearchIcon.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
            } else {
                searchBar.setVisibility(View.GONE);
            }
        });

        return view;
    }

}
