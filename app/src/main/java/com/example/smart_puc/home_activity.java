package com.example.smart_puc;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.content.pm.PackageManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.graphics.Color;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
public class home_activity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private double lastNotifiedEmissions = -1;
    private long lastNotificationTime = 0;
    private Double mostRecentEmissions = null;

    private ListView listViewEmissions;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private List<String> emissionsList;
    private BarChart barChart;
    private List<String> dateLabels = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("carbon_emissions");

        // Find the ListView and LineChart in the layout
        listViewEmissions = findViewById(R.id.listViewEmissions);
        barChart = findViewById(R.id.barChart);


        // Initialize the list and adapter
        emissionsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, emissionsList);
        listViewEmissions.setAdapter(adapter);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Smart PUC");

        // Initialize the Floating Action Button for the profile
        FloatingActionButton profileFab = findViewById(R.id.profile_fab);
        profileFab.setOnClickListener(v -> {
            // Navigate to Profile Page
            Intent intent = new Intent(home_activity.this, profile_page.class);
            startActivity(intent);
        });

        // Request notification permission
        requestNotificationPermission();

        // Read data from Firebase
        readDataFromFirebase();

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void readDataFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    emissionsList.clear(); // Clear the list before adding new data
                    List<BarEntry> barEntriesAbove600 = new ArrayList<>(); // For emissions above 600
                    List<BarEntry> barEntriesBelow600 = new ArrayList<>(); // For emissions below 600
                    dateLabels.clear(); // Clear old date labels
                    int index = 0; // X-axis index for the graph

                    String mostRecentDate = null; // Track the most recent date

                    for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        String location = dateSnapshot.child("location").getValue(String.class);
                        Double emissions = dateSnapshot.child("emissions_g").getValue(Double.class);

                        if (emissions != null) {
                            // Update most recent emissions
                            updateMostRecentEmissions(emissions);
                            // Add the date to the dateLabels list
                            dateLabels.add(date);  // Add the date for X-axis

                            // Update the most recent date
                            if (mostRecentDate == null || date.compareTo(mostRecentDate) > 0) {
                                mostRecentDate = date;
                            }

                            // Format the data and add it to the list
                            String entry = String.format("Date: %s\nLocation: %s\nEmissions (g): %.2f", date, location, emissions);
                            emissionsList.add(entry);

                            // Add the data to the graph
                            if (emissions > 600) {
                                barEntriesAbove600.add(new BarEntry(index, emissions.floatValue()));
                            } else {
                                barEntriesBelow600.add(new BarEntry(index, emissions.floatValue()));
                            }
                            index++; // Increment the index for the next data point
                        }
                    }

                    // Check if the most recent emissions exceed the threshold
                    if (mostRecentEmissions != null) {
                        sendNotificationIfNeeded(mostRecentEmissions); // Trigger a notification if necessary
                    }

                    // Update the ListView adapter
                    adapter.notifyDataSetChanged();

                    // Update the BarChart with the new data
                    updateBarChart(barEntriesAbove600, barEntriesBelow600);

                } else {
                    emissionsList.clear(); // Clear the list if no data is available
                    emissionsList.add("No data available.");
                    adapter.notifyDataSetChanged(); // Notify the adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                emissionsList.clear(); // Clear the list on error
                emissionsList.add("Error loading data: " + databaseError.getMessage());
                adapter.notifyDataSetChanged(); // Notify the adapter
                // Log the error for debugging
                Log.e("HomeActivity", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public class DateValueFormatter extends ValueFormatter {
        private final List<String> dateLabels;

        public DateValueFormatter(List<String> dateLabels) {
            this.dateLabels = dateLabels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < dateLabels.size()) {
                return dateLabels.get(index); // Return the corresponding date label
            }
            return ""; // Fallback if the index is out of bounds
        }
    }


    // Function to update the BarChart with the data points
    private void updateBarChart(List<BarEntry> barEntriesAbove600, List<BarEntry> barEntriesBelow600) {
        // Create BarDataSets from the entries (data points)
        BarDataSet barDataSetAbove600 = new BarDataSet(barEntriesAbove600, "Not Allowed > 600g");
        barDataSetAbove600.setColor(Color.RED); // Set bar color for above 600
        barDataSetAbove600.setValueTextColor(Color.RED); // Set value text color for above 600

        BarDataSet barDataSetBelow600 = new BarDataSet(barEntriesBelow600, "Allowed < 600g");
        barDataSetBelow600.setColor(Color.GREEN); // Set bar color for below 600
        barDataSetBelow600.setValueTextColor(Color.GREEN); // Set value text color for below 600

        // Create a dataset collection
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSetAbove600);
        dataSets.add(barDataSetBelow600);

        // Create BarData object
        BarData barData = new BarData(dataSets);
        barData.setBarWidth(0.45f); // Set the bar width (optional for customizing look)

        // Set the data in the chart
        barChart.setData(barData);

        // Set the maximum number of visible entries (e.g., 5 bars visible at a time)
        barChart.setVisibleXRangeMaximum(5);

        // Enable dragging and scaling on X-axis
        barChart.setDragEnabled(true);  // Enable dragging
        barChart.setScaleEnabled(true); // Enable scaling

        // Customize X-Axis to display dates
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter(dateLabels)); // Set the custom date formatter
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // One unit interval for X-axis
        xAxis.setGranularityEnabled(true);

        // Customize Y-Axis
        barChart.getAxisRight().setEnabled(false); // Disable the right Y-axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f); // One unit interval for Y-axis

        // Refresh chart
        barChart.invalidate(); // Refresh the chart with new data
    }


    // Refactored method to send a notification, works for both FCM and in-app usage
    public void sendNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "emission_notifications";

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Emission Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an intent that will be fired when the notification is clicked
        Intent intent = new Intent(this, home_activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.smart_puc_alert_icon)  // Use your notification icon here
                .setContentTitle("Emission Alert")
                .setContentText(message)  // Use the passed message as the content
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Show the notification (notificationId is 1 to avoid overwriting)
        notificationManager.notify(1, notificationBuilder.build());
    }

    public class FCMService extends FirebaseMessagingService {
        @Override
        public void onMessageReceived(RemoteMessage remoteMessage) {
            // Handle FCM messages here.
            if (remoteMessage.getNotification() != null) {
                // Handle notification payload
                String messageBody = remoteMessage.getNotification().getBody();
                sendNotification(messageBody);  // Reuse the common sendNotification method
            }
        }
    }

    private void sendNotificationIfNeeded(Double emissions) {
        long currentTime = System.currentTimeMillis();

        if (emissions > 600 && (lastNotifiedEmissions != emissions || (currentTime - lastNotificationTime) > 3600000)) {
            sendNotification("Warning: Today's emissions exceeded threshold! Current: " + emissions);
            lastNotifiedEmissions = emissions; // Update to the new notified emissions
            lastNotificationTime = currentTime; // Update last notification time
        }
    }

    private void updateMostRecentEmissions(Double newEmissions) {
        if (newEmissions != null) {
            mostRecentEmissions = newEmissions; // Always update most recent emissions
        }
    }
}
