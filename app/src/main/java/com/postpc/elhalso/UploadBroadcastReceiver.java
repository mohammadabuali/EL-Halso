package com.postpc.elhalso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UploadBroadcastReceiver extends BroadcastReceiver {

    private final MutableLiveData<String> newImage  = new MutableLiveData<>();
    private String businessID;
    private boolean isLogo;
    private boolean uploaded;

    private static final String TAG = "UploadReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(AppLoader.UPLOAD_BROADCAST))
            return;

        businessID = intent.getStringExtra("businessID");
        isLogo = intent.getBooleanExtra("isLogo", false);
        uploaded = intent.getBooleanExtra("uploaded", false);
        newImage.postValue(intent.getStringExtra("newImage"));
    }

    public LiveData<String> getNewImage(){
        return newImage;
    }

    public String getBusinessID() {
        return businessID;
    }

    public boolean isLogo() {
        return isLogo;
    }

    public boolean isUploaded() {
        return uploaded;
    }
}
