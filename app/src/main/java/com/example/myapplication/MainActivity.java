package com.example.myapplication;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.item_nav.SongsFragment;
import com.example.myapplication.item_nav.FavouriteFragment;
import com.example.myapplication.item_nav.HomeFragment;
import com.example.myapplication.item_nav.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

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
                    selectedFragment = new SongsFragment();
                } else if (item.getItemId() == R.id.menu_item_favourite) {
                    selectedFragment = new FavouriteFragment();
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
}