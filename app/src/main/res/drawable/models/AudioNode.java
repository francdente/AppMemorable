package fr.eurecom.memorable.models;

import android.content.Context;
import android.view.View;

public class AudioNode extends ContentNode {
    public AudioNode(int album, int day) {
        super(album, day);
    }

    @Override
    public View createView(Context context) {
        return null;
    }

    @Override
    public View createDetailedView(Context context) {
        return null;
    }

    @Override
    public View createEditView(Context context) {
        return null;
    }
}
