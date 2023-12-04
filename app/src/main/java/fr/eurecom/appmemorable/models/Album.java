package fr.eurecom.appmemorable.models;

import java.util.HashMap;

public class Album {
    private HashMap<String, ContentNode> nodes;
    private String id, title;

    public Album(){
        nodes = new HashMap<>();
    }
    public Album(String title, HashMap<String, ContentNode> nodes) {
        this.nodes = nodes;
        this.title = title;
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

}
