package com.stem.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;

public class HomeActivity extends AppCompatActivity {

    MaterialButton btn_Customer,btn_Driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_Customer = findViewById(R.id.btn_Customer);
        btn_Driver = findViewById(R.id.btn_Driver);

        btn_Driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),DriverLogin.class));
            }
        });

        btn_Customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),CustomerRequst.class));
            }
        });
    }
}
