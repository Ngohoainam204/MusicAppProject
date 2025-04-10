package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword, editTextNewPassword;
    private Button btnAccept, btnCancel;
    private ImageView userIcon, togglePassword, togglePassword2;
    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password); // XML giao diện

        // Ánh xạ view
        editTextCurrentPassword = findViewById(R.id.current_password);
        editTextNewPassword = findViewById(R.id.new_password);
        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnCancel);
        userIcon = findViewById(R.id.user_icon);
        togglePassword = findViewById(R.id.togglePassword);
        togglePassword2 = findViewById(R.id.togglePassword2);

        // Xử lý nút Accept
        btnAccept.setOnClickListener(v -> changePassword());

        // Xử lý nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Sự kiện ẩn/hiện Current Password
        togglePassword.setOnClickListener(v -> {
            if (isCurrentPasswordVisible) {
                editTextCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.blind); // mắt đóng
            } else {
                editTextCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.visible); // mắt mở
            }
            editTextCurrentPassword.setSelection(editTextCurrentPassword.length());
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
        });

        // Sự kiện ẩn/hiện New Password
        togglePassword2.setOnClickListener(v -> {
            if (isNewPasswordVisible) {
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword2.setImageResource(R.drawable.blind);
            } else {
                editTextNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword2.setImageResource(R.drawable.visible);
            }
            editTextNewPassword.setSelection(editTextNewPassword.length());
            isNewPasswordVisible = !isNewPasswordVisible;
        });
    }

    private void changePassword() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String currentPass = editTextCurrentPassword.getText().toString().trim();
        String newPass = editTextNewPassword.getText().toString().trim();

        if (user != null && !currentPass.isEmpty() && !newPass.isEmpty()) {
            String email = user.getEmail();

            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPass);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPass)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(ChangePasswordActivity.this, "Password changed successfully. Please login again.", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().signOut();

                                            Intent intent = new Intent(ChangePasswordActivity.this, RegisterLoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(ChangePasswordActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        }
    }
}
