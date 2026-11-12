package com.example.mynoteapp.api;

import com.example.mynoteapp.model.Category;
import com.example.mynoteapp.model.Note;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // --- Notes API Endpoints ---

    @GET("notes")
    Call<List<Note>> getNotes();

    @GET("notes")
    Call<List<Note>> searchNotes(@Query("search") String query);

    @GET("notes/{id}")
    Call<Note> getNoteById(@Path("id") int id);

    @POST("notes")
    Call<Note> createNote(@Body Note note);

    @PUT("notes/{id}")
    Call<Note> updateNote(@Path("id") int id, @Body Note note);

    @DELETE("notes/{id}")
    Call<Void> deleteNote(@Path("id") int id);

    // --- Categories API Endpoints ---

    @GET("categories")
    Call<List<Category>> getCategories();

    @POST("categories")
    Call<Category> createCategory(@Body Category category);

    @PUT("categories/{id}")
    Call<Category> updateCategory(@Path("id") int id, @Body Category category);

    @DELETE("categories/{id}")
    Call<Void> deleteCategory(@Path("id") int id);
}
