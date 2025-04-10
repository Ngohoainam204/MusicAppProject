package com.example.myapplication.item_nav;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.ChangePasswordActivity;
import com.example.myapplication.LoginFragment;
import com.example.myapplication.R;
import com.example.myapplication.StartedActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends Fragment {

    private Button btnAddAccount, btnLogout, btnChangepw;

    public SettingFragment() {
        // Required empty public constructor
    }

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

        // Sự kiện cho nút Add new account
        btnAddAccount.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogout.setOnClickListener(v -> {
            // Đăng xuất khỏi Firebase
            FirebaseAuth.getInstance().signOut();

            // Đăng xuất khỏi Google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)) // giống như cấu hình đăng nhập
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                // Mở lại GetStartedActivity
                Intent intent = new Intent(getActivity(), StartedActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish(); // Kết thúc Activity hiện tại
            });
        });
        btnChangepw.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

    }
}
