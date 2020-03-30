package com.example.map;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;


public class MainActivity extends AppCompatActivity {

    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (googleServiceAvailable()){
            init();
        }
    }

    private void init() {
        Button btmMap = findViewById(R.id.btnMap);
        btmMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });
    }


    public boolean googleServiceAvailable()
    {
        GoogleApiAvailability api =GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }
        else if (api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this,isAvailable,0);
            dialog.show();
        }
        else{
            Toast.makeText(this,"Cant connect to play Service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

}
