package com.example.mynoteapp.api;

import com.example.mynoteapp.model.Category;
import com.example.mynoteapp.model.Note;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlexibleDeserializers {

    public static class NoteListDeserializer implements JsonDeserializer<List<Note>> {
        @Override
        public List<Note> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<Note> list = new ArrayList<>();
            Gson gson = new Gson();

            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                for (JsonElement elem : array) {
                    Note note = gson.fromJson(elem, Note.class);
                    if (note != null) list.add(note);
                }
            } else if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                JsonArray array = null;
                if (obj.has("data") && obj.get("data").isJsonArray()) {
                    array = obj.getAsJsonArray("data");
                } else if (obj.has("notes") && obj.get("notes").isJsonArray()) {
                    array = obj.getAsJsonArray("notes");
                }
                if (array != null) {
                    for (JsonElement elem : array) {
                        Note note = gson.fromJson(elem, Note.class);
                        if (note != null) list.add(note);
                    }
                }
            }
            return list;
        }
    }

    public static class NoteSingleDeserializer implements JsonDeserializer<Note> {
        @Override
        public Note deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new Gson();
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                if (obj.has("data") && obj.get("data").isJsonObject()) {
                    return gson.fromJson(obj.getAsJsonObject("data"), Note.class);
                } else if (obj.has("note") && obj.get("note").isJsonObject()) {
                    return gson.fromJson(obj.getAsJsonObject("note"), Note.class);
                }
                return gson.fromJson(obj, Note.class);
            }
            return null;
        }
    }

    public static class CategoryListDeserializer implements JsonDeserializer<List<Category>> {
        @Override
        public List<Category> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            List<Category> list = new ArrayList<>();
            Gson gson = new Gson();

            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                for (JsonElement elem : array) {
                    Category cat = gson.fromJson(elem, Category.class);
                    if (cat != null) list.add(cat);
                }
            } else if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                JsonArray array = null;
                if (obj.has("data") && obj.get("data").isJsonArray()) {
                    array = obj.getAsJsonArray("data");
                } else if (obj.has("categories") && obj.get("categories").isJsonArray()) {
                    array = obj.getAsJsonArray("categories");
                }
                if (array != null) {
                    for (JsonElement elem : array) {
                        Category cat = gson.fromJson(elem, Category.class);
                        if (cat != null) list.add(cat);
                    }
                }
            }
            return list;
        }
    }
}
