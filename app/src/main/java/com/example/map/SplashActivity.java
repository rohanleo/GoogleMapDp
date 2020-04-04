package com.example.map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

public class SplashActivity extends AppCompatActivity {

    public static LatLngBounds.Builder builder;
    public static LatLngBounds bounds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        builder = new LatLngBounds.Builder();

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || !isConnected ) {
            buildAlertMessageNoGps();
        }else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }, 1500);
        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn on your location and Check your internet connectivity")
                .setCancelable(true);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                finish();
                }
                }, 4000);
                /*.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        Toast.makeText(SplashActivity.this,"Location Service not Provided",Toast.LENGTH_LONG).show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                finish();
                            }
                        }, 1000);
                    }
                });*/
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void getDeviceLocation(){
        //Log.d(TAG,"getDeviceLocation: getting the device current location");
        FusedLocationProviderClient mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            Task location = mfusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        //Log.d(TAG, "onComplete: Found Location");
                        Location currentLocation = (Location) task.getResult();
                        //moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f, "My location");
                        builder.include(getsouthwest(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                        builder.include(getnortheast(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                        bounds = builder.build();
                    } else {
                        //Log.d(TAG, "onComplete: current location is null");
                        Toast.makeText(SplashActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }catch (SecurityException e)
        {
            //Log.d(TAG,"getDeviceLocation: SecurityException" + e.getMessage());
        }
    }

    private LatLng getnortheast(LatLng center) {
        double radiusInMeters = 5000;
        LatLng northeast = SphericalUtil.computeOffset(center,radiusInMeters,45.0);
        return northeast;
    }
    private LatLng getsouthwest(LatLng center) {
        double radiusInMeters = 5000;
        LatLng southwest = SphericalUtil.computeOffset(center,radiusInMeters,225.0);
        return southwest;
    }
}
