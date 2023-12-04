package fr.eurecom.appmemorable.models;

public abstract class ContentNode {
    private String id;
    private String album, day;
    private User user;

    public ContentNode(String album, String day, User user) {
        this.album = album;
        this.day = day;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
