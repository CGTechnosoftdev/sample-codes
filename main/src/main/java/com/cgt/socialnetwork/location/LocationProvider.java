package com.cgt.socialnetwork.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.cgt.socialnetwork.common.Constants;
import com.cgt.socialnetwork.common.Preference;

/**
 * Created by CGTechnosoft
 */
public class LocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static LocationProvider instance = null;
    private Context context = null;
    private GoogleApiClient googleApiClient = null;
    private Location mLastLocation = null;
    private ResultReceiver resultReceiver = null;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private LocationProvider(Context context) {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    public static LocationProvider getInstance(Context context) {
        if (instance == null) {
            instance = new LocationProvider(context.getApplicationContext());
        }

        return instance;
    }

    public void connectToPlayService() {
        googleApiClient.connect();
    }

    public void connectToPlayService(ResultReceiver resultReceiver) {
        googleApiClient.connect();
        this.resultReceiver = resultReceiver;
    }

    public void disconnectFromPlayService() {
        googleApiClient.disconnect();
        resultReceiver = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        //Debug.i("CURRENT LOCATION : " + mLastLocation);
        if (mLastLocation != null) {
            notifyCaller(Constants.SUCCESS_RESULT);
            Preference.getInstance(context).put(Constants.KEY_CURRENT_LAT, String.valueOf(mLastLocation.getLatitude()));
            Preference.getInstance(context).put(Constants.KEY_CURRENT_LONG, String.valueOf(mLastLocation.getLongitude()));
        } else {
            notifyCaller(Constants.FAILURE_RESULT);
        }

        // Begin polling for new location updates.
        //startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        notifyCaller(Constants.FAILURE_RESULT);
    }

    private void notifyCaller(int resultCode) {
        if (resultReceiver != null) {
            resultReceiver.send(resultCode, null);
        }
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        stopLocationUpdates();
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        Preference.getInstance(context).put(Constants.KEY_CURRENT_LAT, String.valueOf(location.getLatitude()));
        Preference.getInstance(context).put(Constants.KEY_CURRENT_LONG, String.valueOf(location.getLongitude()));
        stopLocationUpdates();
    }

}
