package fr.eurecom.appmemorable.models;

import android.graphics.drawable.Drawable;

import fr.eurecom.appmemorable.R;

public class ImageNode extends ContentNode {
    private String text;
    private String imageUrl;
    public ImageNode(String album, String day, User user, String text, String imageUrl) {
        super(album, day, user);
        this.text = text;
        this.imageUrl = imageUrl;
    }




    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
