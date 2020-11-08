package com.postpc.elhalso.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.R;
import com.postpc.elhalso.callbacks.OnCategoryClick;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for categories list in the user main screen
 */
public class CategoryItemRecyclerViewAdapter extends RecyclerView.Adapter<CategoryItemRecyclerViewAdapter.ViewHolder> {

    public List<String> mValues;
    private OnCategoryClick onCategoryClick;

    public CategoryItemRecyclerViewAdapter(OnCategoryClick onCategoryClick) {
        mValues = new ArrayList<>();
        this.onCategoryClick = onCategoryClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_categories_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(v -> {
            int position = holder.getLayoutPosition();
            CategoryItemRecyclerViewAdapter.this.onCategoryClick.onCategoryClick(mValues.get(position));
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = mValues.get(position);
        holder.mCatTextView.setText(item);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        private TextView mCatTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mCatTextView = mView.findViewById(R.id.category_text);
        }
    }
}
