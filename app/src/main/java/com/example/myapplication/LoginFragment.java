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
import android.content.SharedPreferences;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {

    private AutoCompleteTextView editTextUsername;
    private EditText editTextPassword;
    private Button btnSignIn;
    private ImageView imgGoogleSignIn, togglePassword;
    private TextView btnRegisterNow;
    private CheckBox checkBoxRemember;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

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

        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnRegisterNow = view.findViewById(R.id.btnRegisterNow);
        checkBoxRemember = view.findViewById(R.id.checkBoxRemember);
        togglePassword = view.findViewById(R.id.togglePassword);
        imgGoogleSignIn = view.findViewById(R.id.imgGoogleSignIn);

        // Gợi ý email đã lưu
        SharedPreferences prefs = requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE);
        String savedEmail = prefs.getString("savedEmail", "");
        if (!savedEmail.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    new String[]{savedEmail});
            editTextUsername.setAdapter(adapter);
            editTextUsername.setThreshold(1);
            editTextUsername.setText(savedEmail);

            // Ẩn dropdown khi chọn
            editTextUsername.setOnItemClickListener((adapterView, view1, i, l) -> {
                editTextUsername.dismissDropDown();
            });
        }

        // Đăng nhập thường
        btnSignIn.setOnClickListener(v -> loginUser());

        // Đăng ký
        btnRegisterNow.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Hiện / Ẩn mật khẩu
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

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Đảm bảo bạn đã thêm client_id vào strings.xml
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        imgGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
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

        btnSignIn.setEnabled(false);
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(getContext());
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        long startTime = System.currentTimeMillis();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    long duration = System.currentTimeMillis() - startTime;
                    progressDialog.dismiss();
                    btnSignIn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login successful (" + duration + " ms)", Toast.LENGTH_SHORT).show();

                        // Lưu email nếu được chọn
                        if (checkBoxRemember.isChecked()) {
                            requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE)
                                    .edit().putString("savedEmail", email).apply();
                        }

                        openMainActivity();
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(getContext(), "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Google sign in successful", Toast.LENGTH_SHORT).show();
                        openMainActivity();
                    } else {
                        Toast.makeText(getActivity(), "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
