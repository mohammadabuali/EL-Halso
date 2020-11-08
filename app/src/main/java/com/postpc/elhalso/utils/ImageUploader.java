package com.postpc.elhalso.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.postpc.elhalso.AppLoader;
import com.postpc.elhalso.data.Business;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ImageUploader extends ListenableWorker {
    private CallbackToFutureAdapter.Completer<Result> callback;
    private Context appContext;
    private String businessID;
    private Uri imageUri;
    private String imageName;
    private boolean isLogo;

    private static final String TAG = "ImageUploader";

    public static void addImageUpload(final Context appContext, final Business business, final Uri imageUri, final boolean isLogo) {
        final String imageName = getUniqueImageName(business, DocumentFile.fromSingleUri(appContext, imageUri).getName());
        if(!isLogo)
            business.addImage(imageName);

        WorkManager workManager = WorkManager.getInstance(appContext);
        OneTimeWorkRequest.Builder imageUploadBuilder = new OneTimeWorkRequest.Builder(ImageUploader.class);
        Data.Builder data = new Data.Builder();
        Map<String, Object> map = new HashMap<>();
        map.put("businessID", business.getId());
        map.put("imageUri", imageUri.toString());
        map.put("imageName", imageName);
        map.put("isLogo", isLogo);
        data.putAll(map);
        imageUploadBuilder.setInputData(data.build());
        imageUploadBuilder.setConstraints(Constraints.NONE);
        String uniqueTaskName = "Upload" + imageUri.toString() + (isLogo ? "1" : "0");
        Log.d(TAG, "enqueuing unique name: " + uniqueTaskName);
//      workManager.enqueueUniqueWork(uniqueTaskName, ExistingWorkPolicy.KEEP, imageUploadBuilder.build());
        workManager.enqueue(imageUploadBuilder.build());
    }

    public static String getUniqueImageName(Business business, String imageName) {
        if(!business.getGallery().contains(imageName))
            return imageName;

        int idx, num = 1, numLength;
        idx = imageName.lastIndexOf('.');
        imageName = imageName.substring(0,idx) + "_1" + imageName.substring(idx);
        idx = imageName.lastIndexOf('_');
        while(business.getGallery().contains(imageName)){
            num += 1;
            numLength = ("" + num).length();
            imageName = imageName.substring(0,idx+1) + num + imageName.substring(idx+numLength+1);
        }
        return imageName;
    }

    public ImageUploader(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        this.appContext = appContext;
        Map<String, Object> map = workerParams.getInputData().getKeyValueMap();
        this.businessID = (String) map.get("businessID");
        this.imageUri = Uri.parse((String) map.get("imageUri"));
        this.imageName = (String) map.get("imageName");
        this.isLogo = (Boolean) map.get("isLogo");
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ListenableFuture<Result> future = CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Result>() {
            @Nullable
            @Override
            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Result> completer) throws Exception {
                callback = completer;
                return null;
            }
        });

        uploadImage();
        return future;
    }

    private void uploadImage() {
        Log.d(TAG, "uploading image");
        ThreadingHelper.runAsyncInBackground(new Runnable() {
            @Override
            public void run() {
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(businessID + "/" + imageName);
                ref.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d(TAG, "putting storage file is " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.e(TAG, task.getException().toString());
                            return null;
                        }
                        if (isLogo) {
                            return FirebaseFirestore.getInstance().collection("business").document(businessID).update("logo", imageName);
                        } else {
                            return FirebaseFirestore.getInstance().collection("business").document(businessID).update("gallery", FieldValue.arrayUnion(imageName));
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent();
                        intent.setAction(AppLoader.UPLOAD_BROADCAST);
                        intent.putExtra("businessID", businessID);
                        intent.putExtra("newImage", imageName);
                        intent.putExtra("isLogo", isLogo);

                        if (task.isSuccessful()) {
                            intent.putExtra("uploaded", true);
                            appContext.sendBroadcast(intent);
                            if (callback != null)
                                callback.set(Result.success());
                            Log.d(TAG, "image uploaded to storage and firestore successfully");
                        } else {
                            intent.putExtra("uploaded", false);
                            appContext.sendBroadcast(intent);
                            Log.d(TAG, "image failed to upload to firestore");
                            Log.d(TAG, task.getException().toString());
                            if (callback != null)
                                callback.set(Result.failure());
                        }
                    }
                });
            }
        });
    }

    private void copyImageToAppDir(String imageName) throws IOException {
        File businessDir = new File(appContext.getFilesDir(), businessID);
        if(!businessDir.exists()) {
            businessDir.mkdir();
        }
        File file = new File(businessDir, imageName);
        if(file.exists()){
            return;
        }

        InputStream input = appContext.getContentResolver().openInputStream(imageUri);
        Files.copy(input, file.toPath());
        input.close();
    }
}
