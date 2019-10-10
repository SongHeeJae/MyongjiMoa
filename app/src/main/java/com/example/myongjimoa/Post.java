package com.example.myongjimoa;

import java.util.ArrayList;

public class Post {
    String id;
    String title;
    String description;
    String number;
    String major;
    String date;
    String nickname;
    ArrayList<String> images;
    int recommend_num;

    public Post(String id, String title, String description, String number, String major, String date, String nickname, ArrayList<String> images, int recommend_num) {
        this.images = new ArrayList<String>();
        this.images.addAll(images);
        this.id=id;
        this.title = title;
        this.description = description;
        this.number = number;
        this.major = major;
        this.date = date;
        this.nickname = nickname;
        this.recommend_num = recommend_num;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() { return description; }

    public String getNickname() { return nickname; }

    public String getDate() { return date; }

    public String getId() { return id; }

    public String getNumber() {
        return number;
    }

    public String getMajor() {
        return major;
    }

   public ArrayList<String> getImages() {
       return images;
   }

   public int getRecommend_num() {
       return recommend_num;
   }

   public void setRecommend_num(int recommend_num) {
       this.recommend_num = recommend_num;
   }
}
