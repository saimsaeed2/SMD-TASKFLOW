package com.example.taskflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Holds the search / filters / sort of the Tasks tab.
// It lives in ProjectActivity, so it is kept while you switch tabs during the same visit.
public class TaskQuery implements Serializable {

    // "All" option for status and priority
    public static final int ALL = -1;

    // Date filter options (same order as the Date spinner)
    public static final int DATE_ALL = 0;
    public static final int DATE_TODAY = 1;
    public static final int DATE_OVERDUE = 2;
    public static final int DATE_NO_DATE = 3;

    // Sort options (same order as the Sort spinner)
    public static final int SORT_DUE_ASC = 0;
    public static final int SORT_DUE_DESC = 1;
    public static final int SORT_PRIORITY = 2;
    public static final int SORT_NEWEST = 3;
    public static final int SORT_OLDEST = 4;

    public static final String[] STATUS_OPTIONS = {"All statuses", "To do", "In progress", "Done"};
    public static final String[] PRIORITY_OPTIONS = {"All priorities", "High", "Normal", "Low"};
    public static final String[] DATE_OPTIONS = {"All dates", "Today", "Overdue", "No due date"};
    public static final String[] SORT_OPTIONS = {"Due date ↑", "Due date ↓", "Priority", "Newest first", "Oldest first"};

    public String search;
    public int status;
    public int priority;
    public int dateFilter;
    public int sort;
    public boolean manualOrder;

    public TaskQuery() {
        reset();
    }

    // Default query: empty search, All statuses, All priorities, All dates, Due date ascending
    public void reset() {
        search = "";
        status = ALL;
        priority = ALL;
        dateFilter = DATE_ALL;
        sort = SORT_DUE_ASC;
        manualOrder = false;
    }

    // A task is shown only if it passes the search AND every filter
    public boolean matches(Task t) {
        String s = search.trim().toLowerCase();
        if (!s.isEmpty() && !t.getDescription().toLowerCase().contains(s)) return false;

        if (status != ALL && t.getStatus() != status) return false;

        if (priority != ALL && t.getPriority() != priority) return false;

        if (dateFilter == DATE_TODAY && !t.isDueToday()) return false;
        if (dateFilter == DATE_OVERDUE && !t.isOverdue()) return false;
        if (dateFilter == DATE_NO_DATE && t.hasDueDate()) return false;

        return true;
    }

    // Returns a NEW list with the matching tasks, sorted (unless manual drag order is active)
    public ArrayList<Task> apply(ArrayList<Task> allTasks) {
        ArrayList<Task> result = new ArrayList<>();
        for (Task t : allTasks) {
            if (matches(t)) result.add(t);
        }
        if (!manualOrder) {
            Collections.sort(result, getComparator());
        }
        return result;
    }

    private Comparator<Task> getComparator() {
        if (sort == SORT_DUE_DESC) {
            // latest date first, tasks without a date go last
            return (a, b) -> {
                if (a.hasDueDate() != b.hasDueDate()) return a.hasDueDate() ? -1 : 1;
                return Long.compare(b.getDueDate(), a.getDueDate());
            };
        } else if (sort == SORT_PRIORITY) {
            // High -> Normal -> Low, then by due date
            return (a, b) -> {
                if (a.getPriority() != b.getPriority()) return Integer.compare(a.getPriority(), b.getPriority());
                return compareDueAsc(a, b);
            };
        } else if (sort == SORT_NEWEST) {
            return (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt());
        } else if (sort == SORT_OLDEST) {
            return (a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt());
        } else {
            return TaskQuery::compareDueAsc;
        }
    }

    // earliest date first, tasks without a date go last
    private static int compareDueAsc(Task a, Task b) {
        if (a.hasDueDate() != b.hasDueDate()) return a.hasDueDate() ? -1 : 1;
        return Long.compare(a.getDueDate(), b.getDueDate());
    }
}
