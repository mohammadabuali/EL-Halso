package com.postpc.elhalso.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.BusinessListActivity;
import com.postpc.elhalso.MainMapActivity;
import com.postpc.elhalso.R;
import com.postpc.elhalso.adapters.CategoryItemRecyclerViewAdapter;
import com.postpc.elhalso.callbacks.OnCategoryClick;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.location.LocationTracker;
import com.postpc.elhalso.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment screen for the user main screen as a list of categories and businesses
 */
public class CategoriesFragment extends Fragment implements OnCategoryClick {

    private static final String TAG = "CategoriesFragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private CategoryItemRecyclerViewAdapter adapter;

    private List<Business> businesses = null;
    private Map<String, List<Business>> categories;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(int columnCount) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new CategoryItemRecyclerViewAdapter(this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        MainMapActivity mainActivity = (MainMapActivity) requireActivity();
        mainActivity.callbacks.put(TAG, () -> {
            businesses = mainActivity.businessList;
            getCategories();
            populateList();
        });
        return view;
    }

    private void getCategories() {
        categories = new HashMap<>();

        for (Business business : businesses) {
            if (business.getCategory() == null) {
                continue;
            }
            if (categories.containsKey(business.getCategory())) {
                categories.get(business.getCategory()).add(business);
            } else {
                ArrayList<Business> list = new ArrayList<>();
                list.add(business);
                categories.put(business.getCategory(), list);
            }
        }
        LocationTracker locationTracker = ((MainMapActivity) requireActivity()).locationTracker;
        LatLng currentLocation = null;
        if (locationTracker.getLastLocation() != null) {
            currentLocation = locationTracker.getLastLocation().toLatLng();
        }
        if (currentLocation != null) {
            for (List<Business> businesses : categories.values()) {
                final LatLng finalCurrentLocation = currentLocation;
                businesses.sort((b1, b2) -> {
                    if (b1.getCoordinates() == null || b2.getCoordinates() == null) {
                        return b1.getCoordinates() != null ? 1 : b2.getCoordinates() != null ? -1 : 0;
                    }
                    LatLng loc1 = new LatLng(b1.getCoordinates().getLatitude(), b1.getCoordinates().getLongitude());
                    LatLng loc2 = new LatLng(b2.getCoordinates().getLatitude(), b2.getCoordinates().getLongitude());
                    float d1 = Utils.distanceBetween(finalCurrentLocation, loc1);
                    float d2 = Utils.distanceBetween(finalCurrentLocation, loc2);
                    int f = Float.compare(d1, d2);
                    if (f == 0) {
                        return Float.compare(b1.getReviewsScore(), b2.getReviewsScore());
                    } else {
                        return f;
                    }
                });
            }
        }
    }

    public void populateList() {
        adapter.mValues.clear();
        adapter.mValues.addAll(categories.keySet());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(String category) {
        // Open Businesses list view
        Log.d(TAG, "onCategoryClick: " + category);
        Gson gson = new Gson();
        Intent intent = new Intent(requireContext(), BusinessListActivity.class);
        intent.putExtra("cat_name", category);
        intent.putExtra("business_list", gson.toJson(categories.get(category)));
        LocationTracker locationTracker = ((MainMapActivity) requireActivity()).locationTracker;
        intent.putExtra("user_location", gson.toJson(locationTracker.getLastLocation()));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
    }
}