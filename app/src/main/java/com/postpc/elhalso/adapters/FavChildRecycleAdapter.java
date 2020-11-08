package com.postpc.elhalso.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.AppLoader;
import com.postpc.elhalso.BusinessActivity;
import com.postpc.elhalso.R;
import com.postpc.elhalso.callbacks.OnBusinessDelete;
import com.postpc.elhalso.data.Business;

import java.util.List;

public class FavChildRecycleAdapter extends RecyclerView.Adapter<FavChildRecycleAdapter.ViewHolder> {

    List<Business> items;
    Context context;
    private OnBusinessDelete onItemDelete;


    public FavChildRecycleAdapter(List<Business> items, Context context, OnBusinessDelete onItemDelete) {
        this.items = items;
        this.context = context;
        this.onItemDelete = onItemDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.context = parent.getContext();
        View view = layoutInflater.inflate(R.layout.fav_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemTextView.setText(items.get(position).getName());
        AppLoader context = (AppLoader) this.context.getApplicationContext();
        holder.delBtn.setOnClickListener(v -> this.onItemDelete.onBusinessDelete(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BusinessActivity.class);
            intent.putExtra("business", items.get(position));
            FavChildRecycleAdapter.this.context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemTextView;
        ImageView delBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemTextView = itemView.findViewById(R.id.fav_row_tv);
            delBtn = itemView.findViewById(R.id.favDeleteBtn);

        }
    }
}
