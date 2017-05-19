package com.microtree.www.theftdetectionapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMS_REQUEST_CODE = 300;
    SharedPreferences sharedPreferences;
    EditText mob, email;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mob = (EditText) findViewById(R.id.edit_mob);
        email = (EditText) findViewById(R.id.edit_email);
        button = (Button) findViewById(R.id.btn_active);
        button.setOnClickListener(this);
        if (!hasPermissions()){
            requestPerms();
        }
        sharedPreferences = getSharedPreferences(Constants.SharedPref.PrefName, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.SharedPref.STATUS, false)){
            button.setText("De Activate Detection");
            email.setText(sharedPreferences.getString(Constants.SharedPref.EMAIL, ""));
            mob.setText(sharedPreferences.getString(Constants.SharedPref.MOBILE, ""));
        }


    }

    private String getText(EditText editText){
        return editText.getText().toString();
    }




    private void Activate() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds")
        String serial = tm.getSimSerialNumber();

        if (getText(email).isEmpty()){
            email.setError("enter email");
            return;
        }

        if (getText(mob).isEmpty()){
            mob.setError("enter mobile number");
            return;
        }
        SaveInfo(serial, getText(email), getText(mob), true, "Service Activated");

    }

    private void SaveInfo(String ser, String email, String mob, boolean b, String msg){
        Log.d("MZK_APP", ser);
        Log.d("MZK_APP", email);
        Log.d("MZK_APP", mob);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.SharedPref.SIM_SERIAL_NUMBER, ser);
        editor.putString(Constants.SharedPref.EMAIL, email);
        editor.putString(Constants.SharedPref.MOBILE, mob);
        editor.putBoolean(Constants.SharedPref.STATUS, b);
        editor.apply();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        onBackPressed();

    }


    @Override
    public void onClick(View view) {
        if(sharedPreferences.getBoolean(Constants.SharedPref.STATUS, false)) {
            SaveInfo("", "", "", false, "Service De Activated");
        }else {
            Activate();
        }

    }

    private boolean hasPermissions(){
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions){
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }

    private void requestPerms(){
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions,PERMS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;
        switch (requestCode){
            case PERMS_REQUEST_CODE:
                for (int res : grantResults){
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed){

        }
        else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(this, "Permissions denied by user", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
