package fr.eurecom.memorable.models;

import java.util.ArrayList;

import fr.eurecom.memorable.models.ContentNode;

public class Album {
    private ArrayList<ContentNode> nodes;
    private int id;
    private String title;

    public Album(int id, String title, ArrayList<ContentNode> nodes) {
        this.nodes = nodes;
        this.id = id;
        this.title = title;
    }

    public ArrayList<ContentNode> getNodes() {
        return nodes;
    }

    public void addNode(ContentNode node) {
        nodes.add(node);
    }

    public void setNodes(ArrayList<ContentNode> nodes) {
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
