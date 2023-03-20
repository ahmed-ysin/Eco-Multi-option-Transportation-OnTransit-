package com.stem.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stem.app.Model.Driver;

import java.util.HashMap;
import java.util.Map;

public class DriverLogin extends AppCompatActivity {

    private MaterialButton btn_SignUp,btn_SignIn;
    private FirebaseAuth mAuth;
    private TextInputEditText txt_Email,txt_Password;
    private AlertDialog dialog = null;

    String email,password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        mAuth = FirebaseAuth.getInstance();
        this.setFinishOnTouchOutside(false);

        CreateSignupAction();
        CreateSignInAction();
    }


    private void CreateSignupAction(){
        btn_SignUp = findViewById(R.id.btn_SignUp);
        btn_SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),DriverSignup.class));
            }
        });
    }

    private void CreateSignInAction(){
        btn_SignIn = findViewById(R.id.btn_SignIn);
        txt_Email = findViewById(R.id.txt_Email);
        txt_Password = findViewById(R.id.txt_Password);

        btn_SignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisableActionsUntilCheckForLogin();
                if(!Validate()){
                    return;
                }

                Login();
            }
        });
    }

    private void Login() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            User.SetCurrentUser(mAuth.getCurrentUser());

                            db.collection(User.driversColliction).document(User.GetCurrentUser().getUid())
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    HashMap<String,Object> data = new HashMap(task.getResult().getData());
                                    User.currentDriver = new Driver(data);

                                    //GetDriver data
                                    User.SetDriverOnline(true,User.currentDriver.key);

                                    EndLoginCheck("Welcome back ");
                                    startActivity(new Intent(getApplicationContext(), DriversMapActivity.class));
                                }
                            });

                        } else {
                            EndLoginCheck("Error adding document");
                        }
                    }
                });
    }

    private boolean Validate(){
        email = txt_Email.getText().toString();
        password = txt_Password.getText().toString();

        if (TextUtils.isEmpty(email)){
            EndLoginCheck("Please Enter your Email");
            return  false;
        }
        if (TextUtils.isEmpty(password)){
            EndLoginCheck("Please Enter your Password");
            return  false;
        }
        return true;
    }
    private void DisableActionsUntilCheckForLogin(){
        dialog = new MaterialAlertDialogBuilder(DriverLogin.this)
                .setTitle("Driver Login")
                .setMessage("Please wait, while we are checking your data...")
                .show();

        btn_SignIn.setActivated(false);
    }
    private void EndLoginCheck(String msg){
        Toast.makeText(DriverLogin.this, msg, Toast.LENGTH_SHORT).show();
        btn_SignIn.setActivated(true);
        dialog.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dialog != null)
            dialog.hide();
    }
}
