package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

// Home screen. Keeps the FULL project collection.
public class HomeActivity extends AppCompatActivity {

    private static final String KEY_PROJECTS = "projects";

    ArrayList<Project> projects;        // all projects (the real collection)
    ArrayList<Project> shownProjects;   // projects that match the search (what the ListView shows)
    ProjectAdapter adapter;

    ListView lvProjects;
    EditText etSearchProjects;
    TextView tvShowingProjects, tvNoProjects;
    Button btnNewProject;

    // Opens ProjectActivity and receives the updated project when the user comes back
    ActivityResultLauncher<Intent> projectLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // + 20dp keeps the side margins of the screen
            v.setPadding(systemBars.left + dp(20), systemBars.top,
                    systemBars.right + dp(20), systemBars.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            // e.g. after rotation - keep the user's changes
            projects = (ArrayList<Project>) savedInstanceState.getSerializable(KEY_PROJECTS);
        } else {
            projects = SampleData.createProjects();
        }
        init();
    }

    private void init() {
        lvProjects = findViewById(R.id.lvProjects);
        etSearchProjects = findViewById(R.id.etSearchProjects);
        tvShowingProjects = findViewById(R.id.tvShowingProjects);
        tvNoProjects = findViewById(R.id.tvNoProjects);
        btnNewProject = findViewById(R.id.btnNewProject);

        shownProjects = new ArrayList<>();
        adapter = new ProjectAdapter(this, shownProjects);
        lvProjects.setAdapter(adapter);

        // Row click -> open that project (we take its ID, not its name or row number)
        lvProjects.setOnItemClickListener((parent, view, position, id) -> {
            Project clicked = adapter.getItem(position);
            if (clicked != null) {
                openProject(clicked.getId());
            }
        });

        projectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Project updated = (Project) result.getData()
                                .getSerializableExtra(ProjectActivity.EXTRA_PROJECT);
                        if (updated != null) {
                            replaceProjectById(updated);
                            applySearch();
                        }
                    }
                });

        btnNewProject.setOnClickListener(v -> showNewProjectDialog());

        etSearchProjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                applySearch();
            }
        });

        applySearch();
    }

    private void openProject(String projectId) {
        Project project = findProjectById(projectId);
        if (project == null) return;

        Intent i = new Intent(HomeActivity.this, ProjectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ProjectActivity.EXTRA_PROJECT, project);
        i.putExtras(bundle);
        projectLauncher.launch(i);
    }

    private void showNewProjectDialog() {
        ProjectDialog.show(this, null, (name, tag, description, attachment) -> {
            Project p = new Project(name, tag, description);   // 0 tasks, 0 comments, 0/0, 0%
            if (attachment != null) {
                p.addAttachment(attachment);                    // 0 or 1 attachment
            }
            projects.add(p);
            applySearch();
        });
    }

    // Case-insensitive search on any part of the name or the tag
    private void applySearch() {
        String query = etSearchProjects.getText().toString().trim().toLowerCase();

        shownProjects.clear();
        for (Project p : projects) {
            if (query.isEmpty()
                    || p.getName().toLowerCase().contains(query)
                    || p.getTag().toLowerCase().contains(query)) {
                shownProjects.add(p);
            }
        }
        adapter.notifyDataSetChanged();

        if (query.isEmpty()) {
            tvShowingProjects.setText("Showing " + shownProjects.size() + " projects");
        } else {
            tvShowingProjects.setText("Showing " + shownProjects.size() + " of " + projects.size() + " projects");
        }
        tvNoProjects.setVisibility(shownProjects.isEmpty() ? View.VISIBLE : View.GONE);
        tvNoProjects.setText(projects.isEmpty() ? "No projects yet" : "No projects match your search");
    }

    private Project findProjectById(String id) {
        for (Project p : projects) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    // Update the existing project with the same ID (never append a duplicate)
    private void replaceProjectById(Project updated) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId().equals(updated.getId())) {
                projects.set(i, updated);
                return;
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_PROJECTS, projects);
    }
}
