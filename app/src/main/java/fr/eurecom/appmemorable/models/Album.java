package fr.eurecom.appmemorable.models;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private List<ContentNode> nodes;
    private int id;
    private String title;

    public Album(){
        nodes = new ArrayList<>();
    }
    public Album(int id, String title, List<ContentNode> nodes) {
        this.nodes = nodes;
        this.id = id;
        this.title = title;
    }

    public List<ContentNode> getNodes() {
        return nodes;
    }

    public void addNode(ContentNode node) {
        nodes.add(node);
    }

    public void setNodes(List<ContentNode> nodes) {
        this.nodes = nodes;
    }

    public int getId(){
        return id;
    }

    public int getLength() {
        return nodes.size();
    }

    public String getTitle() {
        return title;
    }
}
