package fr.eurecom.appmemorable.models;

import android.util.Log;

//support classes used for serializing objects when sending and receiving data from firebase (because ContentNode is an abstract class)
public class ConcreteNode {
    private String text, day, album;
    private User user;
    private String imageUrl = null;
    private String audioUrl = null;

    private String duration = null;

    public ConcreteNode() {
    }

    public ConcreteNode(ContentNode node) {
        if (node instanceof TextNode) {
            this.text = ((TextNode) node).getText();
            this.user = node.getUser();
            this.day = node.getTimestamp();
            this.album = node.getAlbum();
        } else if (node instanceof AudioNode){
            this.text = ((AudioNode) node).getText();
            this.user = node.getUser();
            this.day = node.getTimestamp();
            this.album = node.getAlbum();
            this.audioUrl = ((AudioNode) node).getAudioUrl();
            this.duration = ((AudioNode) node).getDuration();
        } else if (node instanceof ImageNode) {
            this.text = ((ImageNode) node).getText();
            this.user = node.getUser();
            this.day = node.getTimestamp();
            this.album = node.getAlbum();
            this.imageUrl = ((ImageNode) node).getImage();
        }

    }


    public ContentNode IntoContentNode() {

        if(this.imageUrl != null){
            return new ImageNode(album, day, user, text, imageUrl);
        } else if (this.audioUrl != null) {

            return new AudioNode(album, day, user, text, audioUrl, duration);
        }
        else{

            return new TextNode(album, day, user, text);
        }

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getImage() {
        return imageUrl;
    }

    public void setImage(String image) {
        this.imageUrl = image;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
