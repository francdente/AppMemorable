package fr.eurecom.appmemorable.models;

public abstract class ContentNode {
    private String id;
    private String album, day;

    public ContentNode(String album, String day) {
        this.album = album;
        this.day = day;
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
}
