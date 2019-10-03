package com.example.myongjimoa;

public class User {
    String id;
    String email_id;
    String nickname;
    String major;
    String number;
    String name;
    String date;

    public User(String[] user_info) {
        id = user_info[0];
        email_id = user_info[1];
        nickname = user_info[2];
        major = user_info[3];
        number = user_info[4];
        name = user_info[5];
        date = user_info[6];
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
}
