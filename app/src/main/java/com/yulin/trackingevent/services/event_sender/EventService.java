package com.yulin.trackingevent.services.event_sender;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.yulin.trackingevent.rest.Config;
import com.yulin.trackingevent.rest.PostEventService;
import com.yulin.trackingevent.rest.RetrofitInstance;

import java.sql.Timestamp;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.yulin.trackingevent.activities.main.MainActivity.locationPermissions;

public class EventService extends Service implements LocationListener {
    private static final long INTERVAL = 1000 * 60 * 60 * 4; // 4 hours
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Handler handler = new Handler();
    private PostEventService postEventService;

    public EventService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("MissingPermission") //permissions *are* checked, just outside the scope for aesthetic reasons
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notifyUser("events service started");

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null && locationRequestGranted())
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        postEventService = RetrofitInstance.getInstance().create(PostEventService.class);
        scheduleEventSender.run();

        return START_STICKY;
    }

    private void notifyUser(String message) {
        uiHandler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private boolean locationRequestGranted() {
        for (String permission : locationPermissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private Runnable scheduleEventSender = new Runnable() {
        @Override
        public void run() {
            try {
                sendEvent();
            } finally {
                handler.postDelayed(scheduleEventSender, INTERVAL);
            }
        }

    };

    private void sendEvent() {
        Config.timestamp = new Timestamp(new Date().getTime()).toString();
        Call<String> call = postEventService.postEvent(Config.id, Config.event, Config.timestamp, Config.geo, Config.deviceVersion);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                onSuccessfulPost(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (t.getCause() != null)
                    onCallFailed(t.getCause());
            }
        });
    }

    private void onCallFailed(Throwable cause) {
        Log.e("call failed", cause.toString());
    }

    private void onSuccessfulPost(String body) {
        uiHandler.post(() -> notifyUser("event sent"));
    }


    @Override
    public void onLocationChanged(Location location) {
        Config.geo = "lat: " + location.getLatitude() + " , lon: " + location.getLongitude();
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
}
