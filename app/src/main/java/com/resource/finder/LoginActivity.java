package com.resource.finder;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    //var
    private boolean mLocationPermissionGranted = false;

    private SharedPreferences sp;
    private Button submit;
    public static String userName;
    public static String userPhoneNum;
    private EditText unm;
    private EditText phone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("First log",0);
        getLocationPermission();
        if(sp.getString("First login","").toString().equals("no")){
            userName = sp.getString("unm","");
            userPhoneNum = sp.getString("phone","");
            Intent i = new Intent(LoginActivity.this,MapActivity.class);
            startActivity(i);
            finish();
        }else{
            setContentView(R.layout.activity_splash);
            unm = findViewById(R.id.nameText);
            phone = findViewById(R.id.phoneText);
            submit = findViewById(R.id.submitButton);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(googleServiceAvailable() && mLocationPermissionGranted &&
                    Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP){
                        if(unm.getText().toString().matches("") || phone.getText().toString().matches("")){
                            Toast.makeText(LoginActivity.this,"Please fill the details",Toast.LENGTH_LONG).show();
                        }else{
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("First login","no");
                            editor.putString("unm", String.valueOf(unm.getText()));
                            editor.putString("phone", String.valueOf(phone.getText()));
                            editor.commit();
                            userName = sp.getString("unm","");
                            userPhoneNum = sp.getString("phone","");
                            Intent i = new Intent(LoginActivity.this,MapActivity.class);
                            startActivity(i);
                            finish();

                        }
                    }
                    else{
                        //Toast.makeText(LoginActivity.this,"Please agree to all Permission Requests",Toast.LENGTH_LONG).show();
                        getLocationPermission();
                    }
                }
            });
        }
    }

    public boolean googleServiceAvailable()
    {
        GoogleApiAvailability api =GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(LoginActivity.this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }
        else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(LoginActivity.this,isAvailable,9001);
            dialog.show();
        }
        else{
            Toast.makeText(this,"Cant connect to play Service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void getLocationPermission(){
        Log.d(TAG,"getLocationPermission: getting location permissions");

        String[] permissions = {android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                //Log.d(TAG,"getLocationPermission: getting location permissions 1" + mLocationPermissionGranted );
                //initMap();
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
                        if (grantResults[i]!= PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted=false;
                            Log.d(TAG,"onRequestPermissionsResult: permission failed");
                            Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Log.d(TAG,"onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted=true;
                    //Log.d(TAG,"onRequestPermissionsResult: permission granted 1" + mLocationPermissionGranted);
                    //initMap();
                }
            }
        }
    }
}
