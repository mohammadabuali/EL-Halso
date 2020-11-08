package com.postpc.elhalso.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.AppLoader;
import com.postpc.elhalso.FirebaseHandler;
import com.postpc.elhalso.R;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.FavSection;
import com.postpc.elhalso.data.User;

import java.util.List;

public class FavMainRecyclerAdapter extends RecyclerView.Adapter<FavMainRecyclerAdapter.ViewHolder> {

    List<FavSection> sectionList;
    Context context;
    public FavMainRecyclerAdapter(List<FavSection> sectionList, Context context) {
        this.sectionList = sectionList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.section_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FavSection section = sectionList.get(position);
        String sectionName = section.getSectionName();
        List<Business> items = section.getSectionItems();

        holder.sectionNameTextView.setText(sectionName);

        FavChildRecycleAdapter childRecyclerAdapter = new FavChildRecycleAdapter(items, this.context, pos -> {
            User user = ((AppLoader)context.getApplicationContext()).getUser();
            Business to_be_deleted = items.get(pos);
            items.remove(pos);
            if (items.size() == 0) {
                sectionList.remove(position);
                if (sectionList.size() == 0) {
                    ((Activity)this.context).findViewById(R.id.no_fav_textView).setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            }
            user.removeFavoriteBusiness(to_be_deleted);
            notifyDataSetChanged();
            final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
            firebaseHandler.removeFavoriteBusiness(user, to_be_deleted);
        });
        holder.childRecyclerView.setAdapter(childRecyclerAdapter);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionNameTextView;
        RecyclerView childRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}
