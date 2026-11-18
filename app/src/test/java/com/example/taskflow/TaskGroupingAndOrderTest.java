package com.example.taskflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Unit tests for Assignment 4 grouped task sections, DiffUtil snapshots, and manual reordering.
public class TaskGroupingAndOrderTest {

    @Test
    public void buildGroupedList_usesFixedGroupOrderAndOmitsEmptyGroups() {
        Task done = new Task("Finished task", Task.STATUS_DONE, Task.PRIORITY_LOW, DateUtils.daysFromToday(-2));
        Task todo = new Task("Upcoming task", Task.STATUS_TODO, Task.PRIORITY_HIGH, DateUtils.daysFromToday(2));
        Task overdue = new Task("Late task", Task.STATUS_TODO, Task.PRIORITY_NORMAL, DateUtils.daysFromToday(-3));
        Task inProgress = new Task("Active task", Task.STATUS_IN_PROGRESS, Task.PRIORITY_NORMAL, DateUtils.daysFromToday(1));

        ArrayList<TaskListItem> grouped = TaskListItem.buildGroupedList(
                Arrays.asList(done, todo, overdue, inProgress));

        // 4 headers + 4 task rows = 8 items
        assertEquals(8, grouped.size());

        // Fixed group order: Overdue, In Progress, To Do, Done
        assertTrue(grouped.get(0).isHeader());
        assertEquals(TaskListItem.GROUP_OVERDUE, grouped.get(0).getGroup());
        assertEquals(1, grouped.get(0).getHeaderCount());
        assertEquals(overdue.getId(), grouped.get(1).getTaskId());

        assertTrue(grouped.get(2).isHeader());
        assertEquals(TaskListItem.GROUP_IN_PROGRESS, grouped.get(2).getGroup());
        assertEquals(inProgress.getId(), grouped.get(3).getTaskId());

        assertTrue(grouped.get(4).isHeader());
        assertEquals(TaskListItem.GROUP_TODO, grouped.get(4).getGroup());
        assertEquals(todo.getId(), grouped.get(5).getTaskId());

        assertTrue(grouped.get(6).isHeader());
        assertEquals(TaskListItem.GROUP_DONE, grouped.get(6).getGroup());
        assertEquals(done.getId(), grouped.get(7).getTaskId());
    }

    @Test
    public void buildGroupedList_hidesHeadersForEmptyGroups() {
        Task todo = new Task("Only todo", Task.STATUS_TODO, Task.PRIORITY_NORMAL, Task.NO_DUE_DATE);
        ArrayList<TaskListItem> grouped = TaskListItem.buildGroupedList(Arrays.asList(todo));

        assertEquals(2, grouped.size());
        assertTrue(grouped.get(0).isHeader());
        assertEquals(TaskListItem.GROUP_TODO, grouped.get(0).getGroup());
        assertEquals(1, grouped.get(0).getHeaderCount());
        assertFalse(grouped.get(1).isHeader());
        assertEquals(todo.getId(), grouped.get(1).getTaskId());
    }

    @Test
    public void markingOverdueTaskDone_movesItToDoneGroupAndDetectsDiffContentChange() {
        Task overdue = new Task("Late task", Task.STATUS_TODO, Task.PRIORITY_HIGH, DateUtils.daysFromToday(-4));
        TaskListItem before = TaskListItem.taskRow(TaskListItem.getGroupForTask(overdue), overdue);

        overdue.setStatus(Task.STATUS_DONE);
        TaskListItem after = TaskListItem.taskRow(TaskListItem.getGroupForTask(overdue), overdue);

        assertEquals(TaskListItem.GROUP_OVERDUE, before.getGroup());
        assertEquals(TaskListItem.GROUP_DONE, after.getGroup());
        assertTrue(before.isSameItem(after));
        assertFalse(before.hasSameContent(after));
    }

    @Test
    public void syncTaskOrder_reordersVisibleTasksWithoutDisturbingHiddenTasks() {
        Project project = new Project("Test", "Tag", "Desc");
        Task a = new Task("A", Task.STATUS_DONE, Task.PRIORITY_LOW, Task.NO_DUE_DATE);
        Task b = new Task("B", Task.STATUS_TODO, Task.PRIORITY_HIGH, DateUtils.daysFromToday(1));
        Task c = new Task("C", Task.STATUS_DONE, Task.PRIORITY_NORMAL, Task.NO_DUE_DATE);
        Task d = new Task("D", Task.STATUS_TODO, Task.PRIORITY_LOW, DateUtils.daysFromToday(2));
        project.addTask(a);
        project.addTask(b);
        project.addTask(c);
        project.addTask(d);

        // Reorder visible To Do tasks [B, D] -> [D, B]
        project.syncTaskOrder(Arrays.asList(d, b));

        List<Task> tasks = project.getTasks();
        assertEquals(a.getId(), tasks.get(0).getId());
        assertEquals(d.getId(), tasks.get(1).getId());
        assertEquals(c.getId(), tasks.get(2).getId());
        assertEquals(b.getId(), tasks.get(3).getId());
    }

    @Test
    public void taskQuery_preservesManualOrderUntilReset() {
        ArrayList<Task> tasks = new ArrayList<>();
        Task later = new Task("Later", Task.STATUS_TODO, Task.PRIORITY_LOW, DateUtils.daysFromToday(5));
        Task earlier = new Task("Earlier", Task.STATUS_TODO, Task.PRIORITY_HIGH, DateUtils.daysFromToday(1));
        tasks.add(later);
        tasks.add(earlier);

        TaskQuery query = new TaskQuery();
        // Default sort: Due date asc -> earlier first
        assertEquals(earlier.getId(), query.apply(tasks).get(0).getId());

        // Manual order preserves list order -> later first
        query.manualOrder = true;
        assertEquals(later.getId(), query.apply(tasks).get(0).getId());

        // Reset restores automatic sort
        query.reset();
        assertFalse(query.manualOrder);
        assertEquals(earlier.getId(), query.apply(tasks).get(0).getId());
    }
}
