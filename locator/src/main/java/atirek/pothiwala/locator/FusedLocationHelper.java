package atirek.pothiwala.locator;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class FusedLocationHelper extends Service {

    public interface LocationListener {
        void onLocationReceived(@NonNull Location location);

        void onLocationAvailability(boolean isAvailable);
    }

    private Context context;
    private String TAG;
    private boolean enableDebug;
    private Location currentLocation;

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private void checkLog(Object data) {
        if (enableDebug){
            Log.d(TAG + ">>", data.toString());
        }
    }

    // The minimum time between updates in milliseconds
    private long TIME_INTERVAL = 5000;

    // The minimum distance between updates in metres
    private long DISPLACEMENT = 10;

    // The priority for location updates
    private int PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;

    private FusedLocationProviderClient locationProviderClient;
    private LocationListener listener;

    public FusedLocationHelper(Context context, String TAG, boolean enableDebug) {
        this.context = context;
        this.TAG = TAG;
        this.enableDebug = enableDebug;
    }

    public void setListener(LocationListener listener) {
        this.listener = listener;
    }

    public void setPriority(int priority) {
        this.PRIORITY = priority;
    }

    public void setTimeInterval(long milliseconds) {
        this.TIME_INTERVAL = milliseconds;
    }

    public void setDisplacement(long metres) {
        this.DISPLACEMENT = metres;
    }

    @SuppressLint("MissingPermission")
    public void initializeLocationProviders() {
        checkLog("Initialized");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(PRIORITY);
        locationRequest.setInterval(TIME_INTERVAL);
        locationRequest.setFastestInterval(TIME_INTERVAL);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            currentLocation = locationResult.getLastLocation();
            if (listener != null) {
                checkLog("Position: " + currentLocation.toString());
                listener.onLocationReceived(currentLocation);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);

            boolean isAvailable = locationAvailability != null && locationAvailability.isLocationAvailable();
            if (listener != null) {
                listener.onLocationAvailability(isAvailable);
            }

            String message = "Location Available";
            if (!isAvailable) {
                message = "Location Not Available";
            }
            checkLog(message);
        }
    };

    void stopLocationUpdates() {
        checkLog("Stopped");
        if (locationProviderClient != null && locationCallback != null) {
            locationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
