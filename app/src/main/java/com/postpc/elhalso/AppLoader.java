package com.postpc.elhalso;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AlertDialog;

import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.User;
import com.postpc.elhalso.data.LocationInfo;
import com.postpc.elhalso.location.LocationTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.List;


public class AppLoader extends Application {

    private static final String TAG = "AppLoader";

    private User user;
    private Business business;
    private UploadBroadcastReceiver uploadReceiver;
    private SweetAlertDialog loadingDialog;

    public static final String UPLOAD_BROADCAST = "business_updated";

    @Override
    public void onCreate() {
        super.onCreate();
        uploadReceiver = new UploadBroadcastReceiver();
        registerReceiver(uploadReceiver, new IntentFilter(UPLOAD_BROADCAST));
    }

    public User getUser() {
        return user;
    }

    public Business getBusiness() {
        return business;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public UploadBroadcastReceiver getUploadReceiver() {
        return uploadReceiver;
    }

    public void logout(final Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Are you sure you wish to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                setUser(null);
                setBusiness(null);
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void openProfile(final Context context, List<Business> businessList) {
        Gson gson = new Gson();
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra("businesses", gson.toJson(businessList));
        context.startActivity(intent);
    }

    public void setRadius(double radius) {
        this.user.setRadius(radius);
        FirebaseHandler.getInstance().updateUserRadius(user);
    }

    public void showLoadingDialog(Context context, String title, String message) {
        if (loadingDialog != null) {
            loadingDialog.setTitleText(title);
            loadingDialog.setContentText(message);
            return;
        }
        loadingDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText(title);
        loadingDialog.setContentText(message);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
