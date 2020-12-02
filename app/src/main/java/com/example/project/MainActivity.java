package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignup;
    private TextView moveSignUp;
    private TextView moveLogin;
    private ProgressDialog progressDialog;
    LinearLayout register;
    LinearLayout login;
    private EditText LeditTextEmail;
    private EditText LeditTextPassword;
    private Button buttonSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        LeditTextEmail = (EditText) findViewById(R.id.loginEditTextEmail);
        LeditTextPassword = (EditText) findViewById(R.id.loginEditTextPassword);
        moveLogin = (TextView) findViewById(R.id.moveLogin);
        register = findViewById(R.id.Register);
        login = findViewById(R.id.signIn);
        register.setVisibility(View.INVISIBLE);
        moveSignUp = findViewById(R.id.moveSignUp);

        moveSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setVisibility(View.VISIBLE);
                login.setVisibility(View.INVISIBLE);
            }
        });
        moveLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setVisibility(View.INVISIBLE);
                login.setVisibility(View.VISIBLE);
            }
        });

        buttonSignup = (Button) findViewById(R.id.buttonSignup);
        buttonSignIn = (Button) findViewById(R.id.loginButtonSignup);
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            Intent i = new Intent(MainActivity.this, List.class);
            finish();  //Kill the activity from which you will go to next activity
            startActivity(i);
        }
buttonSignIn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String email = LeditTextEmail.getText().toString().trim();
        String password  = LeditTextPassword.getText().toString().trim();

        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(MainActivity.this,"Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)||password.length()<=6){
            Toast.makeText(MainActivity.this,"Please enter password of atleast 7 characters",Toast.LENGTH_LONG).show();
            return;
        }
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,"Login Successfull",Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(MainActivity.this,List.class);
startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this,task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
});
        progressDialog = new ProgressDialog(this);

        //attaching listener to button
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }
    private void registerUser(){

        //getting email and password from edit texts
        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        //checking if email and passwords are empty
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(password)||password.length()<=6){
            Toast.makeText(this,"Please enter password of atleast 7 characters",Toast.LENGTH_LONG).show();
            return;
        }

        //if the email and password are not empty
        //displaying a progress dialog

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        //creating a new user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //checking if success
                        if(task.isSuccessful()){
                            //display some message here
                            Toast.makeText(MainActivity.this,"Successfully registered",Toast.LENGTH_LONG).show();
                        }else{
                            //display some message here

                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();

                        }
                        progressDialog.dismiss();
                    }
                });

    }


}

