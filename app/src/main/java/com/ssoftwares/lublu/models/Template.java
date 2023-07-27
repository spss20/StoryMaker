package com.ssoftwares.lublu.models;

import java.io.Serializable;

import io.realm.RealmObject;

public class Template extends RealmObject implements Serializable {

    private String id;
    private int imageId;
    private int layoutId;
    private String text;
    private String imageBase64;
    private float textSize;

    public Template(){}

    public Template(String id, int imageId, int layoutId) {
        this.id = id;
        this.imageId = imageId;
        this.layoutId = layoutId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }
}
