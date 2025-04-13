package com.example.myapplication.item_nav;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.login_register.ChangePasswordActivity;
import com.example.myapplication.login_register.RegisterLoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Map;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";

    private Button btnLogout, btnChangepw;
    private TextView tvUsername;
    private DatabaseReference userRef;

    public SettingFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangepw = view.findViewById(R.id.btnChangePassword);
        tvUsername = view.findViewById(R.id.tvUsername);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            Log.d(TAG, "Current UID: " + uid);

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Users");
            userRef = rootRef.child(uid);

            // Di chuyển dữ liệu từ node "0" (nếu có)
            rootRef.child("0").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Map<String, Object> oldUserData = (Map<String, Object>) snapshot.getValue();
                        rootRef.child(uid).setValue(oldUserData).addOnSuccessListener(unused -> {
                            rootRef.child("0").removeValue();
                            Log.d(TAG, "Moved data from node 0 to user UID: " + uid);
                        }).addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to move data from node 0: " + e.getMessage());
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Data move cancelled: " + error.getMessage());
                }
            });

            // Nhấn vào username để sửa
            tvUsername.setOnClickListener(v -> {
                EditText input = new EditText(requireContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Enter new username");

                new AlertDialog.Builder(requireContext())
                        .setTitle("Edit username")
                        .setView(input)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String newUsername = input.getText().toString().trim();
                            if (!newUsername.isEmpty()) {
                                userRef.child("username").setValue(newUsername)
                                        .addOnSuccessListener(unused -> {
                                            // Sau khi cập nhật Firebase, load lại username từ Firebase
                                            loadUsername(); // ✅ Reload từ Firebase để đồng bộ
                                            Toast.makeText(getContext(), "Update successful!", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "Username updated in Firebase to: " + newUsername);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "Failed to update username: " + e.getMessage());
                                        });
                            } else {
                                Toast.makeText(getContext(), "Please enter username", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

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

    @Override
    public void onResume() {
        super.onResume();
        loadUsername(); // ✅ Mỗi lần quay lại Fragment thì load lại tên từ Firebase
    }

    private void loadUsername() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.child("username").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String username = snapshot.getValue(String.class);
                    tvUsername.setText(username);
                    Log.d(TAG, "Username refreshed: " + username);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error refreshing username: " + e.getMessage());
            });
        }
    }
}
