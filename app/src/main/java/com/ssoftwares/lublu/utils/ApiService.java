package com.ssoftwares.lublu.utils;
import com.google.gson.JsonObject;
import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @FormUrlEncoded
    @POST("auth/local")
    Call<JsonObject> login(
            @Field("identifier") String email,
            @Field("password") String password
    );

    @Multipart
    @POST("{endpoint}")
    Call<JsonObject> createEntry(
            @Header("Authorization") String token,
            @Path("endpoint") String endpoint,
            @Part("data") RequestBody data,
            @Part List<MultipartBody.Part> files
            );

    @Multipart
    @POST("storymakers")
    Call<JsonObject> buildApp(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part icon,
            @Part MultipartBody.Part config
    );
}
