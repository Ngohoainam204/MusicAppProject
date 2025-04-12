package com.example.myapplication.login_register;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;

public class RegisterLoginActivity extends AppCompatActivity {

    private Button btnRegister, btnLogin;
    private View mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        // Ánh xạ view
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnSignIn);
        mainContent = findViewById(R.id.mainContent);

        // Hiển thị RegisterFragment mặc định
        if (savedInstanceState == null) {
            showFragment(new RegisterFragment());
        }

        // Sự kiện nút Register
        btnRegister.setOnClickListener(v -> showFragment(new RegisterFragment()));

        // Sự kiện nút Login
        btnLogin.setOnClickListener(v -> showFragment(new LoginFragment()));

        // Xử lý nút Back theo cách mới
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Khi quay về lần cuối, hiện lại giao diện ban đầu
                    findViewById(R.id.fragment_container).setVisibility(View.GONE);
                    mainContent.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().popBackStack();
                }
            }
        });
    }

    // Hàm thay thế fragment
    private void showFragment(Fragment fragment) {
        mainContent.setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
