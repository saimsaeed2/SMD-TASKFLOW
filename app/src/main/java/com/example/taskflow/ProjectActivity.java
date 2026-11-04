package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// Shows one project with 4 tabs: Overview, Tasks, Files, Discussion.
// Holds ONE working copy of the project; all fragments read and change this object.
public class ProjectActivity extends AppCompatActivity implements ProjectHost {

    public static final String EXTRA_PROJECT = "extra_project";

    public static final int TAB_OVERVIEW = 0;
    public static final int TAB_TASKS = 1;
    public static final int TAB_FILES = 2;
    public static final int TAB_DISCUSSION = 3;

    private static final String KEY_PROJECT = "project";
    private static final String KEY_TASK_QUERY = "task_query";
    private static final String KEY_FILE_QUERY = "file_query";

    Project project;
    TaskQuery taskQuery;
    FileQuery fileQuery;

    ImageButton btnBack;
    TextView tvProjectTag, tvProjectName;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ProjectPagerAdapter adapter;
    TabLayoutMediator mediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_project);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState != null) {
            project = (Project) savedInstanceState.getSerializable(KEY_PROJECT);
            taskQuery = (TaskQuery) savedInstanceState.getSerializable(KEY_TASK_QUERY);
            fileQuery = (FileQuery) savedInstanceState.getSerializable(KEY_FILE_QUERY);
        } else {
            // Project sent from HomeActivity through intent extras
            project = (Project) getIntent().getSerializableExtra(EXTRA_PROJECT);
            taskQuery = new TaskQuery();
            fileQuery = new FileQuery();
        }

        if (project == null) {   // should not happen, but never crash
            finish();
            return;
        }
        init();
    }

    private void init() {
        btnBack = findViewById(R.id.btnBack);
        tvProjectTag = findViewById(R.id.tvProjectTag);
        tvProjectName = findViewById(R.id.tvProjectName);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewpager2);

        showHeader();

        adapter = new ProjectPagerAdapter(this, project.getId());
        viewPager2.setAdapter(adapter);
        viewPager2.setOffscreenPageLimit(3);   // keep all 4 tabs alive during this visit

        mediator = new TabLayoutMediator(tabLayout, viewPager2,
                (tab, pos) -> tab.setText(getTabTitle(pos)));
        mediator.attach();

        // Toolbar arrow and system Back both return the updated project to Home
        btnBack.setOnClickListener(v -> finishWithResult());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithResult();
            }
        });
        // Avatars + Invite are UI only (no click listeners on purpose)
    }

    private void showHeader() {
        tvProjectTag.setText(project.getTag());
        tvProjectName.setText(project.getName());
    }

    private String getTabTitle(int position) {
        if (position == TAB_OVERVIEW)
            return "Overview";
        else if (position == TAB_TASKS)
            return "Tasks (" + project.getTotalTasks() + ")";
        else if (position == TAB_FILES)
            return "Files (" + project.getAttachmentCount() + ")";
        else
            return "Discussion (" + project.getCommentCount() + ")";
    }

    private void updateTabTitles() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(getTabTitle(i));
            }
        }
    }

    // Send the updated project back to the EXISTING HomeActivity
    private void finishWithResult() {
        Intent data = new Intent();
        data.putExtra(EXTRA_PROJECT, project);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_PROJECT, project);
        outState.putSerializable(KEY_TASK_QUERY, taskQuery);
        outState.putSerializable(KEY_FILE_QUERY, fileQuery);
    }

    // ---------------- ProjectHost ----------------

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public TaskQuery getTaskQuery() {
        return taskQuery;
    }

    @Override
    public FileQuery getFileQuery() {
        return fileQuery;
    }

    @Override
    public void onProjectChanged() {
        showHeader();
        updateTabTitles();
    }

    @Override
    public void openTab(int position) {
        // TabLayoutMediator keeps the indicator in sync with the ViewPager2 page
        viewPager2.setCurrentItem(position);
    }

    @Override
    public void showTasksWithFilter(int status, int dateFilter) {
        taskQuery.search = "";
        taskQuery.priority = TaskQuery.ALL;
        taskQuery.status = status;
        taskQuery.dateFilter = dateFilter;
        // TasksFragment reads the query again in onResume() when it becomes visible
        openTab(TAB_TASKS);
    }
}
