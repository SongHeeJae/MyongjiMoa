package com.example.myongjimoa;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ConnectDB {
    final String Base_URL = "http://13.209.77.0/myongjimoa/";

    @FormUrlEncoded
    @POST("download_post.php")
    Call<List<Post>> downloadPost (
            @Field("board_title") String board_title,
            @Field("count_board_id") String count_board_id
    );

    @FormUrlEncoded
    @POST("download_comments.php")
    Call<List<Comment>> downloadComments(
            @Field("board_title") String board_title,
            @Field("board_id") String board_id
    );

    //@Multipart
    @FormUrlEncoded
    @POST("write_post.php")
    Call<String> writePost(
            @Field("board_title") String board_title,
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
            @Field("board_title") String board_title,
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
}
