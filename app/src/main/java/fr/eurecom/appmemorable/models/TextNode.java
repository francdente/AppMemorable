package fr.eurecom.appmemorable.models;

public class TextNode implements ContentNode {

    private String text, author;
    private int album, day;

    public TextNode(){

    }
    public TextNode(int album, int day, String author, String text) {
        this.album = album;
        this.day = day;
        this.author = author;
        this.text = text;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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
}
