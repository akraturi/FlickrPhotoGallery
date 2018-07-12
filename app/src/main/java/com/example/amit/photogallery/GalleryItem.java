package com.example.amit.photogallery;

import com.google.gson.annotations.SerializedName;

//This class serves as a model object for a single gallary item
public class GalleryItem {
    @SerializedName("id")
    private String mId;
    @SerializedName("title")
    private String mCaption;
    @SerializedName("url_s")
    private String mUrl;
    @Override
    public String toString()
    {
        return mCaption;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmCaption() {
        return mCaption;
    }

    public void setmCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
