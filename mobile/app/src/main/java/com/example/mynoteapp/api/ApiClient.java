package com.example.mynoteapp.api;

import com.example.mynoteapp.model.Category;
import com.example.mynoteapp.model.Note;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // Default local API URL as depicted in diagram (10.0.2.2 points to host machine from Android Emulator)
    private static String BASE_URL = "http://10.0.2.2:8000/api/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static void setBaseUrl(String newUrl) {
        if (newUrl == null || newUrl.trim().isEmpty()) {
            return;
        }
        newUrl = newUrl.trim();
        if (!newUrl.endsWith("/")) {
            newUrl += "/";
        }
        BASE_URL = newUrl;
        retrofit = null;
        apiService = null;
    }

    public static String getBaseUrl() {
        if (BASE_URL != null && !BASE_URL.endsWith("/")) {
            BASE_URL += "/";
        }
        return BASE_URL;
    }

    public static ApiService getApiService() {
        String url = getBaseUrl();
        if (apiService == null || retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            Type noteListType = new TypeToken<List<Note>>(){}.getType();
            Type categoryListType = new TypeToken<List<Category>>(){}.getType();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(noteListType, new FlexibleDeserializers.NoteListDeserializer())
                    .registerTypeAdapter(Note.class, new FlexibleDeserializers.NoteSingleDeserializer())
                    .registerTypeAdapter(categoryListType, new FlexibleDeserializers.CategoryListDeserializer())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
