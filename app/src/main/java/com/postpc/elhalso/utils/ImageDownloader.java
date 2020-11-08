package com.postpc.elhalso.utils;

import android.util.Log;

import com.postpc.elhalso.utils.ThreadingHelper;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;

public class ImageDownloader {
    public interface DownloadCallback{
        void onImageDownloaded(String businessID, String imageName, boolean successful);
    }

    private static ArrayList<DownloadCallback> callbacks = new ArrayList<>();
    private static FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final String TAG = "ImageDownloader";

    synchronized public static void getImage(String imageName, String businessID, boolean isTemp, File downloadFolder, DownloadCallback callback) {
        if(!callbacks.contains(callback))
            callbacks.add(callback);
        bgDownload(imageName, businessID, isTemp, downloadFolder);
    }

    synchronized private static void bgDownload(final String imageName, final String businessID, final boolean isTemp, final File downloadFolder) {
        ThreadingHelper.runAsyncInBackground(new Runnable() {
            @Override
            public void run() {
                File localFile;
                try {
                    localFile = new File(downloadFolder, imageName);
                    if(localFile.exists()){
                        Log.d(TAG, "file <"+imageName+"> already exists");
                        downloadDone(businessID, imageName, true);
                        return;
                    }
                    localFile = new File(downloadFolder, imageName);
                    if(isTemp)
                        localFile.deleteOnExit();
                } catch(Exception e){
                    Log.e(TAG, "Failed to create file.");
                    Log.e(TAG, e.toString());
                    downloadDone(businessID, imageName, false);
                    return;
                }

                storage.getReference().child(businessID + "/" + imageName).getFile(localFile).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "image <" + imageName +"> downloaded successfully");
                        downloadDone(businessID, imageName, true);
                    }
                    else {
                        Log.d(TAG, "image failed to download");
                        downloadDone(businessID, imageName, false);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "run: Failed to download image", e);
                    downloadDone(businessID, imageName, false);
                });
            }
        });
    }

    private synchronized static void downloadDone(String businessID, String imageName, boolean successful) {
        for (DownloadCallback callback: callbacks) {
            if(callback != null)
                callback.onImageDownloaded(businessID, imageName, successful);
        }
    }

    public static void removeCallback(DownloadCallback callback) {
        callbacks.remove(callback);
    }
}
