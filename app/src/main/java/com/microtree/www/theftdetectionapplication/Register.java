package com.microtree.www.theftdetectionapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends AppCompatActivity {

    EditText user, email, phone, pass, re_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = (EditText) findViewById(R.id.edit_name);
        email = (EditText) findViewById(R.id.edit_email);
        phone = (EditText) findViewById(R.id.edit_phone);
        pass = (EditText) findViewById(R.id.edit_pass);
        re_pass = (EditText) findViewById(R.id.edit_repass);


    }

    private String getText(EditText editText){
        return editText.getText().toString();
    }

    public void click(View view) {
        if (getText(user).isEmpty()){
            user.setError("enter username");
            return;
        }

        if (getText(email).isEmpty()){
            email.setError("enter email");
            return;
        }

        if (getText(phone).isEmpty()){
            phone.setError("enter mobile number");
            return;
        }

        if (getText(pass).isEmpty()) {
            pass.setError("Enter Password");
            return;
        }

        if (!getText(re_pass).equals(getText(pass))){
            re_pass.setError("password didn't match");
            return;
        }

        Database database = new Database(this);
        if (database.Register(getText(user), getText(email), getText(phone), getText(pass))){
            Toast.makeText(this, "Register Successfully", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }else{
            Toast.makeText(this, "Unable to Register", Toast.LENGTH_SHORT).show();
        }


    }
}

