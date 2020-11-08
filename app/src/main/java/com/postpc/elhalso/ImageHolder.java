package com.postpc.elhalso;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageHolder extends RecyclerView.ViewHolder{
    public ProgressBar imageProgress;
    public ImageView imageView;
    public CheckBox selectedBox;

    public ImageHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageItem);
        selectedBox = itemView.findViewById(R.id.selectedBox);
        imageProgress = itemView.findViewById(R.id.imageProgress);
    }
}
