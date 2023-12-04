package fr.eurecom.appmemorable.models;

import fr.eurecom.appmemorable.models.AudioNode;
import fr.eurecom.appmemorable.models.ContentNode;
import fr.eurecom.appmemorable.models.ImageNode;
import fr.eurecom.appmemorable.models.TextNode;
import fr.eurecom.appmemorable.models.User;

//support classes used for serializing objects when sending and receiving data from firebase (because ContentNode is an abstract class)
public class ConcreteNode {
    private String text, day, album;
    private User user;
    private int image = -1;

    public ConcreteNode() {
    }

    public ConcreteNode(ContentNode node) {
        if (node instanceof TextNode) {
            this.text = ((TextNode) node).getText();
            this.user = node.getUser();
            this.day = node.getDay();
            this.album = node.getAlbum();
        } else if (node instanceof AudioNode) {
            this.text = ((AudioNode) node).getText();
            this.user = node.getUser();
            this.day = node.getDay();
            this.album = node.getAlbum();
        } else if (node instanceof ImageNode) {
            this.text = ((ImageNode) node).getText();
            this.user = node.getUser();
            this.day = node.getDay();
            this.album = node.getAlbum();
            this.image = ((ImageNode) node).getImage();
        }

    }


    public ContentNode IntoContentNode() {
        if (image == -1) {
            return new TextNode(album, day, user, text);
        } else {
            return new ImageNode(album, day, user, text, image);
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

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
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
