package com.postpc.elhalso.callbacks;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.ImageHolder;

public class ImageMoveCallback extends ItemTouchHelper.Callback {

    private final ImageTouchHelperContract imageTouchHelper;

    public ImageMoveCallback(ImageTouchHelperContract imageTouchHelper){
        this.imageTouchHelper = imageTouchHelper;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        imageTouchHelper.onImageMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ImageHolder) {
                ImageHolder myViewHolder = (ImageHolder) viewHolder;
                imageTouchHelper.onImageSelected(myViewHolder);
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ImageHolder) {
            ImageHolder myViewHolder= (ImageHolder) viewHolder;
            imageTouchHelper.onImageClear(myViewHolder);
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }

    public interface ImageTouchHelperContract {
        void onImageMoved(int fromPosition, int toPosition);
        void onImageSelected(ImageHolder imageHolder);
        void onImageClear(ImageHolder imageHolder);
    }
}
