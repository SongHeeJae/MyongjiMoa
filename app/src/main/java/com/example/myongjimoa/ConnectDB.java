package com.example.myongjimoa;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ConnectDB {
    final String Base_URL = "http://13.209.77.0/myongjimoa/";

    //@FormUrlEncoded
    @POST("download_board.php")
    Call<List<Board>> downloadBoard();

    @FormUrlEncoded
    @POST("download_post.php")
    Call<List<Post>> downloadPost (
            @Field("board_title_id") String board_title_id,
            @Field("count_board_id") String count_board_id
    );

    @FormUrlEncoded
    @POST("download_comments.php")
    Call<List<Comment>> downloadComments(
            @Field("board_id") String board_id
    );

    //@Multipart
    @FormUrlEncoded
    @POST("write_post.php")
    Call<String> writePost(
            @Field("board_title_id") String board_title_id,
            @Field("user_id") String user_id,
            @Field("title") String title,
            @Field("description") String description,
            @Field("image_path[]") ArrayList<String> image_path,
            @Field("date") String date
    );

    @FormUrlEncoded
    @POST("write_comment.php")
    Call<String> writeComment(
            @Field("board_id") String board_id,
            @Field("user_id") String user_id,
            @Field("comment") String comment,
            @Field("date") String date
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<User> userLogin(
            @Field("email_id") String email_id,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("add_user.php")
    Call<String> addUser(
            @Field("email_id") String email_id,
            @Field("password") String password,
            @Field("nickname") String nickname,
            @Field("major") String major,
            @Field("number") String number,
            @Field("name") String name,
            @Field("date") String date
    );

    @FormUrlEncoded
    @POST("download_restaurant.php")
    Call<List<Restaurant>> downloadRestaurant (
            @Field("category") String category,
            @Field("order") String order
    );

    @FormUrlEncoded
    @POST("download_review.php")
    Call<ReviewResult> downloadReview (
            @Field("restaurant_id") String restaurant_id,
            @Field("count_review_id") String count_review_id
    );

    @FormUrlEncoded
    @POST("write_review.php")
    Call<String> writeReview(
            @Field("restaurant_id") String restaurant_id,
            @Field("description") String description,
            @Field("user_id") String user_id,
            @Field("score") float score,
            @Field("date") String date,
            @Field("image_path[]") ArrayList<String> image_path
    );

   // @POST("/")
   // Call<String> downloadMenu();

    @FormUrlEncoded
    @POST("recommend_post.php")
    Call<String> recommendPost(
            @Field("board_id") String board_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("modify_user.php")
    Call<String> modifyUser(
            @Field("user_id") String user_id,
            @Field("user_nickname") String user_nickname,
            @Field("user_major") String usr_major
    );

    @FormUrlEncoded
    @POST("download_search_post.php")
    Call<List<Post>> downloadSearchPost (
            @Field("board_title_id") String board_title_id,
            @Field("count_board_id") String count_board_id,
            @Field("search_text") String search
    );

    @FormUrlEncoded
    @POST("remove_post.php")
    Call<String> removePost(
            @Field("board_id") String board_id
    );

    @FormUrlEncoded
    @POST("report_post.php")
    Call<String> reportPost(
            @Field("board_id") String board_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("report_comment.php")
    Call<String> reportComment(
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("remove_comment.php")
    Call<String> removeComment(
            @Field("comment_id") String comment_id
    );

    @FormUrlEncoded
    @POST("report_review.php")
    Call<String> reportReview(
            @Field("review_id") String review_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("remove_review.php")
    Call<String> removeReview(
            @Field("review_id") String review_id
    );

}
