// login_page.java
package com.example.smart_puc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;  // Added to get input from EditText fields
import android.widget.Toast;     // Added to show error messages
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login_page extends AppCompatActivity {

    private FirebaseAuth mAuth;  // Firebase Authentication instance
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        Button signupButton = findViewById(R.id.signup_button);
        signupButton.setOnClickListener(v -> {
            Intent intent = new Intent(login_page.this, signup_page.class);
            startActivity(intent);
        });

        Button loginButton = findViewById(R.id.go_to_login_button);
        loginButton.setOnClickListener(v -> {
            String inputEmail = emailEditText.getText().toString();
            String inputPassword = passwordEditText.getText().toString();

            // Firebase login logic
            mAuth.signInWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener(login_page.this, task -> {
                        if (task.isSuccessful()) {
                            // Login success, navigate to home activity
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(login_page.this, home_activity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Login failed, show a message
                            Toast.makeText(login_page.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
