package com.ssoftwares.lublu.utils;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    private static Retrofit retrofit;
    public static final String BASE_URL = "http://ssoft.xyz:1337";

    public static ApiService create() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    @NonNull
    public static RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(descriptionString, okhttp3.MultipartBody.FORM);
    }

    @NonNull
    public static MultipartBody.Part prepareFilePart(Context context, String keyName, Uri fileUri) throws IOException {
        // create RequestBody instance from file
            InputStream is = context.getContentResolver().openInputStream(fileUri);
            RequestBody requestFile =
                    RequestBody.create(
                            IOUtils.toByteArray(is),
                            MediaType.parse(context.getContentResolver().getType(fileUri))
                    );

            // MultipartBody.Part is used to send also the actual file name
            return MultipartBody.Part.createFormData(keyName,
                    AppUtils.getFileName(context, fileUri), requestFile);
    }
}
