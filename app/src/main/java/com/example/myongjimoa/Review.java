package com.example.myongjimoa;

import java.util.ArrayList;

public class Review {
    String id;
    String description;
    String number;
    String major;
    String nickname;
    float score;
    String date;
    ArrayList<String> images;

    public Review(String id, String description, String number, String major, String nickname, float score, String date, ArrayList<String> images) {
        this.images = new ArrayList<String>();
        this.id = id;
        this.description = description;
        this.number = number;
        this.major = major;
        this.nickname = nickname;
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

    public String getNumber() {
        return number;
    }

    public String getMajor() {
        return major;
    }

    public ArrayList<String> getImages() {
        return images;
    }
    public String getNickname() {
        return nickname;
    }
}
