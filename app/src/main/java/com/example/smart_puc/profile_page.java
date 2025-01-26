package com.example.smart_puc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class profile_page extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private TextView usernameTextView, emailTextView, pucIdTextView, phoneTextView;
    private Button logoutButton, editButton;

    private String email, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Profile");

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Smart PUC");
        }

        // Get references to the views
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        pucIdTextView = findViewById(R.id.puc_id);
        phoneTextView = findViewById(R.id.phone);
        logoutButton = findViewById(R.id.logout_button);
        editButton = findViewById(R.id.edit_button);

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            email = currentUser.getEmail(); // Assign email here
            // Use email to generate the userId, replacing '.' for Firebase compatibility
            userId = "User_" + email.replace(".", ""); // Make sure this matches the format used in profile_edit_page

            emailTextView.setText("Email: " + email);

            // Load existing profile data
            loadProfileData(userId);
        } else {
            // If user is not logged in, navigate to login page
            Intent intent = new Intent(profile_page.this, login_page.class);
            startActivity(intent);
            finish();
        }

        // Edit button functionality (navigate to EditProfilePage)
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(profile_page.this, profile_edit_page.class);
            startActivity(intent);
        });

        // Logout button functionality
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(profile_page.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(profile_page.this, login_page.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // Fetch profile data from Firebase
    private void loadProfileData(String userId) {
        DatabaseReference userRef = databaseReference.child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get user details and update the UI
                    String username = snapshot.child("username").getValue(String.class);
                    String pucId = snapshot.child("pucId").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);

                    usernameTextView.setText("Username: " + username);
                    pucIdTextView.setText("PUC ID: " + pucId);
                    phoneTextView.setText("Phone: " + phone);
                } else {
                    Toast.makeText(profile_page.this, "No profile data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile_page.this, "Error loading profile data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload the profile data when the activity is resumed (e.g., after returning from the edit page)
        if (userId != null) {
            loadProfileData(userId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(profile_page.this, home_activity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}