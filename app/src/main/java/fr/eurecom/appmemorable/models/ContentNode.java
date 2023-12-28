package fr.eurecom.appmemorable.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class ContentNode {
    private String id;
    private String album, timestamp;
    private User user;

    public ContentNode(String album, String timestamp, User user) {
        this.album = album;
        this.timestamp = timestamp;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessageDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTimestamp = LocalDateTime.parse(timestamp);
        if (now.getYear() == dateTimestamp.getYear() && now.getMonthValue() == dateTimestamp.getMonthValue() && now.getDayOfMonth() == dateTimestamp.getDayOfMonth()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Today at' hh:mm a");
            return dateTimestamp.format(formatter);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

        // Format the date
        String formattedDate = dateTimestamp.format(formatter);

        return formattedDate;
    }
}
