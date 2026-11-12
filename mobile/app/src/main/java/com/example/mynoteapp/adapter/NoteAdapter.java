package com.example.mynoteapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynoteapp.R;
import com.example.mynoteapp.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onItemClick(Note note);
        void onEditClick(Note note);
        void onDeleteClick(Note note);
    }

    private final Context context;
    private List<Note> noteList;
    private List<Note> noteListFiltered;
    private final OnNoteClickListener listener;

    // Default category color palette matching the UI picture
    private final int[] categoryColors = new int[]{
            Color.parseColor("#FFB300"), // Yellow/Amber
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#9C27B0"), // Purple
            Color.parseColor("#FF5722")  // Deep Orange
    };

    public NoteAdapter(Context context, List<Note> noteList, OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList != null ? noteList : new ArrayList<>();
        this.noteListFiltered = new ArrayList<>(this.noteList);
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.noteList = notes != null ? notes : new ArrayList<>();
        this.noteListFiltered = new ArrayList<>(this.noteList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        noteListFiltered.clear();
        if (query == null || query.trim().isEmpty()) {
            noteListFiltered.addAll(noteList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Note note : noteList) {
                if ((note.getTitle() != null && note.getTitle().toLowerCase().contains(filterPattern)) ||
                    (note.getContent() != null && note.getContent().toLowerCase().contains(filterPattern)) ||
                    (note.getCategoryName() != null && note.getCategoryName().toLowerCase().contains(filterPattern))) {
                    noteListFiltered.add(note);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteListFiltered.get(position);

        holder.tvTitle.setText(note.getTitle());

        String dateStr = note.getDate() != null && !note.getDate().isEmpty() ? note.getDate() : "";
        holder.tvDate.setText(dateStr);

        // Apply background color to Category Icon Frame
        int color = getCategoryColor(note, position);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dpToPx(8));
        drawable.setColor(color);
        holder.layoutCategoryIcon.setBackground(drawable);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(note);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(note);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteListFiltered.size();
    }

    private int getCategoryColor(Note note, int position) {
        if (note.getCategory() != null && note.getCategory().getColor() != null) {
            try {
                return Color.parseColor(note.getCategory().getColor());
            } catch (Exception ignored) {
            }
        }
        // Fallback color cycling to match screenshot
        return categoryColors[position % categoryColors.length];
    }

    private float dpToPx(int dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        FrameLayout layoutCategoryIcon;
        ImageView ivCategoryIcon;
        TextView tvTitle;
        TextView tvDate;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutCategoryIcon = itemView.findViewById(R.id.layoutCategoryIcon);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvDate = itemView.findViewById(R.id.tvNoteDate);
            btnEdit = itemView.findViewById(R.id.btnEditNote);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
        }
    }
}
