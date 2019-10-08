package com.example.myongjimoa;

import java.util.ArrayList;

public class Review {
    String id;
    String description;
    String nickname;
    String user_id;
    float score;
    String date;
    ArrayList<String> images;

    public Review(String id, String description, String nickname, String user_id, float score, String date, ArrayList<String> images) {
        this.images = new ArrayList<String>();
        this.id = id;
        this.description = description;
        this.nickname = nickname;
        this.user_id = user_id;
        this.score = score;
        this.date = date;
        this.images.addAll(images);
    }

    public String getId() {
        return id;
    }

    public float getScore() {
        return score;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getUser_id() {
        return user_id;
    }

    public ArrayList<String> getImages() {
        return images;
    }
    public String getNickname() {
        return nickname;
    }
}
