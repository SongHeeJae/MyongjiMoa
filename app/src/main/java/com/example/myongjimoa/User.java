package com.example.myongjimoa;

public class User {
    String id;
    String email_id;
    String nickname;
    String major;
    String number;
    String name;
    String date;
    boolean admin; // true 관리자

    public User(String id, String email_id, String nickname, String major, String number, String name, String date, boolean admin) {
        this.id = id;
        this.email_id = email_id;
        this.nickname = nickname;
        this.major = major;
        this.number = number;
        this.name = name;
        this.date = date;
        this.admin = admin;
    }

    public String getId() { return id; }

    public String getDate() { return date; }

    public String getMajor() { return major; }

    public String getNumber() { return number; }

    public String getName() { return name; }

    public String getEmail_id() {
        return email_id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname=nickname;
    }

    public void setMajor(String major) {
        this.major=major;
    }

    public boolean getAdmin() {
        return admin;
    }
}