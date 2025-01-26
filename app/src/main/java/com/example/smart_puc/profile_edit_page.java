package com.example.smart_puc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class profile_edit_page extends AppCompatActivity {

    private EditText usernameInput, pucIdInput, phoneInput;
    private Button saveButton;
    private DatabaseReference databaseReference;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_page);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            getSupportActionBar().setTitle("Smart PUC");
        }

        // Initialize UI components
        usernameInput = findViewById(R.id.username_input);
        pucIdInput = findViewById(R.id.puc_id_input);
        phoneInput = findViewById(R.id.phone_input);
        saveButton = findViewById(R.id.save_button);

        // Get current user's email from FirebaseAuth
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Use the email as the unique user ID (replace special characters if necessary)
        String userId = "User_" + currentUserEmail.replace(".", ""); // Change '.' to '' to avoid issues in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Profile").child(userId);

        loadUserData();

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve user data from the snapshot
                    String username = dataSnapshot.child("username").getValue(String.class);
                    String pucId = dataSnapshot.child("pucId").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Update profile UI
                    usernameInput.setText(username);
                    pucIdInput.setText(pucId);
                    phoneInput.setText(phone);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(profile_edit_page.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();
        String pucId = pucIdInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Save to Firebase
        databaseReference.child("username").setValue(username);
        databaseReference.child("pucId").setValue(pucId);
        databaseReference.child("phone").setValue(phone);
        databaseReference.child("email").setValue(currentUserEmail) // Save email as well
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(profile_edit_page.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after saving
                    } else {
                        Toast.makeText(profile_edit_page.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item clicks
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close the activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}