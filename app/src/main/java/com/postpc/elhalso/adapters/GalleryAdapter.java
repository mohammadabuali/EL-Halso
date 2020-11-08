package com.postpc.elhalso.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.BuildConfig;
import com.postpc.elhalso.ImageHolder;
import com.postpc.elhalso.callbacks.ImageMoveCallback;
import com.postpc.elhalso.R;
import com.postpc.elhalso.callbacks.StartDragListener;
import com.postpc.elhalso.data.Business;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<ImageHolder> implements ImageMoveCallback.ImageTouchHelperContract {
    private static final float FULL_ALPHA = 1.0f;
    private static final float SELECTED_ALPHA = 0.7f;

    private Context context;
    private Business business;
//    private ArrayList<String> gallery;
    private ArrayList<String> downloadedGallery;
    private boolean selecting;
    private boolean isEditMode;
    private ArrayList<String> selectedImages;
    private StartDragListener startDragListener;
    private File galleryFolder;
    private MutableLiveData<Integer> selectedImagesSize;
    private boolean orderChanged;

    private static final String TAG = "GalleryAdapter";

    public GalleryAdapter(Context context, Business business, File galleryFolder, boolean isEditMode, StartDragListener startDragListener){
        this.context = context;
        this.business = business;
        this.galleryFolder = galleryFolder;
        this.selectedImages = new ArrayList<>();
        this.selectedImagesSize = new MutableLiveData<>();
        this.selecting = false;
        this.isEditMode = isEditMode;
        this.startDragListener = startDragListener;
        this.downloadedGallery = new ArrayList<>();
        this.orderChanged = false;
    }

    public boolean isOrderChanged() {
        return orderChanged;
    }

    public LiveData<Integer> getSelectedImagesSize() {
        return selectedImagesSize;
    }

    public void addDownloadedImage(String imageName){
        if(!downloadedGallery.contains(imageName))
            downloadedGallery.add(imageName);
        Log.d(TAG, "adding image " + imageName);
        notifyItemChanged(business.getGallery().indexOf(imageName));
    }

    public ArrayList<String> getSelectedImages() {
        return selectedImages;
    }

    public boolean getIsSelecting(){
        return selecting;
    }

    public void triggerSelecting() {
        if(!isEditMode)
            return;
        selecting = !selecting;
        if(!selecting) {
            selectedImages.clear();
            selectedImagesSize.postValue(selectedImages.size());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageHolder(view);
    }

    @Override
    public synchronized void onBindViewHolder(@NonNull final ImageHolder holder, final int position) {
        if(position >= business.getGallery().size())
            return;

        holder.selectedBox.setVisibility(selecting && isEditMode ? View.VISIBLE : View.GONE);

        if(!downloadedGallery.contains(business.getGallery().get(position))) {
            holder.imageView.setImageBitmap(null);
            holder.imageProgress.setVisibility(View.VISIBLE);
            return;
        }

        holder.imageProgress.setVisibility(View.GONE);
        holder.selectedBox.setChecked(selectedImages.contains(business.getGallery().get(position)));

        File file = new File(galleryFolder, business.getGallery().get(position));
        Picasso.get().load(Uri.fromFile(file)).fit().into(holder.imageView);

        View.OnClickListener clickListener = v -> {
            if(selecting) {
                if(selectedImages.contains(business.getGallery().get(position))){
                    selectedImages.remove(business.getGallery().get(position));
                    if(selectedImages.isEmpty()){
                        triggerSelecting();
                        return;
                    }
                    selectedImagesSize.postValue(selectedImages.size());
                }
                else {
                    selectedImages.add(business.getGallery().get(position));
                    selectedImagesSize.postValue(selectedImages.size());
                }
                notifyItemChanged(position);
                return;
            }
            viewImageInDefaultViewer(position);
        };

        holder.imageView.setOnClickListener(clickListener);
        holder.selectedBox.setOnClickListener(clickListener);

        if(!isEditMode)
            return;

        holder.imageView.setOnLongClickListener(v -> {
            if(selecting && startDragListener != null) {
                startDragListener.requestDrag(holder);
                return true;
            }
            selectedImages.add(business.getGallery().get(position));
            selectedImagesSize.postValue(selectedImages.size());
            triggerSelecting();
            return true;
        });
    }

    private void viewImageInDefaultViewer(int position) {
        Log.d(TAG, "viewing image " + business.getGallery().get(position) + " at adapter " + (position + 1));
        Uri uri =  FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(galleryFolder, business.getGallery().get(position)));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return business.getGallery().size();
    }

    @Override
    public void onImageMoved(int fromPosition, int toPosition) {
        if(fromPosition != toPosition)
            orderChanged = true;
        String toSwap = business.getGallery().remove(fromPosition);
        business.getGallery().add(toPosition, toSwap);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onImageSelected(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(SELECTED_ALPHA);
    }

    @Override
    public void onImageClear(ImageHolder imageHolder) {
        imageHolder.itemView.setAlpha(FULL_ALPHA);
        notifyDataSetChanged();
    }
}
