package com.example.smart_puc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;  // Firebase Authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Delay for 3 seconds before navigating
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is already signed in
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is signed in, navigate to HomeActivity
                    Intent intent = new Intent(MainActivity.this, home_activity.class);
                    startActivity(intent);
                } else {
                    // No user is signed in, navigate to LoginActivity
                    Intent intent = new Intent(MainActivity.this, login_page.class);
                    startActivity(intent);
                }
                finish(); // Close MainActivity
            }
        }, 3000); // 3 seconds delay
    }
}
