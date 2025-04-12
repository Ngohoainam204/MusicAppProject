// RegisterLoginActivity.java
package com.example.myapplication.login_register;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;

public class RegisterLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnSignIn);

        // Nếu savedInstanceState == null thì sẽ thay fragment mặc định là RegisterFragment
        if (savedInstanceState == null) {
            replaceFragment(new RegisterFragment());
        }

        // Khi nhấn nút đăng ký
        btnRegister.setOnClickListener(v -> replaceFragment(new RegisterFragment()));

        // Khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(v -> replaceFragment(new LoginFragment()));
    }

    // Hàm thay thế fragment
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
