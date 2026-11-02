package com.example.taskflow;

import java.io.Serializable;
import java.util.UUID;

// One task inside a project.
// Serializable so it can travel inside the Project through intent extras.
public class Task implements Serializable {

    // Status values
    public static final int STATUS_TODO = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_DONE = 2;

    // Priority values
    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_NORMAL = 1;
    public static final int PRIORITY_LOW = 2;

    // Used when the task has no due date
    public static final long NO_DUE_DATE = -1;

    public static final String[] STATUS_LABELS = {"To do", "In progress", "Done"};
    public static final String[] PRIORITY_LABELS = {"High", "Normal", "Low"};

    private String id;          // unique id (never use the description or row position to find a task)
    private String description;
    private int status;
    private int priority;
    private long dueDate;       // start of the due day in millis, or NO_DUE_DATE
    private long createdAt;     // creation time in millis

    public Task(String description, int status, int priority, long dueDate) {
        this.id = UUID.randomUUID().toString();
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean hasDueDate() { return dueDate != NO_DUE_DATE; }

    public boolean isDone() { return status == STATUS_DONE; }

    // Overdue = has a due date BEFORE today AND is not Done.
    // A task due today, or with no date, is NOT overdue.
    public boolean isOverdue() {
        return hasDueDate() && dueDate < DateUtils.todayStart() && !isDone();
    }

    public boolean isDueToday() {
        return hasDueDate() && DateUtils.isToday(dueDate);
    }

    public String getStatusLabel() { return STATUS_LABELS[status]; }

    public String getPriorityLabel() { return PRIORITY_LABELS[priority]; }
}
