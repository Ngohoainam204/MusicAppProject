package com.example.myapplication.item_nav;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.adapters.BannerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerBanner;
    private FirebaseDatabase database;
    private DatabaseReference bannersRef;
    private List<String> bannerUrls;
    private BannerAdapter bannerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance("https://musicplayerapp-aed33-default-rtdb.asia-southeast1.firebasedatabase.app");
        bannersRef = database.getReference("banner/featured_songs");

        // Initialize ViewPager2
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);
        bannerUrls = new ArrayList<>();

        // Initialize the BannerAdapter
        bannerAdapter = new BannerAdapter(getContext(), bannerUrls);
        viewPagerBanner.setAdapter(bannerAdapter);

        // Get banner URLs from Firebase
        bannersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bannerUrls.clear();  // Clear the list before adding new data

                // Loop through the "featured_songs" and get the banner URLs
                for (DataSnapshot bannerSnapshot : dataSnapshot.getChildren()) {
                    String bannerUrl = bannerSnapshot.child("coverUrl").getValue(String.class);
                    if (bannerUrl != null) {
                        bannerUrls.add(bannerUrl);
                    }
                }

                // Notify the adapter that the data has changed
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        return view;
    }
}
