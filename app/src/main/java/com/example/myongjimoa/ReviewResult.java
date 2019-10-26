package com.example.myongjimoa;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ReviewResult {
    float score;
    int review_num;
    List<Review> list;

    public ReviewResult(int score, int review_num, List<Review> list) {
        this.list = new ArrayList<>();
        this.score = score;
        this.review_num = review_num;
        this.list.addAll(list);
    }

    public float getScore() {
        return score;
    }

    public int getReview_num() {
        return review_num;
    }

    public Review getReview(int pos) {
        return list.get(pos);
    }

    public int getReviewCount() {
        return list.size();
    }
}
