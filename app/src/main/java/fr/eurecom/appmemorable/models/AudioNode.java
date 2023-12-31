package fr.eurecom.appmemorable.models;

import android.content.Context;
import android.view.View;

public class AudioNode extends ContentNode {
    String text;
    private String audioUrl;
    private String duration;
    public AudioNode(String album, String timestamp, User user, String text, String audioUrl, String duration) {
        super(album, timestamp, user);
        this.text = text;
        this.audioUrl = audioUrl;
        this.duration = duration;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getDuration() {
        return duration;
    }



}
