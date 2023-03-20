package com.stem.app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class CustomerRequst extends AppCompatActivity {

    MaterialButton btn_Search;
    private TextInputEditText txt_UserName,txt_Phone;
    private AlertDialog dialog = null;
    String userName,phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_requst);

        btn_Search = findViewById(R.id.btn_Search);
        txt_UserName = findViewById(R.id.txt_UserName);
        txt_Phone = findViewById(R.id.txt_Phone);

        btn_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisableActionsUntilCheckForLogin();
                if(!Validate())return;

                User.CreateCustomer(userName,phone);
                startActivity(new Intent(getApplicationContext(),CustomerMapActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dialog != null)
            dialog.dismiss();
    }

    private boolean Validate(){
        userName = txt_UserName.getText().toString();
        phone = txt_Phone.getText().toString();

        if (TextUtils.isEmpty(userName)){
            EndLoginCheck("Please Enter your user Name");
            return  false;
        }
        if (TextUtils.isEmpty(phone)){
            EndLoginCheck("Please Enter your Phone");
            return  false;
        }
        return true;
    }
    private void DisableActionsUntilCheckForLogin(){
        dialog = new MaterialAlertDialogBuilder(CustomerRequst.this)
                .setTitle("Customer Login")
                .setMessage("Please wait, while we are checking your data...")
                .show();

        btn_Search.setActivated(false);
    }
    private void EndLoginCheck(String msg){
        Toast.makeText(CustomerRequst.this, msg, Toast.LENGTH_SHORT).show();
        btn_Search.setActivated(true);
        dialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dialog != null)
            dialog.dismiss();
    }
}
