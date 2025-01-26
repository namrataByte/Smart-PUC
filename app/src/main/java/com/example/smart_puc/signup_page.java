package com.example.smart_puc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class signup_page extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button signupButton;
    private Button goToLoginButton;

    private FirebaseAuth mAuth;  // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signupButton = findViewById(R.id.signup_button);
        goToLoginButton = findViewById(R.id.go_to_login_button);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();

                if (password.equals(confirmPassword)) {
                    // Firebase signup logic
                    mAuth.createUserWithEmailAndPassword(username, password)
                            .addOnCompleteListener(signup_page.this, task -> {
                                if (task.isSuccessful()) {
                                    // Sign up success, redirect to HomeActivity
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(signup_page.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(signup_page.this, home_activity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Sign up failed, display a message
                                    Toast.makeText(signup_page.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(signup_page.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(signup_page.this, login_page.class);
            startActivity(intent);
            finish();
        });
    }
}
