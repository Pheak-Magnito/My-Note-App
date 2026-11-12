package com.example.mynoteapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mynoteapp.adapter.CategorySpinnerAdapter;
import com.example.mynoteapp.adapter.NoteAdapter;
import com.example.mynoteapp.api.ApiClient;
import com.example.mynoteapp.api.ApiService;
import com.example.mynoteapp.model.Category;
import com.example.mynoteapp.model.Note;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private RecyclerView recyclerViewNotes;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabAddNote;
    private ImageView ivSearchIcon, ivCloseSearch, ivMenu;
    private TextView tvAppTitle;
    private LinearLayout layoutSearch;
    private EditText etSearchQuery;

    private NoteAdapter noteAdapter;
    private List<Note> noteList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = ApiClient.getApiService();

        initViews();
        setupRecyclerView();
        setupListeners();
        setupBackPressed();

        // Load data from REST API database
        loadCategoriesAndNotes();
    }

    private void initViews() {
        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabAddNote = findViewById(R.id.fabAddNote);
        ivSearchIcon = findViewById(R.id.ivSearchIcon);
        ivCloseSearch = findViewById(R.id.ivCloseSearch);
        ivMenu = findViewById(R.id.ivMenu);
        tvAppTitle = findViewById(R.id.tvAppTitle);
        layoutSearch = findViewById(R.id.layoutSearch);
        etSearchQuery = findViewById(R.id.etSearchQuery);
    }

    private void setupRecyclerView() {
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(this, noteList, this);
        recyclerViewNotes.setAdapter(noteAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadNotesFromApi);

        fabAddNote.setOnClickListener(v -> showAddEditNoteDialog(null));

        ivSearchIcon.setOnClickListener(v -> openSearch());

        ivCloseSearch.setOnClickListener(v -> closeSearch());

        ivMenu.setOnClickListener(v -> showServerConfigDialog());

        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showServerConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server API Configuration");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(44, 20, 44, 10);

        TextView tvHint = new TextView(this);
        tvHint.setText("Set your Laravel REST API Base URL:");
        tvHint.setTextSize(14);
        tvHint.setTextColor(getColor(R.color.text_primary));
        container.addView(tvHint);

        final EditText etUrl = new EditText(this);
        etUrl.setText(ApiClient.getBaseUrl());
        etUrl.setHint("http://10.0.2.2:8000/api/");
        container.addView(etUrl);

        builder.setView(container);

        builder.setPositiveButton("Connect", (dialog, which) -> {
            String newUrl = etUrl.getText().toString().trim();
            if (!newUrl.isEmpty()) {
                ApiClient.setBaseUrl(newUrl);
                apiService = ApiClient.getApiService();
                Toast.makeText(MainActivity.this, "Connecting to: " + ApiClient.getBaseUrl(), Toast.LENGTH_SHORT).show();
                noteList.clear();
                loadCategoriesAndNotes();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Port 8000", (dialog, which) -> {
            ApiClient.setBaseUrl("http://10.0.2.2:8000/api/");
            apiService = ApiClient.getApiService();
            noteList.clear();
            loadCategoriesAndNotes();
        });

        builder.show();
    }

    private void setupBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutSearch != null && layoutSearch.getVisibility() == View.VISIBLE) {
                    closeSearch();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void openSearch() {
        layoutSearch.setVisibility(View.VISIBLE);
        if (tvAppTitle != null) tvAppTitle.setVisibility(View.GONE);
        if (ivMenu != null) ivMenu.setVisibility(View.GONE);
        if (ivSearchIcon != null) ivSearchIcon.setVisibility(View.GONE);

        etSearchQuery.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etSearchQuery, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void closeSearch() {
        etSearchQuery.setText("");
        layoutSearch.setVisibility(View.GONE);
        if (tvAppTitle != null) tvAppTitle.setVisibility(View.VISIBLE);
        if (ivMenu != null) ivMenu.setVisibility(View.VISIBLE);
        if (ivSearchIcon != null) ivSearchIcon.setVisibility(View.VISIBLE);

        noteAdapter.filter("");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
        }
    }

    private void loadCategoriesAndNotes() {
        apiService.getCategories().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    categoryList = response.body();
                } else {
                    setupDefaultCategories();
                }
                loadNotesFromApi();
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                setupDefaultCategories();
                loadNotesFromApi();
            }
        });
    }

    private void setupDefaultCategories() {
        if (categoryList.isEmpty()) {
            categoryList.add(new Category(1, "Study", "#FFB300"));
            categoryList.add(new Category(2, "Android", "#4CAF50"));
            categoryList.add(new Category(3, "Work", "#2196F3"));
            categoryList.add(new Category(4, "Personal", "#9C27B0"));
        }
    }

    // GET /notes
    private void loadNotesFromApi() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        apiService.getNotes().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    noteList = response.body();
                    matchCategoriesWithNotes(noteList);
                    updateNotesUI(noteList);
                } else {
                    Toast.makeText(MainActivity.this, "API Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    if (noteList.isEmpty()) {
                        showSampleNotes();
                    } else {
                        updateNotesUI(noteList);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Cannot connect to API: " + ApiClient.getBaseUrl(), Toast.LENGTH_LONG).show();
                if (noteList.isEmpty()) {
                    showSampleNotes();
                } else {
                    updateNotesUI(noteList);
                }
            }
        });
    }

    private void matchCategoriesWithNotes(List<Note> notes) {
        if (notes == null || categoryList.isEmpty()) return;
        for (Note n : notes) {
            if (n.getCategory() == null && n.getCategoryId() != null) {
                for (Category c : categoryList) {
                    if (c.getId() == n.getCategoryId()) {
                        n.setCategory(c);
                        break;
                    }
                }
            }
        }
    }

    private void showSampleNotes() {
        if (noteList.isEmpty()) {
            noteList.add(new Note(1, "Learn Retrofit", "Call API...", "2026-10-02 10:30 AM", 1));
            noteList.get(0).setCategory(categoryList.get(0));

            noteList.add(new Note(2, "Study Android", "Build App...", "2026-09-30 09:15 AM", 2));
            noteList.get(1).setCategory(categoryList.get(1));

            noteList.add(new Note(3, "Build Great App", "Deploy to server...", "2026-09-28 04:20 PM", 3));
            noteList.get(2).setCategory(categoryList.get(2));
        }
        updateNotesUI(noteList);
    }

    private void updateNotesUI(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewNotes.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewNotes.setVisibility(View.VISIBLE);
            noteAdapter.setNotes(notes);
            recyclerViewNotes.scrollToPosition(0);
        }
    }

    private void addOrUpdateNoteInList(Note note) {
        if (note == null) return;
        boolean found = false;
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getId() > 0 && noteList.get(i).getId() == note.getId()) {
                noteList.set(i, note);
                found = true;
                break;
            }
        }
        if (!found) {
            noteList.add(0, note);
        }
        updateNotesUI(noteList);
    }

    // --- OnNoteClickListener Interface Implementation ---

    @Override
    public void onItemClick(Note note) {
        showAddEditNoteDialog(note);
    }

    @Override
    public void onEditClick(Note note) {
        showAddEditNoteDialog(note);
    }

    @Override
    public void onDeleteClick(Note note) {
        showDeleteConfirmationDialog(note);
    }

    // --- Add / Edit Dialog ---

    private void showAddEditNoteDialog(final Note existingNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_note, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        final TextInputEditText etNoteTitle = view.findViewById(R.id.etNoteTitle);
        final TextInputEditText etNoteContent = view.findViewById(R.id.etNoteContent);
        final TextInputEditText etNoteDate = view.findViewById(R.id.etNoteDate);
        final Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        ImageButton btnAddCategory = view.findViewById(R.id.btnAddCategory);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        // Ensure default categories exist if list is empty
        setupDefaultCategories();

        // Setup Spinner
        CategorySpinnerAdapter spinnerAdapter = new CategorySpinnerAdapter(this, categoryList);
        spinnerCategory.setAdapter(spinnerAdapter);

        // Date selection
        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US);

        if (existingNote == null) {
            tvDialogTitle.setText(R.string.add_note);
            btnSave.setText(R.string.save_note);
            etNoteDate.setText(dateFormat.format(calendar.getTime()));
        } else {
            tvDialogTitle.setText(R.string.edit_note);
            btnSave.setText(R.string.update_note);
            etNoteTitle.setText(existingNote.getTitle());
            etNoteContent.setText(existingNote.getContent());
            etNoteDate.setText(existingNote.getDate());

            // Select Category in spinner
            if (existingNote.getCategoryId() != null) {
                for (int i = 0; i < categoryList.size(); i++) {
                    if (categoryList.get(i).getId() == existingNote.getCategoryId()) {
                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        }

        etNoteDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (datePicker, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                                (timePicker, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    etNoteDate.setText(dateFormat.format(calendar.getTime()));
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false);
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog(spinnerAdapter));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String title = etNoteTitle.getText() != null ? etNoteTitle.getText().toString().trim() : "";
            String content = etNoteContent.getText() != null ? etNoteContent.getText().toString().trim() : "";
            String dateStr = etNoteDate.getText() != null ? etNoteDate.getText().toString().trim() : "";
            Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

            if (title.isEmpty()) {
                etNoteTitle.setError("Title is required");
                return;
            }

            Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;

            if (existingNote == null) {
                // POST /notes
                Note newNote = new Note(title, content, dateStr, categoryId);
                newNote.setCategory(selectedCategory);
                createNoteApi(newNote, dialog);
            } else {
                // PUT /notes/{id}
                existingNote.setTitle(title);
                existingNote.setContent(content);
                existingNote.setDate(dateStr);
                existingNote.setCategoryId(categoryId);
                existingNote.setCategory(selectedCategory);
                updateNoteApi(existingNote, dialog);
            }
        });

        dialog.show();
    }

    // POST /notes
    private void createNoteApi(final Note note, final AlertDialog dialog) {
        apiService.createNote(note).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                Note createdNote = (response.isSuccessful() && response.body() != null) ? response.body() : note;
                if (createdNote.getCategory() == null) {
                    createdNote.setCategory(note.getCategory());
                }

                // Add to list immediately & refresh RecyclerView UI
                addOrUpdateNoteInList(createdNote);

                Toast.makeText(MainActivity.this, "Note added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                // Re-sync full list from server
                loadNotesFromApi();
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                addOrUpdateNoteInList(note);
                Toast.makeText(MainActivity.this, "Note added to list (Offline)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    // PUT /notes/{id}
    private void updateNoteApi(final Note note, final AlertDialog dialog) {
        apiService.updateNote(note.getId(), note).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                addOrUpdateNoteInList(note);
                Toast.makeText(MainActivity.this, "Note updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadNotesFromApi();
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                addOrUpdateNoteInList(note);
                Toast.makeText(MainActivity.this, "Note updated (Offline)", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    // DELETE /notes/{id}
    private void showDeleteConfirmationDialog(final Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete \"" + note.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNoteApi(note))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNoteApi(final Note note) {
        apiService.deleteNote(note.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                noteList.remove(note);
                updateNotesUI(noteList);
                Toast.makeText(MainActivity.this, "Note deleted successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                noteList.remove(note);
                updateNotesUI(noteList);
                Toast.makeText(MainActivity.this, "Note removed (Offline)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // POST /categories
    private void showAddCategoryDialog(final CategorySpinnerAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        final TextInputEditText etCategoryName = view.findViewById(R.id.etCategoryName);
        MaterialButton btnCancelCategory = view.findViewById(R.id.btnCancelCategory);
        MaterialButton btnSaveCategory = view.findViewById(R.id.btnSaveCategory);

        btnCancelCategory.setOnClickListener(v -> dialog.dismiss());

        btnSaveCategory.setOnClickListener(v -> {
            String name = etCategoryName.getText() != null ? etCategoryName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                etCategoryName.setError("Category name is required");
                return;
            }

            Category newCategory = new Category(name, "#2196F3");
            apiService.createCategory(newCategory).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Category> call, Response<Category> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        categoryList.add(response.body());
                    } else {
                        newCategory.setId(categoryList.size() + 1);
                        categoryList.add(newCategory);
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Category added!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<Category> call, Throwable t) {
                    newCategory.setId(categoryList.size() + 1);
                    categoryList.add(newCategory);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Category added locally!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }
}
