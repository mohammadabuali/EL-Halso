package com.postpc.elhalso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private static final String EMAIL_REGEX = "^(.+)@(.+)\\.(.+)$";
    private static final String TAG = "EmailSign";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if(savedInstanceState != null){
            ((TextView)findViewById(R.id.emailTxt)).setText(savedInstanceState.getString("email", null));
            ((TextView)findViewById(R.id.nameTxt)).setText(savedInstanceState.getString("name", null));
            ((TextView)findViewById(R.id.passTxt)).setText(savedInstanceState.getString("pass", null));
            ((TextView)findViewById(R.id.pass2Txt)).setText(savedInstanceState.getString("pass2", null));
        }
    }

    private boolean validateDetails(){
        String email = ((TextView) findViewById(R.id.emailTxt)).getText().toString();
        String name = ((TextView) findViewById(R.id.nameTxt)).getText().toString();
        String pass = ((TextView) findViewById(R.id.passTxt)).getText().toString();
        String pass2 = ((TextView) findViewById(R.id.pass2Txt)).getText().toString();


        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showMessage("Email address is invalid.");
            return false;
        }

        if(pass.length() < 6){
            showMessage("Password length is too short.");
            return false;
        }
        if(!pass2.equals(pass)){
            showMessage("Passwords do not match.");
            return false;
        }
        if(name.length() < 3){
            showMessage("Name is too short.");
            return false;
        }

        return true;
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void signupBtn(View v){
        if(!validateDetails())
            return;

        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        String pass = ((EditText)findViewById(R.id.passTxt)).getText().toString();

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        final String name = ((EditText)findViewById(R.id.nameTxt)).getText().toString();

        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Signing up", "Creating " + getString(R.string.app_name) + " user...");
        mFirebaseAuth.createUserWithEmailAndPassword(email, pass)
                .continueWithTask(new Continuation<AuthResult, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<AuthResult> task) throws Exception {
                        if(task.isSuccessful()){
                            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            return mFirebaseAuth.getCurrentUser().updateProfile(profile);
                        } else {
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            Log.d(TAG, "Authentication failed ", task.getException());
//                            throw new Exception();
                            showMessage("Email already in use. Please choose a different one.");
                            return null;
                        }
                    }
                }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                if(task.getException() instanceof NullPointerException){
                    return null;
                }
                if(task.isSuccessful()){
                    ((AppLoader)getApplicationContext()).showLoadingDialog(SignupActivity.this, "Signing up", "Sending verification email...");
                    return mFirebaseAuth.getCurrentUser().sendEmailVerification();
                } else {
                    ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                    showMessage("Failed to create user data");
                    Log.d(TAG, "Failed to update user");
                    return null;
                }
            }
        }).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.getException() instanceof NullPointerException){
                    return;
                }
                if (task.isSuccessful()) {
                    ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                    showMessage("Verification Email sent");
                    finish();
                } else {
                    Log.d(TAG, "Authentication failed", task.getException());
                    ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                    showMessage("Sending verification email failed");
                }

            }
        });
    }

    public void goSigninBtn(View view) {
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("email", ((TextView)findViewById(R.id.emailTxt)).getText().toString());
        outState.putString("name", ((TextView)findViewById(R.id.nameTxt)).getText().toString());
        outState.putString("pass", ((TextView)findViewById(R.id.passTxt)).getText().toString());
        outState.putString("pass2", ((TextView)findViewById(R.id.pass2Txt)).getText().toString());
    }
}