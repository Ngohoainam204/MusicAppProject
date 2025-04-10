package com.example.myapplication.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.library.FavouriteAlbumsFragment;
import com.example.myapplication.library.FavouriteAritistsFragment;
import com.example.myapplication.library.FavouriteSongsFragment;
import com.example.myapplication.library.FavouritePlaylistsFragment;

public class LibraryPagerAdapter extends FragmentStateAdapter {

    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FavouritePlaylistsFragment();
            case 1:
                return new FavouriteAritistsFragment();
            case 2:
                return new FavouriteAlbumsFragment();
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
