package com.example.Capstone;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface MyAPI {

    @Multipart
    @POST("/posts/")
    Call<List<PostItem>> post_posts(
            @Part MultipartBody.Part image,

            @Part("address") RequestBody address,
            @Part("text") RequestBody text,
            @Part("report") RequestBody report,
            @Part("result") RequestBody result,
            @Part("information") RequestBody information
    );

    /*@POST("/posts/")
    Call<PostItem> post_posts(@Body PostItem post);*/

    @PATCH("/posts/{pk}/")
    Call<PostItem> patch_posts(@Path("pk") int pk, @Body PostItem post);

    @DELETE("/posts/{pk}/")
    Call<PostItem> delete_posts(@Path("pk") int pk);

    @GET("/posts/")
    Call<List<PostItem>> get_posts();

    @GET("/posts/{pk}/")
    Call<PostItem> get_post_pk(@Path("pk") int pk);
}