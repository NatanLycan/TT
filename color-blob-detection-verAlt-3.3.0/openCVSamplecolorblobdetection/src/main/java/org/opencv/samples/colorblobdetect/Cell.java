package org.opencv.samples.colorblobdetect;

import android.graphics.Bitmap;

/**
 * Created by TTII_B095
 */

public class Cell {
    private String title;
//    private Integer img;
    private Bitmap img;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    //    public Integer getImg() {
//        return img;
//    }
//
//    public void setImg(Integer img) {
//        this.img = img;
//    }
}
