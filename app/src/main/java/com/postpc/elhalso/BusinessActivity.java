package com.postpc.elhalso;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.postpc.elhalso.adapters.GalleryAdapter;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.User;
import com.google.common.io.Files;
import com.postpc.elhalso.utils.ImageDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BusinessActivity extends AppCompatActivity implements ImageDownloader.DownloadCallback {

    private Business business;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private File galleryFolder;
    private boolean ownedBusiness;
    private Menu menu;

    private static final float EMPTY_TEXT_ALPHA = 0.6f;

    private static final int RC_EDIT_BUSINESS = 974;
    private static final int RC_GALLERY = 374;
    private static final int RC_REVIEWS = 647;
    private static final int RC_READ_EXTERNAL_PERMISSION = 675;

    private static final String TAG = "BusinessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        setupBusiness(getIntent());
        fillInBusinessDetails();

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(this, business, galleryFolder, false, null);
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        onBusinessUpdate();

        downloadImages();

        setSupportActionBar((Toolbar) findViewById(R.id.businessToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!ownedBusiness);
        getSupportActionBar().setTitle(business.getName());
    }

    public void descriptionClick(View view) {
        TextView descriptionTxt = findViewById(R.id.descriptionTxt);
        int maxLines = getResources().getInteger(R.integer.description_text_default_lines);
        if(descriptionTxt.getMaxLines() == maxLines) {
            descriptionTxt.setMaxLines(Integer.MAX_VALUE);
        }
        else {
            descriptionTxt.setMaxLines(maxLines);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.business_menu, menu);
        menu.findItem(R.id.action_edit).setVisible(ownedBusiness);
        menu.findItem(R.id.action_favorite).setVisible(!ownedBusiness);
        if(!ownedBusiness) {
            User user = ((AppLoader)getApplicationContext()).getUser();
            int toDraw = user.getFavorites().contains(business.getId()) ? R.drawable.ic_is_favorite : R.drawable.ic_is_not_favorite;
            menu.findItem(R.id.action_favorite).setIcon(toDraw);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_edit:
                editBusiness();
                break;
            case R.id.action_favorite:
                toggleFavorite();
                break;
            case R.id.action_gallery:
                showGallery(null);
                break;
            case R.id.action_reviews:
                showReviews(null);
                break;
            case R.id.action_logout:
                ((AppLoader)getApplicationContext()).logout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void fillInBusinessDetails(){
        ((TextView)findViewById(R.id.descriptionTxt)).setText(business.getDescription().trim().equals("") ? "(No description)" : business.getDescription());
        ((TextView)findViewById(R.id.reviewsTxt)).setText(getResources().getString(R.string.reviews_count, business.getReviews().size()));
        ((TextView)findViewById(R.id.categoryTxt)).setText(getResources().getString(R.string.category_text, business.getCategory()));
        findViewById(R.id.noImagesTxt).setVisibility(business.getGallery().size() > 0 ? View.GONE : View.VISIBLE);
        ((RatingBar)findViewById(R.id.ratingBar)).setRating(business.getReviewsScore());
    }

    private void onBusinessUpdate() {
        final UploadBroadcastReceiver uploadReceiver = ((AppLoader)getApplicationContext()).getUploadReceiver();
        uploadReceiver.getNewImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null || !business.getId().equals(uploadReceiver.getBusinessID()))
                    return;
                if(!uploadReceiver.isUploaded()){
                    int idx = business.getGallery().indexOf(s);
                    business.removeImage(s);
                    adapter.notifyItemRemoved(idx);
                    return;
                }
                if(uploadReceiver.isLogo()) {
                    business.setLogo(s);
                }
                downloadImage(s);
            }
        });
    }

    private void downloadImage(String image) {
        ImageDownloader.getImage(image, business.getId(), !ownedBusiness, galleryFolder, this);
    }

    private void downloadImages() {
        if(business.getLogo() != null) {
            downloadImage(business.getLogo());
        }
        for(String image : business.getGallery()){
            downloadImage(image);
        }
    }

    private void setupBusiness(Intent intent){
        if(intent == null || !intent.hasExtra("business")) {
            business = ((AppLoader) getApplicationContext()).getBusiness();
            ownedBusiness = true;
        }
        else {
            business = getIntent().getParcelableExtra("business");
            ownedBusiness = false;
        }

        ImageView wazeImg = findViewById(R.id.waze_button);

        if(ownedBusiness) {
            galleryFolder = new File(getFilesDir(), business.getId());
            if(!galleryFolder.exists()){
                galleryFolder.mkdir();
            }
            wazeImg.setVisibility(View.GONE);
        }
        else {
            galleryFolder = Files.createTempDir();
            galleryFolder.deleteOnExit();
            wazeImg.setVisibility(View.VISIBLE);
        }
    }

    public void editBusiness() {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }

    public void showReviews(View view){
        Intent intent = new Intent(this, ReviewsActivity.class);
        if(!ownedBusiness)
            intent.putExtra("business", business);
        startActivityForResult(intent, RC_REVIEWS);
    }

    public void toggleFavorite() {
        User user = ((AppLoader)getApplicationContext()).getUser();
        int toDraw;
        if(user.getFavorites().contains(business.getId())){
            FirebaseHandler.getInstance().removeFavoriteBusiness(user, business);
            toDraw = R.drawable.ic_is_not_favorite;
        }
        else {
            FirebaseHandler.getInstance().addFavoriteBusiness(user, business);
            toDraw = R.drawable.ic_is_favorite;
        }
        menu.findItem(R.id.action_favorite).setIcon(toDraw);
    }

    public void showGallery(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        if(!ownedBusiness) {
            intent.putExtra("business", business);
            intent.putExtra("galleryFolder", galleryFolder.getAbsolutePath());
        }
        startActivityForResult(intent, RC_GALLERY);
    }

    @Override
    public synchronized void onImageDownloaded(String businessID, final String imageName, boolean successful) {
        if(!business.getId().equals(businessID) || !successful)
            return;

        runOnUiThread(() -> {
            if(business.getLogo() != null && imageName.equals(business.getLogo())){
                ImageView image = findViewById(R.id.logoImg);
                File imageFile = new File(galleryFolder, imageName);
                image.setOnClickListener(v -> {
                    viewImageInDefaultViewer(imageFile);
                });
                Picasso.get().load(Uri.fromFile(imageFile)).fit().into(image);
                return;
            }
            findViewById(R.id.noImagesTxt).setVisibility(View.GONE);
            adapter.addDownloadedImage(imageName);
        });
    }
    private void viewImageInDefaultViewer(File imageFile) {
        Uri uri =  FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", imageFile);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ownedBusiness)
            return;
        File[] galleryList = galleryFolder.listFiles();
        if (galleryList != null) {
            for (File image : galleryList) {
                image.delete();
            }
        }
        galleryFolder.delete();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_EDIT_BUSINESS && resultCode == RESULT_OK){
            fillInBusinessDetails();
            adapter.notifyDataSetChanged();
            downloadImages();
        }
        else if(requestCode == RC_GALLERY && ownedBusiness) {
            adapter.notifyDataSetChanged();
            downloadImages();
        }
        else if(requestCode == RC_REVIEWS && !ownedBusiness) {
            adapter.notifyDataSetChanged();
            downloadImages();
            business = data.getParcelableExtra("business");
            ((TextView)findViewById(R.id.reviewsTxt)).setText("(" + business.getReviews().size() + " reviews)");
            ((RatingBar)findViewById(R.id.ratingBar)).setRating(business.getReviewsScore());
        }
    }

    public void onWazeClick(View view) {
        if (business.getCoordinates() == null) {
            Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String url = "https://waze.com/ul?ll=" + business.getCoordinates().getLatitude() + "%2C" +
                    business.getCoordinates().getLongitude() + "&navigate=yes";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // If Waze is not installed, open it in Google Play:
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
            startActivity(intent);
        }
    }
}