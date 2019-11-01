package com.ahmadnawaz.i160020_150069;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Map;
import java.util.concurrent.TimeUnit;


public class SignUp extends AppCompatActivity {

    EditText phone_number;
    EditText user_name;
    ProgressDialog progressDialog;
//                    progressDialog.setTitle("Uploading...");
//                    progressDialog.show();


    Button send_code;
    ProgressBar progressBar;
    String mVerificationId;

    EditText input;  // used in alert dialogue box

    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);  // hiding the status bar
        getSupportActionBar().hide(); // hiding the action bar

        phone_number=findViewById(R.id.tv);
        phone_number.setHint("+92.......");
        send_code=findViewById(R.id.send_btn);
        progressBar=findViewById(R.id.progress_bar);
        input = new EditText(SignUp.this);
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(this, "Sign up Activity created", Toast.LENGTH_SHORT).show();
        progressDialog = new ProgressDialog(SignUp.this);


        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("Log In ");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.show();


        send_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone= phone_number.getText().toString();
                phone=phone.replaceAll(" ", ""); // replacing spaces

                if(phone.isEmpty()) {
                    phone_number.setError("phone is not given ");
                    phone_number.requestFocus();
                }
                else if(!Patterns.PHONE.matcher(phone).matches()){
                    phone_number.setError("Please enter the valid phone number ");
                    phone_number.requestFocus();
                }

                else {
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            SignUp.this,
                            mCallBacks);
                }
            }

        });
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(SignUp.this, "Verification completed ", Toast.LENGTH_SHORT).show();
                input.setText("   Verified");
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(SignUp.this,"error in verification",Toast.LENGTH_SHORT).show();
                Toast.makeText(SignUp.this,e.getMessage(),Toast.LENGTH_LONG).show();
                Log.e("--------->>>>>",e.getMessage());

            }


            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                Toast.makeText(SignUp.this, "Code is sent ", Toast.LENGTH_SHORT).show();
                mVerificationId = verificationId;
                mResendToken = token;

                Verify_code(); // in case verification is done through other's phone number

            }

        };

    }

    public void Verify_code() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(SignUp.this);
            alertDialog.setTitle("Verification Code");
            alertDialog.setMessage("Enter code");

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
//                    alertDialog.setIcon(R.drawable.key);

            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String code = input.getText().toString();
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                            signInWithPhoneAuthCredential(credential);

                        }
                    });

            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();

        }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the/ signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
//                            Toast.makeText(SignUp.this, "GOing in to ptofile  ", Toast.LENGTH_SHORT).show();

                            Intent Profile = new Intent(SignUp.this,ProfileActivity.class);
                            Profile.putExtra("Signup_signall","signup");
                            startActivity(Profile);
                            finish();

//                            Toast.makeText(SignUp.this, "GOing out from ptofile  ", Toast.LENGTH_SHORT).show();

                            // if signed up
//                            Toast.makeText(SignUp.this, "GOing in to Signup ", Toast.LENGTH_SHORT).show();
//                            Intent signed_up = new Intent(SignUp.this,Contact_Menu.class);
//                            startActivity(signed_up);
//                            Toast.makeText(SignUp.this, "GOing out from signup  ", Toast.LENGTH_SHORT).show();

                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            Toast.makeText(SignUp.this,"error",Toast.LENGTH_LONG).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();

    }

}
