package com.yulin.trackingevent.activities.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.yulin.trackingevent.services.event_sender.EventService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static List<String> locationPermissions = new ArrayList<>(Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* service will start after user grants (or refuses) needed permissions */
        if (shouldRequestPermissions()) {
            askForPermissions();
        } else startEventsService();

    }

    private void askForPermissions() {
        //just the location permission is needed in our case
        //shows explanation if needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRequestPermissionRationale(this::askForLocationPermission, this::onPermissionDenied);
        } else askForLocationPermission();
    }

    private boolean shouldRequestPermissions() {
        for (String permission : locationPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void showRequestPermissionRationale(Runnable onPositiveButtonClick, Runnable onNegativeButtonClicked) {
        // Shows an explanation to the user
        // waiting for the user's response! After the user sees the explanation, try to request the permission.
        new AlertDialog.Builder(this)
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton("OK", (dialogInterface, i) -> onPositiveButtonClick.run())
                .setNegativeButton("Cancel", (dialog, which) -> onNegativeButtonClicked.run())
                .create()
                .show();
    }

    private void startEventsService() {
        startService(new Intent(this, EventService.class));
        finish();
    }

    private void askForLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (!Arrays.asList(permissions).contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
                onPermissionDenied();
            }
        }
        startEventsService();
    }

    private void onPermissionDenied() {
        Toast.makeText(this, "Service will send event without location", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(this::startEventsService, 1000);
    }
}
