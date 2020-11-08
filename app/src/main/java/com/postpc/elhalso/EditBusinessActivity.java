package com.postpc.elhalso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.utils.ImageUploader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class EditBusinessActivity extends AppCompatActivity {

    private Business business;
    private Uri newLogoUri;
    private ImageView logoImg;
    private EditText nameTxt;
    private EditText descriptionTxt;
    private File galleryFolder;
    private boolean startedHere;
    private Menu menu;
    private Spinner categorySpinner;

    private static final int RC_EDIT_GALLERY = 481;
    private static final int RC_CHANGE_LOGO = 543;
    private static final int RC_SET_LOCATION = 870;
    private static final String TAG = "EditBusinessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_business);
        business = ((AppLoader) getApplicationContext()).getBusiness();
        startedHere = business.getName() == null;
        galleryFolder = new File(getFilesDir(), business.getId());
        if(!galleryFolder.exists())
            galleryFolder.mkdir();
        onBusinessUpdate();

        categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.categories));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        categorySpinner.setSelection(0);

        fillInBusinessDetails(savedInstanceState);

        setSupportActionBar((Toolbar) findViewById(R.id.editBusinessToolbar));
        if(!startedHere)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business.getName() != null ? business.getName() : "<No Name>");
        getSupportActionBar().setSubtitle("Edit");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.edit_business_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_save:
                saveBusiness();
                break;
            case R.id.action_logout:
                ((AppLoader)getApplicationContext()).logout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void onBusinessUpdate() {
        final UploadBroadcastReceiver uploadReceiver = ((AppLoader)getApplicationContext()).getUploadReceiver();
        uploadReceiver.getNewImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null || !business.getId().equals(uploadReceiver.getBusinessID()) || !uploadReceiver.isLogo())
                    return;
                if(!uploadReceiver.isUploaded()){
                    business.removeImage(s);
                    return;
                }
                if(uploadReceiver.isLogo()) {
                    business.setLogo(s);
                    File imageFile = new File(galleryFolder, s);
                    Picasso.get().load(Uri.fromFile(imageFile)).fit().into((ImageView)findViewById(R.id.logoImgBtn));
                }
                else {
                    business.addImage(s);
                }
            }
        });
    }

    private void fillInBusinessDetails(Bundle savedInstanceState){
        logoImg = (ImageView) findViewById(R.id.logoImgBtn);
        nameTxt = (EditText) findViewById(R.id.nameTxt);
        descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);

        if(savedInstanceState != null){
            nameTxt.setText(savedInstanceState.getString("name"));
            descriptionTxt.setText(savedInstanceState.getString("description"));
            categorySpinner.setSelection(savedInstanceState.getInt("selected_category"));
            if(savedInstanceState.containsKey("newLogo")){
                Picasso.get().load(Uri.parse(savedInstanceState.getString("newLogo"))).fit().into(logoImg);
            }
            else if(business.getLogo() != null) {
                File logo = new File(galleryFolder, business.getLogo());
                Picasso.get().load(Uri.fromFile(logo)).fit().into(logoImg);
            }
            return;
        }

        nameTxt.setText(business.getName() == null ? "" : business.getName());
        descriptionTxt.setText(business.getDescription() == null ? "" : business.getDescription());
        categorySpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.categories)).indexOf(business.getCategory()));
        if(business.getLogo() != null) {
            File logo = new File(galleryFolder, business.getLogo());
            Picasso.get().load(Uri.fromFile(logo)).fit().into(logoImg);
        }
    }

    public void saveBusiness() {
        if(nameTxt.getText().toString().trim().length() < 3){
            showMessage("Business name is too short.");
            return;
        }

        if(!detailsChanged()){
            finish();
            return;
        }

        business.setName(nameTxt.getText().toString());
        business.setDescription(descriptionTxt.getText().toString());
        business.setCategory((String) categorySpinner.getSelectedItem());

        if(newLogoUri != null) {
            if(business.getLogo() != null) {
                File logo = new File(galleryFolder, business.getLogo());
                logo.delete();
            }
            ImageUploader.addImageUpload(getApplicationContext(), business, newLogoUri, true);
        }
        FirebaseHandler.getInstance().updateEditedBusiness(business);
        if(startedHere) {
            Intent intent = new Intent(this, BusinessActivity.class);
            startActivity(intent);
        }
        setResult(RESULT_OK);
        finish();
    }

    public void changeLogoButton(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), RC_CHANGE_LOGO);
    }

    private boolean detailsChanged() {
        String origDescription = business.getDescription() != null ? business.getDescription() : "";
        String origName = business.getName() != null ? business.getName() : "";
        return !origName.equals(nameTxt.getText().toString())
                || !origDescription.equals(descriptionTxt.getText().toString())
                || (newLogoUri != null)
                || !business.getCategory().equals(categorySpinner.getSelectedItem());

    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void editGalleryButton(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivityForResult(intent, RC_EDIT_GALLERY);
    }

    public void setLocationButton(View view) {
        Intent intent = new Intent(this, BusinessLocationActivity.class);
        startActivityForResult(intent, RC_SET_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_EDIT_GALLERY && resultCode == RESULT_OK) {

        }
        else if(requestCode == RC_CHANGE_LOGO && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                newLogoUri = data.getClipData().getItemAt(0).getUri();
            } else if (data.getData() != null) {
                newLogoUri = data.getData();
            }
            if(newLogoUri != null) {
                getContentResolver().takePersistableUriPermission(newLogoUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            Picasso.get().load(newLogoUri).fit().into(logoImg);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", nameTxt.getText().toString());
        outState.putString("description", descriptionTxt.getText().toString());
        outState.putInt("selected_category", categorySpinner.getSelectedItemPosition());
        if(newLogoUri != null)
            outState.putString("newLogo", newLogoUri.toString());
    }
}