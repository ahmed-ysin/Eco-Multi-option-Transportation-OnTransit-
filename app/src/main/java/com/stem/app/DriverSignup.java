package com.stem.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.stem.app.Model.Driver;

public class DriverSignup extends AppCompatActivity {

    MaterialButton btn_Submit;
    Spinner sp_CarType , sp_FuelType;
    TextInputEditText txt_Email,txt_Password,txt_Name,txt_Phone,txt_CarNumber,txt_Cost,txt_FuelConsumption;
    private Driver driver;
    private FirebaseAuth mAuth;
    private AlertDialog dialog = null;
    String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        driver = new Driver();

        CreateToolbar();
        CreateSpinnerForCarType();
        CreateSpinnerForFoulsType();
        CreateSubmitButtonActions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dialog != null)
            dialog.dismiss();
    }

    private void CreateToolbar(){
        ImageView imageView = findViewById(R.id.backButton);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void CreateSpinnerForCarType(){
        sp_CarType = findViewById(R.id.sp_CarType);

        ArrayAdapter<User.CarTypes> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,User.CarTypes.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_CarType.setAdapter(adapter);
    }

    private void CreateSpinnerForFoulsType(){
         sp_FuelType = findViewById(R.id.sp_FuelType);

        ArrayAdapter<User.FuelType> adapter_Cost = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,User.FuelType.values());
        adapter_Cost.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_FuelType.setAdapter(adapter_Cost);
    }

    private void CreateSubmitButtonActions(){
        btn_Submit = findViewById(R.id.btn_Submit);
        txt_Email = findViewById(R.id.txt_Email);
        txt_Password = findViewById(R.id.txt_Password);
        txt_Name= findViewById(R.id.txt_Name);
        txt_Phone= findViewById(R.id.txt_Phone);
        txt_CarNumber= findViewById(R.id.txt_CarNumber);
        txt_Cost= findViewById(R.id.txt_Cost);
        txt_FuelConsumption= findViewById(R.id.txt_FuelConsumption);

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Start Validation;
                DisableActionsUntilCheckForLogin();
                if(!Validate()){
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(DriverSignup.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    User.SetCurrentUser(mAuth.getCurrentUser());
                                    driver.key = User.GetCurrentUser().getUid();

                                    User.currentDriver = User.CreateDriver(driver);

                                    // Add a new document with a generated ID
                                    startActivity(new Intent(getApplicationContext(),DriversMapActivity.class));
                                }
                                else {
                                    EndLoginCheck(task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }
    private boolean Validate(){
        email = txt_Email.getText().toString();
        password = txt_Password.getText().toString();
        driver.name = txt_Name.getText().toString();
        driver.phone = txt_Phone.getText().toString();
        driver.carNumber = txt_CarNumber.getText().toString();
        driver.carType = (User.CarTypes)sp_CarType.getSelectedItem();
        driver.fuelType = (User.FuelType) sp_FuelType.getSelectedItem();

        String cost = txt_Cost.getText().toString();
        String fuelConsumption = txt_FuelConsumption.getText().toString();

        if (TextUtils.isEmpty(email)){
            EndLoginCheck("Please Enter your Email");
            return  false;
        }
        if (TextUtils.isEmpty(password)){
            EndLoginCheck("Please Enter your Password");
            return  false;
        }

        if (TextUtils.isEmpty(driver.name)){
            EndLoginCheck("Please Enter your Name");
            return  false;
        }
        if (TextUtils.isEmpty(driver.phone)){
            EndLoginCheck("Please Enter your Phone");
            return  false;
        }

        if (TextUtils.isEmpty(driver.carNumber)){
            EndLoginCheck("Please Enter your Car Number");
            return  false;
        }
        if (TextUtils.isEmpty(cost)){
            EndLoginCheck("Please Enter your Cost");
            return  false;
        }
        if (TextUtils.isEmpty(fuelConsumption)){
            EndLoginCheck("Please Enter your Fuel Consumption");
            return  false;
        }
        driver.fuelConsumption = Double.parseDouble(fuelConsumption);
        driver.cost = Double.parseDouble(cost);
        driver.isOnline = true;
        return true;
    }
    private void DisableActionsUntilCheckForLogin(){
        dialog = new MaterialAlertDialogBuilder(DriverSignup.this)
                .setTitle("Driver Sign Up")
                .setMessage("Please wait, while we are check your data...")
                .show();

        btn_Submit.setCheckable(false);
    }
    private void EndLoginCheck(String msg){
        Toast.makeText(DriverSignup.this, msg, Toast.LENGTH_SHORT).show();
        btn_Submit.setCheckable(true);
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dialog != null)
            dialog.dismiss();
    }
}
