package fr.eurecom.appmemorable.models;

import java.time.LocalDateTime;
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

    public String getTimeFromCreation(LocalDateTime now) {
        String time = "";
        if (now.getYear() - creationDate.getYear() > 0) {
            time = (now.getYear() - creationDate.getYear()) + " years ago";
        } else if (now.getMonthValue() - creationDate.getMonthValue() > 0) {
            time = (now.getMonthValue() - creationDate.getMonthValue()) + " months ago";
        } else if (now.getDayOfMonth() - creationDate.getDayOfMonth() > 0) {
            time = (now.getDayOfMonth() - creationDate.getDayOfMonth()) + " days ago";
        } else if (now.getHour() - creationDate.getHour() > 0) {
            time = (now.getHour() - creationDate.getHour()) + " hours ago";
        }
        else {
            time = (now.getMinute() - creationDate.getMinute()) + " minutes ago";
        }
        return time;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
