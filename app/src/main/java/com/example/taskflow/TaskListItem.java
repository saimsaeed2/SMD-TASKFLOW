package com.example.taskflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Represents either a status section header or a task row inside the single Tasks RecyclerView.
// Stores an immutable snapshot of rendered fields so DiffUtil reliably detects in-place mutations.
public class TaskListItem {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_TASK = 1;

    // Fixed display order for status groups
    public static final int GROUP_OVERDUE = 0;
    public static final int GROUP_IN_PROGRESS = 1;
    public static final int GROUP_TODO = 2;
    public static final int GROUP_DONE = 3;

    private final int viewType;
    private final int group;

    // Header fields
    private final String headerTitle;
    private final int headerCount;
    private final int headerColorRes;

    // Task fields + immutable snapshot for DiffUtil
    private final Task task;
    private final String taskId;
    private final String description;
    private final int status;
    private final int priority;
    private final long dueDate;
    private final boolean overdue;
    private final boolean dueToday;

    private TaskListItem(int group, String headerTitle, int headerCount, int headerColorRes) {
        this.viewType = VIEW_TYPE_HEADER;
        this.group = group;
        this.headerTitle = headerTitle;
        this.headerCount = headerCount;
        this.headerColorRes = headerColorRes;
        this.task = null;
        this.taskId = null;
        this.description = null;
        this.status = -1;
        this.priority = -1;
        this.dueDate = Task.NO_DUE_DATE;
        this.overdue = false;
        this.dueToday = false;
    }

    private TaskListItem(int group, Task task) {
        this.viewType = VIEW_TYPE_TASK;
        this.group = group;
        this.headerTitle = null;
        this.headerCount = 0;
        this.headerColorRes = 0;
        this.task = task;
        this.taskId = task.getId();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.priority = task.getPriority();
        this.dueDate = task.getDueDate();
        this.overdue = task.isOverdue();
        this.dueToday = task.isDueToday();
    }

    public static TaskListItem header(int group, int count) {
        return new TaskListItem(group, getGroupTitle(group), count, getGroupColorRes(group));
    }

    public static TaskListItem taskRow(int group, Task task) {
        return new TaskListItem(group, task);
    }

    public static int getGroupForTask(Task task) {
        if (task.isDone()) {
            return GROUP_DONE;
        }
        if (task.isOverdue()) {
            return GROUP_OVERDUE;
        }
        if (task.getStatus() == Task.STATUS_IN_PROGRESS) {
            return GROUP_IN_PROGRESS;
        }
        return GROUP_TODO;
    }

    public static String getGroupTitle(int group) {
        switch (group) {
            case GROUP_OVERDUE:
                return "Overdue";
            case GROUP_IN_PROGRESS:
                return "In Progress";
            case GROUP_TODO:
                return "To Do";
            case GROUP_DONE:
            default:
                return "Done";
        }
    }

    public static int getGroupColorRes(int group) {
        switch (group) {
            case GROUP_OVERDUE:
                return R.color.header_overdue;
            case GROUP_IN_PROGRESS:
                return R.color.header_in_progress;
            case GROUP_TODO:
                return R.color.header_todo;
            case GROUP_DONE:
            default:
                return R.color.header_done;
        }
    }

    // Groups tasks into Overdue, In Progress, To Do, Done (non-empty groups only).
    public static ArrayList<TaskListItem> buildGroupedList(List<Task> tasks) {
        ArrayList<Task> overdueList = new ArrayList<>();
        ArrayList<Task> inProgressList = new ArrayList<>();
        ArrayList<Task> todoList = new ArrayList<>();
        ArrayList<Task> doneList = new ArrayList<>();

        for (Task task : tasks) {
            int group = getGroupForTask(task);
            if (group == GROUP_OVERDUE) {
                overdueList.add(task);
            } else if (group == GROUP_IN_PROGRESS) {
                inProgressList.add(task);
            } else if (group == GROUP_TODO) {
                todoList.add(task);
            } else {
                doneList.add(task);
            }
        }

        ArrayList<TaskListItem> grouped = new ArrayList<>();
        appendGroup(grouped, GROUP_OVERDUE, overdueList);
        appendGroup(grouped, GROUP_IN_PROGRESS, inProgressList);
        appendGroup(grouped, GROUP_TODO, todoList);
        appendGroup(grouped, GROUP_DONE, doneList);
        return grouped;
    }

    private static void appendGroup(ArrayList<TaskListItem> out, int group, ArrayList<Task> tasks) {
        if (tasks.isEmpty()) return;
        out.add(header(group, tasks.size()));
        for (Task task : tasks) {
            out.add(taskRow(group, task));
        }
    }

    public int getViewType() { return viewType; }
    public boolean isHeader() { return viewType == VIEW_TYPE_HEADER; }
    public int getGroup() { return group; }

    public String getHeaderTitle() { return headerTitle; }
    public int getHeaderCount() { return headerCount; }
    public int getHeaderColorRes() { return headerColorRes; }

    public Task getTask() { return task; }
    public String getTaskId() { return taskId; }

    public boolean isSameItem(TaskListItem other) {
        if (this.viewType != other.viewType) return false;
        if (this.viewType == VIEW_TYPE_HEADER) {
            return this.group == other.group;
        }
        return Objects.equals(this.taskId, other.taskId);
    }

    public boolean hasSameContent(TaskListItem other) {
        if (this.viewType != other.viewType || this.group != other.group) return false;
        if (this.viewType == VIEW_TYPE_HEADER) {
            return this.headerCount == other.headerCount
                    && Objects.equals(this.headerTitle, other.headerTitle)
                    && this.headerColorRes == other.headerColorRes;
        }
        return Objects.equals(this.taskId, other.taskId)
                && Objects.equals(this.description, other.description)
                && this.status == other.status
                && this.priority == other.priority
                && this.dueDate == other.dueDate
                && this.overdue == other.overdue
                && this.dueToday == other.dueToday;
    }
}
