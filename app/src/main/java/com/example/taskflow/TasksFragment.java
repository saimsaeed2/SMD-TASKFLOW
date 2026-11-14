package com.example.taskflow;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Tasks tab: search + Status / Priority / Date / Sort + single grouped RecyclerView of tasks
public class TasksFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    private String projectId;
    private ProjectHost host;

    EditText etSearchTasks;
    Spinner spStatus, spPriority, spDate, spSort;
    TextView tvShowingTasks, tvResetFilters, tvEmptyMessage;
    RecyclerView rvTasks;
    View layoutEmpty;
    Button btnEmptyAction, btnAddTask;

    TaskAdapter adapter;

    // true while WE change the controls in code (so the listeners don't react)
    private boolean updatingControls = false;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance(String projectId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
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
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        syncControlsFromQuery();
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tab became visible: the query may have been changed by Overview, and tasks may have changed
        syncControlsFromQuery();
        refreshList();
    }

    private Project getProject() {
        Project p = host.getProject();
        return (p != null && p.getId().equals(projectId)) ? p : null;
    }

    private void init(View view) {
        etSearchTasks = view.findViewById(R.id.etSearchTasks);
        spStatus = view.findViewById(R.id.spStatus);
        spPriority = view.findViewById(R.id.spPriority);
        spDate = view.findViewById(R.id.spDate);
        spSort = view.findViewById(R.id.spSort);
        tvShowingTasks = view.findViewById(R.id.tvShowingTasks);
        tvResetFilters = view.findViewById(R.id.tvResetFilters);
        rvTasks = view.findViewById(R.id.rvTasks);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        btnEmptyAction = view.findViewById(R.id.btnEmptyAction);
        btnAddTask = view.findViewById(R.id.btnAddTask);

        setupSpinner(spStatus, TaskQuery.STATUS_OPTIONS);
        setupSpinner(spPriority, TaskQuery.PRIORITY_OPTIONS);
        setupSpinner(spDate, TaskQuery.DATE_OPTIONS);
        setupSpinner(spSort, TaskQuery.SORT_OPTIONS);

        adapter = new TaskAdapter(new TaskAdapter.OnTaskInteractionListener() {
            @Override
            public void onTaskClick(Task task) {
                openTaskDialog(task);
            }

            @Override
            public void onTaskChecked(Task task, boolean checked) {
                TasksFragment.this.onTaskChecked(task, checked);
            }
        });
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);
        attachItemTouchHelper();

        etSearchTasks.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (updatingControls) return;
                host.getTaskQuery().search = s.toString();
                refreshList();
            }
        });

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (updatingControls) return;
                TaskQuery q = host.getTaskQuery();
                if (parent == spStatus) q.status = position - 1;          // 0 = All -> -1
                else if (parent == spPriority) q.priority = position - 1;  // 0 = All -> -1
                else if (parent == spDate) q.dateFilter = position;
                else if (parent == spSort) q.sort = position;
                refreshList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spStatus.setOnItemSelectedListener(spinnerListener);
        spPriority.setOnItemSelectedListener(spinnerListener);
        spDate.setOnItemSelectedListener(spinnerListener);
        spSort.setOnItemSelectedListener(spinnerListener);

        tvResetFilters.setOnClickListener(v -> resetFilters());
        btnAddTask.setOnClickListener(v -> openTaskDialog(null));

        // Empty-state button: "Add Task" when there are no tasks, "Reset Filters" otherwise
        btnEmptyAction.setOnClickListener(v -> {
            Project p = getProject();
            if (p != null && p.getTotalTasks() == 0) openTaskDialog(null);
            else resetFilters();
        });
    }

    private void setupSpinner(Spinner spinner, String[] options) {
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(a);
    }

    // Put the saved query values back into the search box and spinners
    private void syncControlsFromQuery() {
        TaskQuery q = host.getTaskQuery();
        updatingControls = true;
        if (!etSearchTasks.getText().toString().equals(q.search)) {
            etSearchTasks.setText(q.search);
        }
        spStatus.setSelection(q.status + 1);
        spPriority.setSelection(q.priority + 1);
        spDate.setSelection(q.dateFilter);
        spSort.setSelection(q.sort);
        updatingControls = false;
    }

    private void resetFilters() {
        host.getTaskQuery().reset();
        syncControlsFromQuery();
        refreshList();
    }

    private void openTaskDialog(Task task) {
        Project p = getProject();
        if (p == null) return;
        TaskDialog.show(requireContext(), p, task, () -> {
            host.onProjectChanged();
            refreshList();
        });
    }

    // Checkbox ticked -> Done. Unticked -> back to To do.
    private void onTaskChecked(Task task, boolean checked) {
        task.setStatus(checked ? Task.STATUS_DONE : Task.STATUS_TODO);
        host.onProjectChanged();
        refreshList();
    }

    private void attachItemTouchHelper() {
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                // Header rows must not be swiped or dragged
                if (viewHolder.getItemViewType() == TaskAdapter.VIEW_TYPE_HEADER) {
                    return makeMovementFlags(0, 0);
                }
                return makeMovementFlags(0, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                TaskListItem item = adapter.getItemAt(pos);
                if (item == null || item.isHeader()) {
                    if (pos != RecyclerView.NO_POSITION) {
                        adapter.notifyItemChanged(pos);
                    }
                    return;
                }

                if (direction == ItemTouchHelper.LEFT) {
                    handleSwipeLeftMarkDone(item.getTask(), pos);
                }
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(rvTasks);
    }

    // Swiping left marks an unfinished task Done; already-Done tasks snap back with a Toast.
    private void handleSwipeLeftMarkDone(Task task, int position) {
        if (task.isDone()) {
            adapter.notifyItemChanged(position);
            Toast.makeText(requireContext(), "Task already completed", Toast.LENGTH_SHORT).show();
            return;
        }
        task.setStatus(Task.STATUS_DONE);
        host.onProjectChanged();
        refreshList();
    }

    private void refreshList() {
        Project p = getProject();
        if (p == null || adapter == null) return;

        ArrayList<Task> result = host.getTaskQuery().apply(p.getTasks());
        ArrayList<TaskListItem> grouped = TaskListItem.buildGroupedList(result);
        adapter.submitList(grouped);

        tvShowingTasks.setText("Showing " + result.size() + " of " + p.getTotalTasks() + " tasks");

        // Two different empty states
        if (p.getTotalTasks() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("No tasks yet");
            btnEmptyAction.setText("Add Task");
        } else if (result.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("No tasks match these filters");
            btnEmptyAction.setText("Reset Filters");
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
