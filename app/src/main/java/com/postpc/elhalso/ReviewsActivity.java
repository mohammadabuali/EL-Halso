package com.postpc.elhalso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.postpc.elhalso.adapters.ReviewsAdapter;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.Review;
import com.postpc.elhalso.data.User;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewsActivity extends AppCompatActivity {

    private Business business;
    private boolean ownedBusiness;
    private RecyclerView reviewsRecyclerView;
    private ReviewsAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        if(getIntent().hasExtra("business")){
            business = getIntent().getParcelableExtra("business");
            ownedBusiness = false;
        }
        else {
            business = ((AppLoader)getApplication()).getBusiness();
            ownedBusiness = true;
        }
        findViewById(R.id.noReviewsTxt).setVisibility(business.getReviews().size() > 0 ? View.GONE : View.VISIBLE);

        business.sortReviews();
        reviewsRecyclerView = (RecyclerView) findViewById(R.id.reviewsRecyclerView);
        adapter = new ReviewsAdapter(business);
        reviewsRecyclerView.setAdapter(adapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        setSupportActionBar((Toolbar) findViewById(R.id.reviewsToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business.getName());
        getSupportActionBar().setSubtitle("Reviews");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.reviews_menu, menu);
        menu.findItem(R.id.action_add_review).setVisible(!ownedBusiness);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add_review:
                addReview();
                break;
            case R.id.action_logout:
                ((AppLoader)getApplicationContext()).logout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addReview(){
        final User user = ((AppLoader)getApplicationContext()).getUser();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View promptView = layoutInflater.inflate(R.layout.dialog_add_review, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final AlertDialog alertD = alertDialogBuilder.create();

        Button postBtn = (Button) promptView.findViewById(R.id.postBtn);
        Button cancelBtn = (Button) promptView.findViewById(R.id.cancelBtn);

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ((RatingBar)promptView.findViewById(R.id.reviewRating)).getRating();

                String text = ((EditText)promptView.findViewById(R.id.reviewTxt)).getText().toString();
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy;HH:mm:ss");
                Date date = new Date();
                Review review = new Review(business.getId(), user.getId(), user.getName(), format.format(date), rating, text);
                FirebaseHandler.getInstance().addReview(business, review);
                if(business.getReviews().size() > 0){
                    findViewById(R.id.noReviewsTxt).setVisibility(business.getReviews().size() > 0 ? View.GONE : View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
                alertD.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertD.dismiss();
            }
        });

        alertD.show();
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent();
        backIntent.putExtra("business", business);
        setResult(RESULT_OK, backIntent);
        finish();
    }
}