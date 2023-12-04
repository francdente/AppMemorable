package fr.eurecom.appmemorable.models;

import android.graphics.drawable.Drawable;

import fr.eurecom.appmemorable.R;

public class ImageNode extends ContentNode {
    private String text;
    private int image;
    public ImageNode(String album, String day, User user, String text, int image) {
        super(album, day, user);
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


}
