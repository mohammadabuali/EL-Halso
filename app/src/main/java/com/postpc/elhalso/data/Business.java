package com.postpc.elhalso.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class Business implements Parcelable {
    private String id;
    private String name;
    private String category;
    private GeoPoint coordinates;
    private String description;
    private String logo;
    private ArrayList<String> gallery;
    private ArrayList<Review> reviews;

    public Business() {
    }

    public Business(String id) {
        this.id = id;
    }

    public Business(String id, String name, String category, GeoPoint coordinates, String description,
                    String logo, ArrayList<String> gallery, ArrayList<Review> reviews) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.coordinates = coordinates;
        this.description = description;
        this.logo = logo;
        this.gallery = gallery;
        this.reviews = reviews;
    }

    public Business(Business business) {
        this.id = business.id;
        this.name = business.name;
        this.category = business.category;
        this.coordinates = business.coordinates;
        this.description = business.description;
        this.logo = business.logo;
        this.gallery = business.gallery;
        this.reviews = business.reviews;
    }

    protected Business(Parcel in) {
        id = in.readString();
        name = in.readString();
        category = in.readString();
        if (in.readInt() == 1) {
            coordinates = new GeoPoint(in.readDouble(), in.readDouble());
        }
        description = in.readString();
        logo = in.readString();
        gallery = in.createStringArrayList();
        reviews = in.createTypedArrayList(Review.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeInt(coordinates != null ? 1 : 0);
        if (coordinates != null) {
            dest.writeDouble(coordinates.getLatitude());
            dest.writeDouble(coordinates.getLongitude());
        }
        dest.writeString(description);
        dest.writeString(logo);
        dest.writeStringList(gallery);
        dest.writeTypedList(reviews);
    }

    public static final Creator<Business> CREATOR = new Creator<Business>() {
        @Override
        public Business createFromParcel(Parcel in) {
            return new Business(in);
        }

        @Override
        public Business[] newArray(int size) {
            return new Business[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category != null ? category : "Misc";
    }

    public String getName() {
        return name;
    }

    public GeoPoint getCoordinates() {
        return coordinates;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getLogo() {
        return logo;
    }

    public ArrayList<String> getGallery() {
        return gallery == null ? new ArrayList<>() : gallery;
    }

    public ArrayList<Review> getReviews() {
        return reviews == null ? new ArrayList<>() : reviews;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addReview(Review review) {
        reviews = getReviews();
        reviews.add(0, review);
    }

    public void removeReview(Review review) {
        reviews = getReviews();
        reviews.remove(review);
    }

    public void setCoordinates(GeoPoint coordinates) {
        this.coordinates = coordinates;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void addImage(String image) {
        gallery = getGallery();
        if (!gallery.contains(image))
            gallery.add(image);
    }

    public void removeImage(String image) {
        gallery = getGallery();
        gallery.remove(image);
    }

    public void setGallery(ArrayList<String> gallery) {
        this.gallery = gallery;
    }

    public float getReviewsScore() {
        float rating = 0;
        if (getReviews().size() == 0) {
            return 0;
        }
        for (Review review : getReviews()) {
            rating += review.getRating();
        }
        return rating / getReviews().size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void sortReviews() {
        Collections.sort(getReviews());
    }

    @Override
    public String toString() {
        return "Business{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Business business = (Business) o;
        return id.equals(business.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
