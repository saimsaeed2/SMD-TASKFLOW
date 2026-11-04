package com.example.taskflow;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

// Overview tab: description, progress, task breakdown, quick links, Add Task
public class OverviewFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    private String projectId;
    private ProjectHost host;

    TextView tvEditProject, tvDescription, tvPercent, tvCompleted;
    ProgressBar pbProgress;
    TextView tvTodoCount, tvInProgressCount, tvDoneCount, tvOverdueCount;
    View cardTodo, cardInProgress, cardDone, cardOverdue;
    View linkAttachments, linkDiscussion;
    TextView tvAttachmentsSummary, tvDiscussionSummary;
    Button btnAddTask;

    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance(String projectId) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;   // ProjectActivity implements ProjectHost
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();   // read the latest records every time this tab becomes visible
    }

    // Always read the activity's working project (never a separate copy)
    private Project getProject() {
        Project p = host.getProject();
        return (p != null && p.getId().equals(projectId)) ? p : null;
    }

    private void init(View view) {
        tvEditProject = view.findViewById(R.id.tvEditProject);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvPercent = view.findViewById(R.id.tvPercent);
        tvCompleted = view.findViewById(R.id.tvCompleted);
        pbProgress = view.findViewById(R.id.pbProgress);
        tvTodoCount = view.findViewById(R.id.tvTodoCount);
        tvInProgressCount = view.findViewById(R.id.tvInProgressCount);
        tvDoneCount = view.findViewById(R.id.tvDoneCount);
        tvOverdueCount = view.findViewById(R.id.tvOverdueCount);
        cardTodo = view.findViewById(R.id.cardTodo);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardDone = view.findViewById(R.id.cardDone);
        cardOverdue = view.findViewById(R.id.cardOverdue);
        linkAttachments = view.findViewById(R.id.linkAttachments);
        linkDiscussion = view.findViewById(R.id.linkDiscussion);
        tvAttachmentsSummary = view.findViewById(R.id.tvAttachmentsSummary);
        tvDiscussionSummary = view.findViewById(R.id.tvDiscussionSummary);
        btnAddTask = view.findViewById(R.id.btnAddTask);

        tvEditProject.setOnClickListener(v -> editProject());

        // Breakdown cards -> Tasks tab with a filter
        cardTodo.setOnClickListener(v -> host.showTasksWithFilter(Task.STATUS_TODO, TaskQuery.DATE_ALL));
        cardInProgress.setOnClickListener(v -> host.showTasksWithFilter(Task.STATUS_IN_PROGRESS, TaskQuery.DATE_ALL));
        cardDone.setOnClickListener(v -> host.showTasksWithFilter(Task.STATUS_DONE, TaskQuery.DATE_ALL));
        cardOverdue.setOnClickListener(v -> host.showTasksWithFilter(TaskQuery.ALL, TaskQuery.DATE_OVERDUE));

        // Quick links
        linkAttachments.setOnClickListener(v -> host.openTab(ProjectActivity.TAB_FILES));
        linkDiscussion.setOnClickListener(v -> host.openTab(ProjectActivity.TAB_DISCUSSION));

        // Same dialog as the Tasks tab; we stay on Overview after saving
        btnAddTask.setOnClickListener(v -> {
            Project p = getProject();
            if (p == null) return;
            TaskDialog.show(requireContext(), p, null, () -> {
                host.onProjectChanged();   // updates the "Tasks (n)" tab title
                refresh();
            });
        });
    }

    private void editProject() {
        Project p = getProject();
        if (p == null) return;
        ProjectDialog.show(requireContext(), p, (name, tag, description, attachment) -> {
            p.setName(name);
            p.setTag(tag);
            p.setDescription(description);
            host.onProjectChanged();   // updates the header
            refresh();
        });
    }

    // All numbers come from the task / attachment records
    private void refresh() {
        Project p = getProject();
        if (p == null || tvDescription == null) return;

        int total = p.getTotalTasks();
        int done = p.getDoneCount();
        int percent = p.getProgressPercent();

        tvDescription.setText(p.getDescription());
        tvPercent.setText(percent + "%");
        tvCompleted.setText(done + " of " + total + " tasks completed");
        pbProgress.setProgress(percent);

        tvTodoCount.setText(String.valueOf(p.getCountByStatus(Task.STATUS_TODO)));
        tvInProgressCount.setText(String.valueOf(p.getCountByStatus(Task.STATUS_IN_PROGRESS)));
        tvDoneCount.setText(String.valueOf(done));
        tvOverdueCount.setText(String.valueOf(p.getOverdueCount()));

        int files = p.getAttachmentCount();
        if (files == 0) {
            tvAttachmentsSummary.setText("No files yet");
        } else {
            ArrayList<String> names = new ArrayList<>();
            for (AttachmentLink link : p.getAttachments()) {
                names.add(link.getName());
            }
            tvAttachmentsSummary.setText(files + (files == 1 ? " file • " : " files • ") + TextUtils.join(", ", names));
        }
        tvDiscussionSummary.setText(p.getCommentCount() + " comments • Coming soon");
    }
}
