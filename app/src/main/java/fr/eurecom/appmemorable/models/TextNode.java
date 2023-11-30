package fr.eurecom.appmemorable.models;

public class TextNode extends ContentNode {

    private String text, author;


    public TextNode(String album, String day, String author, String text) {
        super(album, day);
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
}
