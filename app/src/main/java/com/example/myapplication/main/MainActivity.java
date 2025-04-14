package com.example.myapplication.main;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.item_nav.Search_Fragment;
import com.example.myapplication.item_nav.LibraryFragment;
import com.example.myapplication.item_nav.HomeFragment;
import com.example.myapplication.item_nav.SettingFragment;
import com.example.myapplication.library.FavouriteSongsFragment; // Import FavouriteSongsFragment
import com.example.myapplication.models.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements FavouriteSongsFragment.OnSongPlayClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        // Load HomeFragment as default
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.menu_item_home) {
                    selectedFragment = new HomeFragment();
                } else if (item.getItemId() == R.id.menu_item_song) {
                    selectedFragment = new Search_Fragment();
                } else if (item.getItemId() == R.id.menu_item_favourite) {
                    selectedFragment = new LibraryFragment();
                } else if (item.getItemId() == R.id.menu_item_setting) {
                    selectedFragment = new SettingFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
                return true;
            }
        });
    }

    @Override
    public void onSongPlayClicked(Song song) {
        // Logic để phát bài hát 'song' khi nó được click từ FavouriteSongsFragment
        Log.d("MainActivity", "Play song from Favourite: " + song.getTitle() + ", URL: " + song.getFileUrl());
        // TODO: Thực hiện logic phát nhạc ở đây.
        // Ví dụ: Gọi một service phát nhạc và truyền URL của bài hát.
    }
}