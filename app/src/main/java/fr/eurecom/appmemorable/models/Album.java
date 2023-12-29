package fr.eurecom.appmemorable.models;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class Album {
    private HashMap<String, ContentNode> nodes;
    private String id, title;

    private List<User> users;
    private LocalDateTime creationDate;

    public Album(){
        nodes = new HashMap<>();
    }
    public Album(String title, HashMap<String, ContentNode> nodes, List<User> users, LocalDateTime creationDate) {
        this.nodes = nodes;
        this.title = title;
        this.creationDate = creationDate;
        this.users = users;
    }

    public HashMap<String, ContentNode> getNodes() {
        return nodes;
    }

    public void addNode(String key, ContentNode node) {
        nodes.put(key, node);
    }

    public void setNodes(HashMap<String, ContentNode> nodes) {
        this.nodes = nodes;
    }
    public int getLength() {
        return nodes.size();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getTimeOfCreation() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateTimestamp = creationDate;
        if (now.getYear() == dateTimestamp.getYear() && now.getMonthValue() == dateTimestamp.getMonthValue() && now.getDayOfMonth() == dateTimestamp.getDayOfMonth()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Today at' hh:mm a");
            return dateTimestamp.format(formatter);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

        // Format the date
        String formattedDate = dateTimestamp.format(formatter);
        formattedDate = capitalizeMonth(formattedDate);
        return formattedDate;
    }

    private String capitalizeMonth(String date) {
        // Split the date string by spaces
        String[] parts = date.split(" ");

        // Capitalize the first letter of the month (assuming it's the first word after the month abbreviation)
        if (parts.length >= 2) {
            parts[0] = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
        }

        // Join the parts back together
        return String.join(" ", parts);
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @NonNull
    public String toString(){
        return title;
    }
}
