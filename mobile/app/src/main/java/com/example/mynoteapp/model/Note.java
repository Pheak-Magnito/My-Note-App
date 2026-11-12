package com.example.mynoteapp.model;

import com.google.gson.annotations.SerializedName;

public class Note {
    @SerializedName("id")
    private int id;

    @SerializedName(value = "title", alternate = {"name", "subject"})
    private String title;

    @SerializedName(value = "content", alternate = {"body", "description", "details"})
    private String content;

    @SerializedName(value = "date", alternate = {"created_at", "updated_at", "date_time"})
    private String date;

    @SerializedName(value = "category_id", alternate = {"categoryId"})
    private Integer categoryId;

    @SerializedName("category")
    private Category category;

    public Note() {
    }

    public Note(int id, String title, String content, String date, Integer categoryId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.categoryId = categoryId;
    }

    public Note(String title, String content, String date, Integer categoryId) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCategoryName() {
        if (category != null && category.getName() != null) {
            return category.getName();
        }
        return "General";
    }
}
