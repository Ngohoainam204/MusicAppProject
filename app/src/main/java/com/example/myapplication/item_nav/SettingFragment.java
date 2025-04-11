package com.example.myapplication.item_nav;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ChangePasswordActivity;
import com.example.myapplication.LoginFragment;
import com.example.myapplication.R;
import com.example.myapplication.RegisterLoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Map;

public class SettingFragment extends Fragment {

    private Button btnAddAccount, btnLogout, btnChangepw;
    private TextView tvUsername;

    public SettingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnAddAccount = view.findViewById(R.id.btnAddAccount);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangepw = view.findViewById(R.id.btnChangePassword);
        tvUsername = view.findViewById(R.id.tvUsername);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference userRef = rootRef.child(uid);

            // Di chuyển dữ liệu từ node "0" (nếu có)
            rootRef.child("0").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Map<String, Object> oldUserData = (Map<String, Object>) snapshot.getValue();
                        rootRef.child(uid).setValue(oldUserData).addOnSuccessListener(unused -> {
                            rootRef.child("0").removeValue();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Hiển thị username
            userRef.child("username").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String username = snapshot.getValue(String.class);
                    tvUsername.setText(username);
                }
            });

            // Nhấn vào username để sửa
            tvUsername.setOnClickListener(v -> {
                EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Nhập username mới");

                new AlertDialog.Builder(requireContext())
                        .setTitle("Sửa username")
                        .setView(input)
                        .setPositiveButton("Lưu", (dialog, which) -> {
                            String newUsername = input.getText().toString().trim();
                            if (!newUsername.isEmpty()) {
                                userRef.child("username").setValue(newUsername)
                                        .addOnSuccessListener(unused -> {
                                            tvUsername.setText(newUsername);
                                            Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(getContext(), "Vui lòng nhập username", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        // Thêm tài khoản khác
        btnAddAccount.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Đăng xuất
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent intent = new Intent(getActivity(), RegisterLoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });

        // Đổi mật khẩu
        btnChangepw.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });
    }
}
