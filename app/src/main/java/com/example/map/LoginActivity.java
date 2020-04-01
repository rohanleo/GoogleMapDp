package com.example.map;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sp;
    private Button submit;
    public static EditText userName;
    public static EditText userPhoneNum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences("First log",0);
        if(sp.getString("First login","").toString().equals("no")){
            Intent i = new Intent(LoginActivity.this,MapActivity.class);
            startActivity(i);
            finish();
        }else{
            setContentView(R.layout.activity_splash);
            userName = findViewById(R.id.nameText);
            userPhoneNum = findViewById(R.id.phoneText);
            submit = findViewById(R.id.submitButton);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(googleServiceAvailable()){
                        if(userName.getText().toString().matches("") || userPhoneNum.getText().toString().matches("")){
                            Toast.makeText(LoginActivity.this,"Please fill the details",Toast.LENGTH_LONG).show();
                        }else{
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("First login","no");
                            editor.commit();
                            Intent i = new Intent(LoginActivity.this,MapActivity.class);
                            startActivity(i);
                            finish();
                        }
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
}
