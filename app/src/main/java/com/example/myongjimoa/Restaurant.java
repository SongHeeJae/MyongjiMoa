package com.example.myongjimoa;

public class Restaurant {
    String id;
    String title;
    String category;
    String telephone;
    String homepage;
    String address;
    String mapx;
    String mapy;
    String restaurant_id;
    String time;
    String menu;
    String image;
    int review_num;
    float score;

    public Restaurant(String id, String title, String cateogory, String telephone, String homepage, String address, String mapx, String mapy, String restaurant_id, String time, String menu, String image, int review_num, float score) {
        this.id=id;
        this.title=title;
        this.category=cateogory;
        this.telephone=telephone;
        this.homepage=homepage;
        this.address=address;
        this.mapx=mapx;
        this.mapy=mapy;
        this.restaurant_id=restaurant_id;
        this.time=time;
        this.menu=menu;
        this.image=image;
        this.review_num=review_num;
        this.score=score;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getId() {
        return id;
    }

    public String getMapy() {
        return mapy;
    }

    public String getMapx() {
        return mapx;
    }

    public String getImage() {
        return image;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getTitle() {
        return title;
    }

    public String getMenu() {
        return menu;
    }

    public int getReview_num() {
        return review_num;
    }

    public float getScore() {
        return score;
    }

    public String getTime() {
        return time;
    }

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setReview_num(int review_num) {
        this.review_num = review_num;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
