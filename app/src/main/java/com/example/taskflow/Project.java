package com.example.taskflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

// A project with its own tasks and attachment links.
// Serializable so the whole project can be sent between activities with intent extras.
public class Project implements Serializable {

    private String id;             // unique id - projects are always found/updated by this id
    private String name;
    private String tag;
    private String description;
    private int commentCount;      // dummy number (Discussion is "Coming Soon")
    private ArrayList<Task> tasks;
    private ArrayList<AttachmentLink> attachments;   // kept in link-insertion order

    public Project(String name, String tag, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.tag = tag;
        this.description = description;
        this.commentCount = 0;
        this.tasks = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    // ---------- basic getters / setters ----------
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public ArrayList<Task> getTasks() { return tasks; }

    public ArrayList<AttachmentLink> getAttachments() { return attachments; }

    // ---------- tasks ----------
    public void addTask(Task task) { tasks.add(task); }

    public Task findTaskById(String taskId) {
        for (Task t : tasks) {
            if (t.getId().equals(taskId)) return t;
        }
        return null;
    }

    public void removeTaskById(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                tasks.remove(i);
                return;
            }
        }
    }

    // ---------- attachments ----------
    public void addAttachment(CatalogItem item) {
        attachments.add(new AttachmentLink(item));
    }

    public boolean hasCatalogItem(String catalogId) {
        for (AttachmentLink link : attachments) {
            if (link.getCatalogId().equals(catalogId)) return true;
        }
        return false;
    }

    public void removeAttachmentById(String linkId) {
        for (int i = 0; i < attachments.size(); i++) {
            if (attachments.get(i).getId().equals(linkId)) {
                attachments.remove(i);
                return;
            }
        }
    }

    // ---------- counts (always calculated from the records) ----------
    public int getTotalTasks() { return tasks.size(); }

    public int getCountByStatus(int status) {
        int count = 0;
        for (Task t : tasks) {
            if (t.getStatus() == status) count++;
        }
        return count;
    }

    public int getDoneCount() { return getCountByStatus(Task.STATUS_DONE); }

    // Overdue is a subset of unfinished tasks (it is NOT a 4th status)
    public int getOverdueCount() {
        int count = 0;
        for (Task t : tasks) {
            if (t.isOverdue()) count++;
        }
        return count;
    }

    // done / total * 100, fractional part removed (int division). Empty project = 0%
    public int getProgressPercent() {
        if (tasks.isEmpty()) return 0;
        return getDoneCount() * 100 / getTotalTasks();
    }

    public int getAttachmentCount() { return attachments.size(); }

    public int getAttachmentCountByType(int type) {
        int count = 0;
        for (AttachmentLink link : attachments) {
            if (link.getType() == type) count++;
        }
        return count;
    }
}
