package com.example.map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.kml.KmlLayer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";
    //Widgets
    private SearchView mSearchText;
    private ImageView mgps, mInfo;
    private Button addMarker;

    GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private Location currentLocation;
    private ArrayList<Marker> markerList;

    DatabaseReference databaseMarkers, databaseMessages;

    private boolean addingMarkerEnabled=false, isTimePickerEnabled=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        markerList = new ArrayList<>();

        databaseMarkers= FirebaseDatabase.getInstance().getReference("geojson");
        databaseMessages = FirebaseDatabase.getInstance().getReference("messages");
        mSearchText = findViewById(R.id.input_search);
        mgps = findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.ic_info);
        addMarker = findViewById(R.id.addMarker);
        addingMarkerEnabled = false;
        isTimePickerEnabled=false;
        initMap();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_LONG).show();
        Log.d(TAG, "OnMapReady: Map is Ready");

        mMap = googleMap;
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            init();
            clickMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseMarkers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //markerList.clear();
                for(Marker m:markerList){
                    m.remove();
                }
                markerList.clear();
                View view = getLayoutInflater().inflate(R.layout.custom_marker_layout,null);
                ImageView img = view.findViewById(R.id.customImg);
                for(DataSnapshot markerSnapshot:dataSnapshot.getChildren()){
                    InfoWindowData info = markerSnapshot.getValue(InfoWindowData.class);
                    MarkerOptions options = new MarkerOptions().position(new LatLng(info.getLat(),info.getLng()))
                            .title("Details Available");
                    if(info.getType().equals("Hospital")){
                        img.setBackgroundResource(R.drawable.hospital);
                        options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(view)));
                    }else if(info.getType().equals("Grocery Shop")){
                        img.setBackgroundResource(R.drawable.shop);
                        options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(view)));
                    }else if(info.getType().equals("Gas Station")){
                        img.setBackgroundResource(R.drawable.fuel);
                        options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(view)));
                    }else if(info.getType().equals("Vegetable Shop")){
                        img.setBackgroundResource(R.drawable.fruit);
                        options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(view)));
                    }else if(info.getType().equals("Medical Store")){
                        img.setBackgroundResource(R.drawable.doctor);
                        options.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(view)));
                    }
                    Marker mk = mMap.addMarker(options);
                    if(SplashActivity.bounds.contains(options.getPosition()))
                        mk.setDraggable(true);
                    markerList.add(mk);
                    mk.setTag(info);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isTimePickerEnabled = false;
    }

    private void clickMap(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(addingMarkerEnabled){
                    Geocoder geocoder = new Geocoder(MapActivity.this);
                    List<Address> list = new ArrayList<>();
                    if(SplashActivity.bounds.contains(latLng)){
                        try {
                            list = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                        }catch (IOException e){
                            Log.e(TAG,"clickMap: Map is clicked" + e.getMessage());
                        }
                        if(list.size()>0){
                            Address address = list.get(0);
                            Log.d(TAG,"geolocate: found a location: " + address.toString());
                            //Toast.makeText(MapActivity.this,address.toString(),Toast.LENGTH_LONG).show();
                            MarkerOptions options = new MarkerOptions().position(latLng).title("No Details");
                            Marker mk = mMap.addMarker(options);
                            mk.setDraggable(true);
                            Toast.makeText(MapActivity.this, "Click Marker to Add Details", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(MapActivity.this,"Outside of your locality, Adding Marker Failed",Toast.LENGTH_LONG).show();
                    }
                    addingMarkerEnabled = false;
                    addMarker.setText("Add Marker");
                }
                isTimePickerEnabled =false;
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
                if(!marker.getTitle().equals("No Details")){
                    LatLng latLng = marker.getPosition();
                    InfoWindowData info = (InfoWindowData) marker.getTag();
                    databaseMarkers.child(info.getId()).removeValue();
                    info.setId(databaseMarkers.push().getKey());
                    info.setLat(latLng.latitude);
                    info.setLng(latLng.longitude);
                    //info.setAddedby(LoginActivity.userName);
                    //info.setAddedon(java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                    databaseMarkers.child(info.getId()).setValue(info);
                    marker.remove();
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTag()==null){
                    setDetails(marker);
                    marker.setTitle("Details Available");
                    return true;
                }
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                setDetails(marker);
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

        final Button btnsave, btndel;
        btnsave = view.findViewById(R.id.save);
        btndel = view.findViewById(R.id.delete);
        final EditText name = view.findViewById(R.id.name1);
        final Spinner type = view.findViewById(R.id.type1);
        final EditText opening = view.findViewById(R.id.openingTime1);
        final ImageView clock1 = view.findViewById(R.id.clock1);
        final EditText closing = view.findViewById(R.id.closingTime1);
        final ImageView clock2 = view.findViewById(R.id.clock2);
        final EditText openingEv = view.findViewById(R.id.openingTime1Ev);
        final ImageView clock1Ev = view.findViewById(R.id.clock1Ev);
        final EditText closingEv = view.findViewById(R.id.closingTime1Ev);
        final ImageView clock2Ev = view.findViewById(R.id.clock2Ev);
        final EditText remark = view.findViewById(R.id.remark1);
        final EditText phoneNum = view.findViewById(R.id.phoneNum1);
        opening.setInputType(InputType.TYPE_NULL);
        closing.setInputType(InputType.TYPE_NULL);
        openingEv.setInputType(InputType.TYPE_NULL);
        closingEv.setInputType(InputType.TYPE_NULL);
        final InfoWindowData info;
        if(marker.getTag()==null)
        {
            info = new InfoWindowData();
            info.setId(databaseMarkers.push().getKey());
            isTimePickerEnabled=true;
        }
        else
        {
            btndel.setText("Chat Room");
            btnsave.setText("Edit Details");
            info= (InfoWindowData) marker.getTag();
            name.setText(info.getName());
            name.setInputType(InputType.TYPE_NULL);
            opening.setText(info.getOpening());
            closing.setText(info.getClosing());
            openingEv.setText(info.getOpeningEv());
            closingEv.setText(info.getClosingEv());
            phoneNum.setText(info.getPhoneNum());
            phoneNum.setInputType(InputType.TYPE_NULL);
            remark.setText(info.getRemark());
            remark.setInputType(InputType.TYPE_NULL);
            type.setSelection(((ArrayAdapter<String>)type.getAdapter()).getPosition(info.getType()));
            type.setEnabled(false);

        }
        clock1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimePickerEnabled){
                    TimePickerDialog mTimePicker = new TimePickerDialog(MapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String ampm;
                            if(selectedHour>=12) ampm ="PM";
                            else ampm = "AM";
                            opening.setText(String.format("%02d",selectedHour) + ":" + String.format("%02d",selectedMinute)+ " " + ampm);
                            // opening.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, 0, 0,false);
                    mTimePicker.show();
                }
            }
        });
        clock2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimePickerEnabled){
                    TimePickerDialog mTimePicker = new TimePickerDialog(MapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String ampm;
                            if(selectedHour>=12) ampm ="PM";
                            else ampm = "AM";
                            closing.setText(String.format("%02d",selectedHour) + ":" + String.format("%02d",selectedMinute)+ " " + ampm);
                        }
                    }, 0, 0,false);
                    mTimePicker.show();
                }
            }
        });
        clock1Ev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimePickerEnabled){
                    TimePickerDialog mTimePicker = new TimePickerDialog(MapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String ampm;
                            if(selectedHour>=12) ampm ="PM";
                            else ampm = "AM";
                            openingEv.setText(String.format("%02d",selectedHour) + ":" + String.format("%02d",selectedMinute)+ " " + ampm);
                            // opening.setText(selectedHour + ":" + selectedMinute);
                        }
                    }, 0, 0,false);
                    mTimePicker.show();
                }
            }
        });
        clock2Ev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimePickerEnabled){
                    TimePickerDialog mTimePicker = new TimePickerDialog(MapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String ampm;
                            if(selectedHour>=12) ampm ="PM";
                            else ampm = "AM";
                            closingEv.setText(String.format("%02d",selectedHour) + ":" + String.format("%02d",selectedMinute)+ " " + ampm);
                        }
                    }, 0, 0,false);
                    mTimePicker.show();
                }
            }
        });
        btndel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btndel.getText().equals("Chat Room")){
                    Toast.makeText(MapActivity.this, "Opening Chatroom", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MapActivity.this,ChatActivity.class);
                    intent.putExtra("markerId",info.getId());
                    startActivity(intent);
                    materialStyledDialog.dismiss();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setMessage("Are you sure to delete this resource location ?").setCancelable(false).
                            setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    marker.remove();
                                    //markerList.remove(marker);
                                    databaseMarkers.child(info.getId()).removeValue();
                                    databaseMessages.child(info.getId()).removeValue();
                                    materialStyledDialog.dismiss();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            return;
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnsave.getText().equals("Save Changes")){
                    isTimePickerEnabled = false;
                    info.setName(name.getText().toString());
                    info.setType(type.getSelectedItem().toString());
                    info.setOpening(opening.getText().toString());
                    info.setClosing(closing.getText().toString());
                    info.setOpeningEv(openingEv.getText().toString());
                    info.setClosingEv(closingEv.getText().toString());
                    info.setPhoneNum(phoneNum.getText().toString());
                    info.setAddedby(LoginActivity.userName);
                    info.setAddedon(java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                    info.setRemark(remark.getText().toString());
                    info.setLat(marker.getPosition().latitude);
                    info.setLng(marker.getPosition().longitude);
                    if(info.getName().equals(""))
                        Toast.makeText(MapActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
                    else{
                        databaseMarkers.child(info.getId()).setValue(info);

                        Toast.makeText(MapActivity.this, "Details Edited and Stored", Toast.LENGTH_SHORT).show();
                        materialStyledDialog.dismiss();
                        marker.hideInfoWindow();
                        marker.remove();
                        marker.setTag(info);
                    }
                }else{
                    if(SplashActivity.bounds.contains(marker.getPosition())){
                        isTimePickerEnabled=true;
                        btndel.setText("Delete Marker");
                        btnsave.setText("Save Changes");
                        name.setInputType(InputType.TYPE_CLASS_TEXT);
                        type.setEnabled(true);
                        //opening.setInputType(InputType.TYPE_CLASS_TEXT);
                        //opening.setClickable(true);
                        //closing.setInputType(InputType.TYPE_CLASS_TEXT);
                        //closing.setClickable(true);
                        phoneNum.setInputType(InputType.TYPE_CLASS_TEXT);
                        remark.setInputType(InputType.TYPE_CLASS_TEXT);
                        marker.getTag();
                    }else{
                        Toast.makeText(MapActivity.this,"Outside of your locality, can't edit",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        materialStyledDialog.show();
        marker.hideInfoWindow();
    }

    private void init(){
        Log.d(TAG,"init: initialising");
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
                Intent intent = new Intent(MapActivity.this,InfoActivity.class);
                startActivity(intent);

            }
        });
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addMarker.getText().equals("Add Marker")){
                    addingMarkerEnabled=true;
                    addMarker.setText("Cancel");
                }else{
                    addingMarkerEnabled =false;
                    addMarker.setText("Add Marker");
                }
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

    private void getDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: getting the device current location");
        FusedLocationProviderClient mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
                Task location = mfusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found Location");
                            currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f, "My location");
                            SplashActivity.builder.include(getsouthwest(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                            SplashActivity.builder.include(getnortheast(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude())));
                            SplashActivity.bounds = SplashActivity.builder.build();
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
        }catch (SecurityException e)
        {
            Log.d(TAG,"getDeviceLocation: SecurityException" + e.getMessage());
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

    public Bitmap createDrawableFromView(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) MapActivity.this).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
}
