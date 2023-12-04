package fr.eurecom.appmemorable.models;

import android.content.Context;
import android.view.View;

public class AudioNode extends ContentNode {
    String text;
    public AudioNode(String album, String day, User user, String text) {
        super(album, day, user);
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
