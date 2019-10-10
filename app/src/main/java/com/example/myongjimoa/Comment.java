package com.example.myongjimoa;

public class Comment {
    String id;
    String number;
    String major;
    String comment;
    String date;
    String nickname;

    public Comment(String id, String number, String major, String comment, String date, String nickname) {
        this.id = id;
        this.number = number;
        this.major = major;
        this.nickname = nickname;
        this.comment = comment;
        this.date = date;
    }

    String getId() { return id; }

    String getNumber() {
        return number;
    }

    String getMajor() {
        return major;
    }

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
