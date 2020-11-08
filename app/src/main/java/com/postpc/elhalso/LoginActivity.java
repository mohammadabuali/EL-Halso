package com.postpc.elhalso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
//import com.firebase.ui.auth.AuthUI;
//import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private CallbackManager mCallerbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mFirebaseAuth;
    private Menu menu;

    private static final String TAG = "LoginAuthentication";
    private boolean isBusinessLogin;
    private static final int RC_GOOGLE_SIGN_IN = 901;
    private static final int RC_EMAIL_SIGN_IN = 902;

    private static final int BUSINESS_LOGO_ID = R.drawable.elhalso_logo_business;
    private static final int REGULAR_LOGO_ID = R.drawable.elhalso_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAuth = FirebaseAuth.getInstance();
        setupFacebookLogin();
        setupGoogleLogin();
        isBusinessLogin = false;
    }

    public void signinBtn(View view) {
        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        String pass = ((EditText)findViewById(R.id.passTxt)).getText().toString();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() || pass.length() < 6){
            showMessage("Invalid sign in details");
            return;
        }

        final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Signing in", "Connecting to " + getString(R.string.app_name) + "...");
        mFirebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            if(!task.getResult().getUser().isEmailVerified()){
                                showMessage("Please verify your email first!");
                                return;
                            }
                            Log.d(TAG, "Authentication success");
                            successfulLogin();
                        } else {
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            Log.d(TAG, "Authentication failed, ", task.getException());
                            showMessage("Authentication failed!");
                        }
                    }
                });
    }

    public void resetPassword(View view) {
        String email = ((EditText)findViewById(R.id.emailTxt)).getText().toString();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            showMessage("Invalid Email address");
            return;
        }
        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Reset Password", "Sending verification email...");
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                        if (task.isSuccessful()) {
                            showMessage("Email sent!");
                            Log.d(TAG, "Email sent.");
                        }
                        else {
                            showMessage("Failed to send email");
                            Log.d(TAG, "Failed to send email. " + task.getException());
                        }
                    }
                });
    }

    public void goSignupBtn(View view) {
        startActivity(new Intent(this, SignupActivity.class));
    }

    public void roundGoogleBtn(View view) {
        ((AppLoader)getApplicationContext()).showLoadingDialog(LoginActivity.this, "Authenticating", "Connecting with Google...");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    public void roundFbBtn(View view) {
        findViewById(R.id.fbLoginBtn).callOnClick();
    }

    public void showTypesDialog(View view){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.dialog_user_type, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);

        final AlertDialog alertD = alertDialogBuilder.create();

        ImageView regularBtn = (ImageView) promptView.findViewById(R.id.regularImgBtn);
        ImageView businessBtn = (ImageView) promptView.findViewById(R.id.businessImgBtn);

        regularBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoginType(false);
                alertD.dismiss();
            }
        });
        businessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateLoginType(true);
                alertD.dismiss();
            }
        });

        alertD.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if(fUser != null && sp.getBoolean("remember_login", false)){
            if(!fUser.isEmailVerified()){
                FirebaseAuth.getInstance().signOut();
                return;
            }
            if(sp.contains("last_user_id") && fUser.getUid().equals(sp.getString("last_user_id", ""))){
                isBusinessLogin = sp.getBoolean("is_business_login", false);
            }
            ((CheckBox)findViewById(R.id.rememberBox)).setChecked(true);
            successfulLogin();
        }
    }

    private void updateLoginType(boolean isBusiness){
        if(this.isBusinessLogin == isBusiness)
            return;

        ImageView logoImg = (ImageView) findViewById(R.id.logoImgBtn);
        this.isBusinessLogin = isBusiness;
        if(isBusiness){
            logoImg.setImageResource(BUSINESS_LOGO_ID);
        }
        else {
            logoImg.setImageResource(REGULAR_LOGO_ID);
        }
    }

    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_oauth2_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupFacebookLogin() {
        mCallerbackManager = CallbackManager.Factory.create();
        final LoginButton fbLoginBtn = (LoginButton) findViewById(R.id.fbLoginBtn);
        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fbLoginBtn.getText().toString().equals("Log out")) {
                    ((AppLoader) getApplicationContext()).showLoadingDialog(LoginActivity.this, "Authenticating", "Connecting with Facebook...");
                }
            }
        });
        fbLoginBtn.registerCallback(mCallerbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Successfully authenticated to Facebook");
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                showMessage("Facebook login canceled");
                Log.d(TAG, "Cancel authenticating to Facebook");
            }

            @Override
            public void onError(FacebookException error) {
                ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                showMessage("Error connecting with Facebook");
                Log.d(TAG, "Error authenticating to Facebook");
            }
        });
    }

    private void handleFacebookToken(AccessToken token){
        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Authenticating", "Connecting " + getString(R.string.app_name) + " with Facebook...");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Sign in to Firebase with credential successful");
                    successfulLogin();
                }
                else{
                    ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                    showMessage("Failed to connect Facebook with " + getString(R.string.app_name));
                    Log.d(TAG, "Sign in to Firebase with credential failed");
                }
            }
        });
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallerbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.d(TAG, "Google sign in failed", e);
                ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                showMessage("Error connecting with " + getString(R.string.app_name));
            }
        }
        else if(resultCode != RESULT_OK){
            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Authenticating", "Connecting " + getString(R.string.app_name) + " with Google...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            successfulLogin();
                        } else {
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            showMessage("Failed to connect Google with " + getString(R.string.app_name));
                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void successfulLogin() {
        FirebaseUser fUser = mFirebaseAuth.getCurrentUser();
        final User user = new User(fUser.getUid(), fUser.getDisplayName(), fUser.getEmail());
        user.setRadius(100);
        final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        final LiveData<Boolean> userUpdateDone = firebaseHandler.getUpdate();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putString("last_user_id", fUser.getUid())
                .putBoolean("is_business_login", isBusinessLogin)
                .putBoolean("remember_login", ((CheckBox)findViewById(R.id.rememberBox)).isChecked())
                .apply();
        ((AppLoader)getApplicationContext()).showLoadingDialog(this, "Syncing", "Getting user info...");
        firebaseHandler.updateOrCreateFirebaseUser(user);
        // wait for user fetch to end
        userUpdateDone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (!aBoolean)
                    return;
                userUpdateDone.removeObserver(this);
                ((AppLoader) getApplicationContext()).setUser(user);

                if(!isBusinessLogin) {
                    ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                    // regular user log in
                    goToUser();
                }
                else {
                    // need to fetch business for user
                    ((AppLoader)getApplicationContext()).showLoadingDialog(LoginActivity.this, "Syncing", "Getting business info...");
                    LiveData<Boolean> businessUpdateDone = firebaseHandler.getUpdate();
                    firebaseHandler.fetchBusinessForUser(user);
                    businessUpdateDone.observe(LoginActivity.this, new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean aBoolean) {
                            if (!aBoolean)
                                return;
                            userUpdateDone.removeObserver(this);
                            ((AppLoader)getApplicationContext()).dismissLoadingDialog();
                            Business business = (Business) firebaseHandler.getUpdatedObject();
                            goToBusiness(business);
                        }
                    });
                }
            }
        });
    }

    private void goToBusiness(final Business business) {
        ((AppLoader) getApplicationContext()).setBusiness(business);
        Intent intent;
        intent = new Intent(this, business.getName() == null ? EditBusinessActivity.class : BusinessActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToUser() {
        User user = ((AppLoader) getApplicationContext()).getUser();
        Intent intent;
        intent = new Intent(this, user.isFirstLogin() ? MainMapActivity.class : InitialSettingsActivity.class);
        startActivity(intent);
        finish();
    }

}