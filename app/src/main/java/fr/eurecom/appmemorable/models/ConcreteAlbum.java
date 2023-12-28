package fr.eurecom.appmemorable.models;

import android.util.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcreteAlbum {
    HashMap<String, ConcreteNode> concreteNodes = new HashMap<>();
    private String id, title, creationDate;
    private List<User> users = new ArrayList<>();

    public ConcreteAlbum() {
    }

    public ConcreteAlbum(Album album) {
        this.id = album.getId();
        this.title = album.getTitle();
        this.creationDate = album.getCreationDate().toString();
        this.users = album.getUsers();
        for (Map.Entry<String, ContentNode> entry : album.getNodes().entrySet()) {
            String key = entry.getKey();
            ContentNode node = entry.getValue();
            concreteNodes.put(key, new ConcreteNode(node));
        }
    }

    public Album IntoAlbum() {
        HashMap<String, ContentNode> nodes = new HashMap<>();
        for (Map.Entry<String, ConcreteNode> entry : concreteNodes.entrySet()) {
            String key = entry.getKey();
            ContentNode node = entry.getValue().IntoContentNode();
            node.setId(key);
            nodes.put(key, node);
        }
        Album album = new Album(this.title, nodes, this.users, LocalDateTime.parse(this.creationDate));
        album.setId(this.id);
        return album;
    }

    public HashMap<String, ConcreteNode> getConcreteNodes() {
        return concreteNodes;
    }

    public void setConcreteNodes(HashMap<String, ConcreteNode> concreteNodes) {
        this.concreteNodes = concreteNodes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
