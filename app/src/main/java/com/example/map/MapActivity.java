package com.example.map;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Duration;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.data.kml.KmlLayer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //Widgets
    private SearchView mSearchText;
    private ImageView mgps, mInfo;

    //var
    private boolean mLocationPermissionGranted = false;
    GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    //private Location mLastKnownLocation;
    //private GoogleApiClient mGoogleApiClient;

    private Marker marker = null;
    private ArrayList<Marker> markerList;

    private boolean isInfoDiplayed = false, isInfoAvailable = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        //checkGps();
        if (googleServiceAvailable()) {
            mSearchText = findViewById(R.id.input_search);
            mgps = findViewById(R.id.ic_gps);
            mInfo = findViewById(R.id.ic_info);
            getLocationPermission();
        }
    }

    /*private void checkGps() {
        LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MapActivity.this,"Please Turn on your GPS",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            /*if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(MapActivity.this,"GPS is not available",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }*/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_LONG).show();
        Log.d(TAG, "OnMapReady: Map is Ready");

        mMap = googleMap;
        if(mLocationPermissionGranted){
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //mMap.getUiSettings().setMapToolbarEnabled(true);
            init();
            clickMap();
        }

    }

    private void clickMap(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Geocoder geocoder = new Geocoder(MapActivity.this);
                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                }catch (IOException e){
                    Log.e(TAG,"clickMap: Map is clicked" + e.getMessage());
                }
                if(list.size()>0){
                    Address address = list.get(0);
                    Log.d(TAG,"geolocate: found a location: " + address.toString());
                    //Toast.makeText(MapActivity.this,address.toString(),Toast.LENGTH_LONG).show();
                    MarkerOptions options = new MarkerOptions().position(latLng).title("Add details");
                    Marker mk = mMap.addMarker(options);
                    mk.setDraggable(true);
                    markerList.add(mk);
                    moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),15f,address.getAddressLine(0));
                }
            }
        });
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //markerList.remove(marker);
                //marker.remove();
                //dragMarker = marker;
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //markerList.remove(dragMarker);
                //dragMarker.remove();
                //MarkerOptions options = new MarkerOptions().title(marker.getTitle()).position(marker.getPosition());
                //marker = mMap.addMarker(options);
                marker.setDraggable(true);
                //markerList.add(marker);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTitle().equals("Add details")){
                    setDetails(marker);
                }
                else {
                    if(isInfoDiplayed){
                        marker.hideInfoWindow();
                        isInfoDiplayed=false;
                    }
                    else{
                        marker.showInfoWindow();
                        isInfoDiplayed=true;
                    }
                }
                return true;
            }
        });

        MyInfoWindowAdapter myInfoWindowAdapter = new MyInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(myInfoWindowAdapter);
    }

    private void setDetails(final Marker marker) {
        View view = getLayoutInflater().inflate(R.layout.add_details_layout,null);
        final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog.Builder(MapActivity.this)
                .setTitle("Add Details")
                .setCustomView(view, 10, 20, 10, 20)
                .withDialogAnimation(true, Duration.FAST)
                .setCancelable(true)
                .setStyle(Style.HEADER_WITH_TITLE)
                .withDarkerOverlay(true)
                .build();

        Button btnsave, btndel;
        final EditText name = view.findViewById(R.id.name1);
        final Spinner type = view.findViewById(R.id.type1);
        final EditText opening = view.findViewById(R.id.openingTime1);
        final EditText closing = view.findViewById(R.id.closingTime1);
        final EditText remark = view.findViewById(R.id.remark1);
        final EditText phoneNum = view.findViewById(R.id.phoneNum1);

        final InfoWindowData info = new InfoWindowData();

        btnsave = view.findViewById(R.id.save);
        btndel = view.findViewById(R.id.delete);
        btndel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marker.remove();
                markerList.remove(marker);
                materialStyledDialog.dismiss();
            }
        });
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info.setName(name.getText().toString());
                info.setType(type.getSelectedItem().toString());
                info.setOpening(opening.getText().toString());
                info.setClosing(closing.getText().toString());
                info.setPhoneNum(phoneNum.getText().toString());
                info.setAddedby(LoginActivity.userName.getText().toString());
                info.setAddedon(java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                info.setRemark(remark.getText().toString());
                materialStyledDialog.dismiss();
                marker.hideInfoWindow();
                marker.setTitle("Details Available");
            }
        });
        materialStyledDialog.show();
        marker.setTag(info);
        marker.hideInfoWindow();
    }

    private void init(){
        Log.d(TAG,"init: initialising");
        System.out.println("check1");
        markerList = new ArrayList<>();
        mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                geoLocate();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mgps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.instruction_layout,null);
                final MaterialStyledDialog materialStyledDialog = new MaterialStyledDialog.Builder(MapActivity.this)
                        .setTitle("How to use")
                        .setCustomView(view, 10, 20, 10, 20)
                        .withDialogAnimation(true, Duration.FAST)
                        .setCancelable(true)
                        .setStyle(Style.HEADER_WITH_TITLE)
                        .withDarkerOverlay(true)
                        .build();
                materialStyledDialog.show();
            }
        });
    }

    private void geoLocate(){
        Log.d(TAG,"geoLocate: geolocating");
        String searchString = mSearchText.getQuery().toString();
        Geocoder geocoder =  new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,1);
        }catch (IOException e){
            Log.e(TAG,"geolocate: IOException " + e.getMessage());
        }
        if(list.size()>0){
            Address address = list.get(0);
            Log.d(TAG,"geolocate: found a location: " + address.toString());
            //Toast.makeText(this,address.toString(),Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),15f,address.getAddressLine(0));
        }
    }

    private void moveCamera(LatLng latLng,float zoom, String title){
        Log.d(TAG,"moveCamera: moving camera to: lat " + latLng.latitude + " log: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }
    private void initMap(){
        Log.d(TAG,"initMap: Map is Initialised");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting location permissions");

        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                //Log.d(TAG,"getLocationPermission: getting location permissions 1" + mLocationPermissionGranted );
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
                //Log.d(TAG,"getLocationPermission: getting location permissions 2" + mLocationPermissionGranted );
            }
        }
        else{
            ActivityCompat.requestPermissions(this,permissions,LOCATION_PERMISSION_REQUEST_CODE);
            //Log.d(TAG,"getLocationPermission: getting location permissions 3" + mLocationPermissionGranted );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult: called");

        mLocationPermissionGranted = false;

        switch (requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0){
                    for (int i=0;i<grantResults.length;i++){
                        if (grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted=false;
                            Log.d(TAG,"onRequestPermissionsResult: permission failed");
                            Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted=true;
                    //Log.d(TAG,"onRequestPermissionsResult: permission granted 1" + mLocationPermissionGranted);
                    initMap();
                }
            }
        }
    }

    public boolean googleServiceAvailable()
    {
        GoogleApiAvailability api =GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(MapActivity.this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }
        else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(MapActivity.this,isAvailable,9001);
            dialog.show();
        }
        else{
            Toast.makeText(this,"Cant connect to play Service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: getting the device current location");
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted){
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found Location");
                            Location location = (Location) task.getResult();
                            /*if(location == null){
                                location.setLatitude(20.5937);
                                location.setLongitude(78.9629);
                                moveCamera(new LatLng(location.getLatitude(),location.getLongitude()), 6f,"My location");
                            }
                            else*/ moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), 15f, "My location");
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e)
        {
            Log.d(TAG,"getDeviceLocation: SecurityException" + e.getMessage());
        }
    }
}
