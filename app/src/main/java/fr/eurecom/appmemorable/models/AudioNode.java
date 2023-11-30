package fr.eurecom.appmemorable.models;

import android.content.Context;
import android.view.View;

public class AudioNode implements ContentNode {

    int album, day, image;
    String author, text;
    public AudioNode(){

    }

    public AudioNode(int album, int day, String author, String text, int image) {
        this.album = album;
        this.day = day;
        this.author = author;
        this.text = text;
        this.image = image;
    }

    public int getAlbum() {
        return album;
    }

    public void setAlbum(int album) {
        this.album = album;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
