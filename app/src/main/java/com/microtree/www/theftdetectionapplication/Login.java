package com.microtree.www.theftdetectionapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;



public class Login extends AppCompatActivity {

    private static final String TAG = "PICTURE";
    EditText pass, email;
    ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pass = (EditText) findViewById(R.id.edit_pass);
        email = (EditText) findViewById(R.id.edit_email);
        pd = new ProgressDialog(this);



    }


    private String getText(EditText editText){
        return editText.getText().toString();
    }

    public void OnRegister(View view) {
        Intent pushIntent = new Intent(getApplicationContext(), MyService.class);
        //startService(pushIntent);
        startActivity(new Intent(getApplicationContext(), Register.class));
    }

    public void OnLogin(View view) {

        //startActivity(new Intent(getApplicationContext(), MainActivity.class));



        if (getText(email).isEmpty()){
            email.setError("enter email");
            return;
        }


        if (getText(pass).isEmpty()) {
            pass.setError("Enter Password");
            return;
        }
        pd = ProgressDialog.show(this, "Authenticating", "Please Wait...");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                LoginResult();
            }
        }, 3000);

    }

    private void LoginResult() {

        Database database = new Database(this);
        if (database.Login(getText(email), getText(pass))){
            pd.dismiss();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }else{
            pd.dismiss();
            Toast.makeText(this, "Invalid Login Credentials", Toast.LENGTH_SHORT).show();
        }

    }


}
