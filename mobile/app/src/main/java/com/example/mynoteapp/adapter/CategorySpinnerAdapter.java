package com.example.mynoteapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mynoteapp.model.Category;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<Category> {

    private final Context context;
    private final List<Category> categories;

    public CategorySpinnerAdapter(@NonNull Context context, @NonNull List<Category> categories) {
        super(context, android.R.layout.simple_spinner_item, categories);
        this.context = context;
        this.categories = categories;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        Category category = getItem(position);
        if (category != null) {
            view.setText(category.getName());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        Category category = getItem(position);
        if (category != null) {
            view.setText(category.getName());
        }
        return view;
    }
}
