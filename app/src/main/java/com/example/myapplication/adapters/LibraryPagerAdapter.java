package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.library.AlbumsFragment;
import com.example.myapplication.library.ArtistsFragment;
import com.example.myapplication.library.FavouriteSongsFragment;
import com.example.myapplication.library.PlaylistsFragment;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PlaylistsFragment();
            case 1:
                return new ArtistsFragment();
            case 2:
                return new AlbumsFragment();
            case 3:
                return new FavouriteSongsFragment(); // Đây là tab "Songs"
            default:
                return new FavouriteSongsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
