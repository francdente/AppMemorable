package fr.eurecom.appmemorable.models;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ConcreteAlbum {
    HashMap<String, ConcreteNode> concreteNodes = new HashMap<>();
    HashMap<String, User> users = new HashMap<>();
    private String id;
    private String title;

    public ConcreteAlbum() {
    }

    public ConcreteAlbum(Album album) {
        this.id = album.getId();
        this.title = album.getTitle();
        Log.e("ConcreteAlbum", "users" + users.size());
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
        Album album = new Album(this.title, nodes);
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

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }
}
