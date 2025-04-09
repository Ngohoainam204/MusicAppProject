package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private EditText editTextUsername, editTextPassword;
    private Button btnSignIn;
    private ImageView btnGoogleSignIn, togglePassword;
    private TextView btnRegisterNow;
    private CheckBox checkBoxRemember;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ UI
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnRegisterNow = view.findViewById(R.id.btnRegisterNow);
        checkBoxRemember = view.findViewById(R.id.checkBoxRemember);
        togglePassword = view.findViewById(R.id.togglePassword);

        // Xử lý sự kiện đăng nhập
        btnSignIn.setOnClickListener(v -> loginUser());

        // Xử lý sự kiện đăng ký
        btnRegisterNow.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Xử lý hiển thị/ẩn mật khẩu
        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.blind);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.visible);
            }
            isPasswordVisible = !isPasswordVisible;
            editTextPassword.setSelection(editTextPassword.getText().length());
        });
    }

    private void loginUser() {
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextUsername.setError("Enter your email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Enter your password");
            return;
        }

        // Ngăn người dùng nhấn liên tục
        btnSignIn.setEnabled(false);

        // Hiển thị loading
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Đo thời gian đăng nhập
        long startTime = System.currentTimeMillis();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    long duration = System.currentTimeMillis() - startTime;
                    progressDialog.dismiss();
                    btnSignIn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login successful (" + duration + " ms)", Toast.LENGTH_SHORT).show();

                        // Nếu chọn Remember, lưu email lại
                        if (checkBoxRemember.isChecked()) {
                            getActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE)
                                    .edit().putString("savedEmail", email).apply();
                        }

                        // Chuyển sang MainActivity
                        if (getActivity() != null) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


}
