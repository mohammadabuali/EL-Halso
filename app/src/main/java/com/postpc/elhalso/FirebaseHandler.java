package com.postpc.elhalso;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.postpc.elhalso.callbacks.BusinessListReadyCallback;
import com.postpc.elhalso.data.Business;
import com.postpc.elhalso.data.Review;
import com.postpc.elhalso.data.User;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHandler {

    private static FirebaseHandler firebaseHandler;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final MutableLiveData<Boolean> updateDone = new MutableLiveData<>();
    private Object objectToUpdate;

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";
    private static final String LOCAL_DOWNLOAD_FOLDER = "myproject";

    private FirebaseHandler() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static FirebaseHandler getInstance() {
        if (firebaseHandler == null)
            firebaseHandler = new FirebaseHandler();

        return firebaseHandler;
    }

    public LiveData<Boolean> getUpdate() {
        updateDone.setValue(false);
        objectToUpdate = null;
        return updateDone;
    }

    public Object getUpdatedObject() {
        return objectToUpdate;
    }

    public void fetchBusinessForUser(final User user) {
        Log.d(TAG, "starting");

        // user starting new business
        if (user.getBusinessID() == null) {
            DocumentReference docR = firestore.collection(BUSINESS).document();
            final Business business = new Business(docR.getId());
            // adding business
            docR.set(business).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // adding businessID to user
                    firestore.collection(USERS).document(user.getId()).update("businessID",
                            business.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setBusinessID(business.getId());
                            objectToUpdate = business;
                            updateDone.postValue(true);
                            if (!task.isSuccessful()) {
                                Log.d(TAG, "failed: " + task.getException().toString());
                            }
                        }
                    });
                }
            });
            return;
        }

        // user already has a business
        firestore.collection(BUSINESS).document(user.getBusinessID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Business business = task.getResult().toObject(Business.class);
                    objectToUpdate = business;
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "Fetching user failed.");
                }
            }
        });
    }

    public void updateOrCreateFirebaseUser(final User user) {
        firestore.collection(USERS).document(user.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snap = task.getResult();
                    if (snap != null && snap.exists()) {
                        User userFetched = snap.toObject(User.class);
                        user.setBusinessID(userFetched.getBusinessID());
                        user.setFavorites(userFetched.getFavorites());
                        user.setRadius(userFetched.getRadius());
                        user.setFirstLogin(userFetched.isFirstLogin());
                        objectToUpdate = user;
                        updateDone.postValue(true);
                    } else {
                        firestore.collection(USERS).document(user.getId()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    objectToUpdate = user;
                                    updateDone.postValue(true);
                                } else
                                    Log.d(TAG, "failed to create user");
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "failed to get user");
                }
            }
        });
    }

    public void businessListener(BusinessListReadyCallback callback) {
        firestore.collection(BUSINESS).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "businessListener: ", e);
                return;
            } else if (snapshots == null) {
                Log.w(TAG, "businessListener: Empty snapshot");
                return;
            }
            List<Business> businessList = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots) {
                if (doc.get("name") != null) {
                    businessList.add(doc.toObject(Business.class));
                }
            }
            callback.onBusinessListReady(businessList);
        });
    }

    public void updateUserRadius(User user) {
        firestore.collection(USERS).document(user.getId()).update("radius", user.getRadius()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "successfully updated user's radius");
                    objectToUpdate = user; // probably no need
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "failed to update user's radius");
                }
            }
        });
    }

    public void updateBusinessLocation(Business business) {
        firestore.collection(BUSINESS).document(business.getId()).update("coordinates", business.getCoordinates()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "successfully updated business's coordinates");
                    objectToUpdate = business;
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "failed to update business's coordinates");
                }
            }
        });
    }

    public void updateUserFirstLogin(User user) {
        firestore.collection(USERS).document(user.getId()).update("firstLogin", user.isFirstLogin())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateUserFirstLogin: Successfully updated first login");
                        objectToUpdate = user;
                        updateDone.postValue(true);
                    } else {
                        Log.d(TAG, "updateUserFirstLogin: Failed to update first login", task.getException());
                    }
                });
    }

    public void fetchCategoryBusinesses(String category) {
        firestore.collection(BUSINESS).whereEqualTo("category", category).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "successfully queried " + task.getResult().size() + " businesses");
                    ArrayList<Business> businesses = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        if (doc.getString("name") != null && !doc.getString("name").equals(""))
                            businesses.add(doc.toObject(Business.class));
                    }
                    objectToUpdate = businesses;
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "failed to query");
                }
            }
        });
    }

    public void fetchNearbyBusinesses(final GeoPoint myLocation, final double distance) {

        firestore.collection(BUSINESS).whereGreaterThanOrEqualTo("name", "")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "successfully queried " + task.getResult().size() + " businesses");
                    ArrayList<Business> businesses = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        GeoPoint point = doc.getGeoPoint("coordinates");
                        if (point != null && calculateDistance(myLocation, point) <= distance * 1000) {
                            businesses.add(doc.toObject(Business.class));
                        }
                    }
                    objectToUpdate = businesses;
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "failed to query");
                }
            }
        });
    }

    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        float[] result = new float[1];
        Location.distanceBetween(point1.getLatitude(), point1.getLongitude(), point2.getLatitude(), point2.getLongitude(), result);
        return result[0];
    }

    private GeoPoint calculateGeopointAtDistanceFrom(GeoPoint location, double distance, double bearing) {
//        double dist = distance/6371.0;
//        double brng = Math.toRadians(bearing);
//        double lat1 = Math.toRadians(location.getLatitude());
//        double lon1 = Math.toRadians(location.getLongitude());
//
//        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
//        double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
//        double lon2 = lon1 + a;
//
//        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
//        return new GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lon2));

        double brngRad = Math.toRadians(bearing);
        double latRad = Math.toRadians(location.getLatitude());
        double lonRad = Math.toRadians(location.getLongitude());
        double distFrac = distance / 6371.0;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        return new GeoPoint(Math.toDegrees(latitudeResult), Math.toDegrees(longitudeResult));
    }

    public void updateBusiness(final Business newBusiness) {
        firestore.collection(BUSINESS).document(newBusiness.getId()).set(newBusiness).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    objectToUpdate = newBusiness;
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "failed to update business.");
                }
            }
        });
    }

    public void deleteImageForBusiness(final Business business, final String image) {
        business.removeImage(image);
        storage.getReference().child(business.getId() + "/" + image).delete().continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Image removed from business storage");
                } else {
                    Log.d(TAG, "Image failed to be removed from business storage");
                }
                return firestore.collection(BUSINESS).document(business.getId()).update("gallery", FieldValue.arrayRemove(image));
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Image removed from business firestore");
                } else {
                    Log.d(TAG, "Image failed to be removed from business firestore");
                }
            }
        });
    }

    public void addReview(Business business, Review review) {
        updateBusinessReviews(business, review, true);
    }

    public void removeReview(Business business, Review review) {
        updateBusinessReviews(business, review, false);
    }

    private void updateBusinessReviews(final Business business, Review review, boolean isAdding) {
        FieldValue value;
        if (isAdding) {
            business.addReview(review);
            value = FieldValue.arrayUnion(review);
        } else {
            business.removeReview(review);
            value = FieldValue.arrayRemove(review);
        }
        firestore.collection(BUSINESS).document(business.getId()).update("reviews", value).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                objectToUpdate = business;
                updateDone.postValue(true);
            } else {
                Log.d(TAG, "failed to add review");
            }
        });
    }

    public void addFavoriteBusiness(User user, Business business) {
        updateUserFavorites(user, business, true);
    }

    public void removeFavoriteBusiness(User user, Business business) {
        updateUserFavorites(user, business, false);
    }

    private void updateUserFavorites(User user, Business business, boolean isAdding) {
        FieldValue value;
        if (isAdding) {
            user.addFavoriteBusiness(business);
            value = FieldValue.arrayUnion(business.getId());
        } else {
            user.removeFavoriteBusiness(business);
            value = FieldValue.arrayRemove(business.getId());
        }
        firestore.collection(USERS).document(user.getId()).update("favorites", value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateDone.postValue(true);
                } else {
                    Log.d(TAG, "adding favorite failed");
                }
            }
        });
    }

    public void updateEditedBusiness(Business business) {
        firestore.collection(BUSINESS).document(business.getId()).update("name", business.getName(),
                "description", business.getDescription(),
                "category", business.getCategory()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "business updated edits successfully");
                } else {
                    Log.d(TAG, "failed to update business edits");
                }
            }
        });
    }

    public void updateGalleryForBusiness(Business business) {
        firestore.collection(BUSINESS).document(business.getId()).update("gallery", business.getGallery())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "gallery update successfully");
                        } else {
                            Log.d(TAG, "failed to update gallery");
                        }
                    }
                });
    }
}
