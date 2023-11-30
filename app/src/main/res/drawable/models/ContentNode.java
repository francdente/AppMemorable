package fr.eurecom.memorable.models;

import android.content.Context;
import android.view.View;

import java.io.Serializable;

public abstract class ContentNode implements Serializable {

    private int album;
    private int day;

    public ContentNode(int album, int day) {
        this.album = album;
        this.day = day;
    }

    /**
     Create and return the view that will represent the given node in the recyclerview
     */
    public abstract View createView(Context context);
    /**
     Create and return the view that will represent the not editable details of the node
     */
    public abstract View createDetailedView(Context context);

    /**
     Create and return the view that will represent the editable details of the node
     */
    public abstract View createEditView(Context context);



}
