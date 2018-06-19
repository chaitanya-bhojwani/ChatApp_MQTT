package com.example.affine.chatapp2;

import java.util.List;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    String BASE_URL = "http://b22bd176.ngrok.io";

    @GET("user/chat_history")
    Single<Response<List<HistoryResponseItem>>> getHistory(@Query("topic_id") String topicId);

    @GET("user/details")
    Single<Response<List<HistoryResponseItem>>> getUserDetails(@Query("topic_id") String topicId);

    @GET("user/details/allUsers")
    Single<Response<List<HistoryResponseItem>>> getAllUsers(@Query("topic_id") String topicId);

    class Factory {
        public Factory() {
        }

        public static ApiService createService() {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(logging).build();
            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build().create(ApiService.class);
        }
    }
}
