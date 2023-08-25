package com.example.capture_current_lat_long;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MyServices extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "my_channel_id";
    private Handler handler;
    private Runnable runnable;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Schedule the task to run every 1 second
        runnable = new Runnable() {
            @Override
            public void run() {
                captureLocation();
                handler.postDelayed(this, 1000); // 1000 milliseconds = 1 second
            }
        };
        handler.post(runnable);

        // Start location updates
        startLocationUpdates();

        // Start the foreground service
        startForeground(NOTIFICATION_ID, createNotification());

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        // Stop location updates
        stopLocationUpdates();
    }

    private void captureLocation() {
        if (locationListener != null && locationManager != null) {
            // Check for location permission before requesting updates
            if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
        }
    }

    private void startLocationUpdates() {
        if (locationManager != null) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // Latitude and longitude captured here
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d("LocationUpdate", "Latitude: " + latitude + ", Longitude: " + longitude);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            // Check for location permission before requesting updates
            if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            }
        }
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    private Notification createNotification() {
        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification to show the foreground service is running
        Intent notificationIntent = new Intent(this, YourActivity.class); // Replace YourActivity with the activity you want to open when the notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText("Service is running in the foreground.")
                    .setSmallIcon(R.drawable.ic_launcher_background) // Replace ic_notification_icon with your notification icon
                    .setContentIntent(pendingIntent)
                    .build();
        }

        return notification;
    }
}
