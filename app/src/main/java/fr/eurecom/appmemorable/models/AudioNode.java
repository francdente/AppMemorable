package fr.eurecom.appmemorable.models;

import android.content.Context;
import android.view.View;

public class AudioNode extends ContentNode {
    String author, text;
    public AudioNode(String album, String day, String author, String text) {
        super(album, day);
        this.author = author;
        this.text = text;
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
