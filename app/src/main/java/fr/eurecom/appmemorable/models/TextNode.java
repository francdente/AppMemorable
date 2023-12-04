package fr.eurecom.appmemorable.models;

public class TextNode extends ContentNode {

    private String text;

    public TextNode(String album, String day, User user, String text) {
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
