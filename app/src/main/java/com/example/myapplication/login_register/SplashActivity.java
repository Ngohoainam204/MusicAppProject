package com.example.myapplication.login_register;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.main.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Bắt buộc phải có layout nếu dùng setContentView()

        // Logic kiểm tra đăng nhập
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Đã đăng nhập
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // Chưa đăng nhập
            startActivity(new Intent(SplashActivity.this, RegisterLoginActivity.class));
        }

        finish(); // kết thúc splash
    }
}