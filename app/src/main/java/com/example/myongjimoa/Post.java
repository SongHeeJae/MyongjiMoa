package com.example.myongjimoa;

import java.util.ArrayList;

public class Post {
    String id;
    String title;
    String description;
    String e_mail;
    String date;
    String nickname;
    ArrayList<String> images;
    int recommend_num;

    public Post(String id, String title, String description, String e_mail, String date, String nickname, ArrayList<String> images, int recommend_num) {
        this.images = new ArrayList<String>();
        this.images.addAll(images);
        this.id=id;
        this.title = title;
        this.description = description;
        this.e_mail = e_mail;
        this.date = date;
        this.nickname = nickname;
        this.recommend_num = recommend_num;
    }

   /* public void addComment(String comment_id, String e_mail, String comment_nickname, String comment_text, String comment_date) {
        comments.add(new Comment(comment_id, e_mail, comment_nickname, comment_text, comment_date));
    }*/

    public String getTitle() {
        return title;
    }

    public String getDescription() { return description; }

    public String getNickname() { return nickname; }

    public String getDate() { return date; }

    public String getId() { return id; }

    public String getE_mail() { return e_mail; }
   /* public ArrayList<Comment> getComments() {
        return comments;
    }*/

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
