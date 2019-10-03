package com.example.myongjimoa;

public class Comment {
    String id;
    String e_mail;
    String comment;
    String date;
    String nickname;

    public Comment(String id, String e_mail, String comment, String date, String nickname) {
        this.id = id;
        this.e_mail = e_mail;
        this.nickname = nickname;
        this.comment = comment;
        this.date = date;
    }

    String getId() { return id; }

    String getE_mail() { return e_mail; }

    String getNickname() {
        return nickname;
    }

    String getComment() {
        return comment;
    }

    String getDate() {
        return date;
    }

}
