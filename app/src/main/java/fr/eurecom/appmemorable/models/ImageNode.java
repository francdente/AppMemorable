package fr.eurecom.appmemorable.models;

import android.graphics.drawable.Drawable;

import fr.eurecom.appmemorable.R;

public class ImageNode implements ContentNode {
    private String text, author;
    private int image, day, album;
    public ImageNode(){

    }
    public ImageNode(int album, int day, String author, String text, int image) {
        this.album = album;
        this.day = day;
        this.author = author;
        this.text = text;
        this.image = image;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }
}
