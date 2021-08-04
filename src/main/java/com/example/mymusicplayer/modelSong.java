package com.example.mymusicplayer;

import android.net.Uri;

public class modelSong {
    private long id;
    private String title;
    private String artist;
    String audioDuration;
    Uri audioUri;


    public modelSong(long songID, String songTitle, String songArtist,Uri audioUri,String audioDuration) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        this.audioUri = audioUri;
        this.audioDuration = audioDuration;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(String audioDuration) {
        this.audioDuration = audioDuration;
    }

    public Uri getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(Uri audioUri) {
        this.audioUri = audioUri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
