package com.example.taskflow;

import java.util.ArrayList;

// Creates the starting projects shown on the Home screen.
// This is called ONCE in HomeActivity - fragments never create their own copies.
public class SampleData {

    private static final int TODO = Task.STATUS_TODO;
    private static final int PROGRESS = Task.STATUS_IN_PROGRESS;
    private static final int DONE = Task.STATUS_DONE;
    private static final int HIGH = Task.PRIORITY_HIGH;
    private static final int NORMAL = Task.PRIORITY_NORMAL;
    private static final int LOW = Task.PRIORITY_LOW;
    private static final long NONE = Task.NO_DUE_DATE;

    public static ArrayList<Project> createProjects() {
        ArrayList<Project> list = new ArrayList<>();

        // 1) Main sample project: 12 tasks (5 To do, 3 In progress, 4 Done, 2 Overdue), 3 files, 3 comments
        Project p1 = new Project("Service Page Website", "User Interface",
                "Make a page display about services for the company website in blue and red and provide some price information on each product card.");
        addTask(p1, "Build header layout", TODO, HIGH, DateUtils.daysFromToday(0));
        addTask(p1, "Add form validation", TODO, HIGH, DateUtils.daysFromToday(0));
        addTask(p1, "Create brand guideline for website design", DONE, NORMAL, DateUtils.daysFromToday(-6));
        addTask(p1, "Create low fidelity service page website", TODO, NORMAL, DateUtils.daysFromToday(-3));
        addTask(p1, "Configure pricing table responsive grid", PROGRESS, NORMAL, DateUtils.daysFromToday(2));
        addTask(p1, "Draft hero copy and CTA text", DONE, LOW, DateUtils.daysFromToday(-8));
        addTask(p1, "Set up service card component with price, title and short description for each service", PROGRESS, HIGH, DateUtils.daysFromToday(-1));
        addTask(p1, "Collect client logos", TODO, LOW, NONE);
        addTask(p1, "Write SEO meta descriptions", TODO, LOW, DateUtils.daysFromToday(6));
        addTask(p1, "Review color palette with client", DONE, NORMAL, DateUtils.daysFromToday(-10));
        addTask(p1, "Integrate contact form API", PROGRESS, HIGH, DateUtils.daysFromToday(4));
        addTask(p1, "Export icon set", DONE, LOW, DateUtils.daysFromToday(-2));
        p1.addAttachment(AttachmentCatalog.findById("cat_1"));
        p1.addAttachment(AttachmentCatalog.findById("cat_2"));
        p1.addAttachment(AttachmentCatalog.findById("cat_3"));
        p1.setCommentCount(3);
        list.add(p1);

        // 2) 3/10 tasks
        Project p2 = new Project("Login API", "Backend",
                "Build authentication endpoints and JWT token refresh handlers");
        addTask(p2, "Design users table", DONE, HIGH, DateUtils.daysFromToday(-9));
        addTask(p2, "Hash passwords with bcrypt", DONE, HIGH, DateUtils.daysFromToday(-7));
        addTask(p2, "POST /login endpoint", DONE, HIGH, DateUtils.daysFromToday(-4));
        addTask(p2, "POST /refresh endpoint", PROGRESS, HIGH, DateUtils.daysFromToday(1));
        addTask(p2, "POST /logout endpoint", TODO, NORMAL, DateUtils.daysFromToday(3));
        addTask(p2, "Rate limit login attempts", TODO, NORMAL, DateUtils.daysFromToday(5));
        addTask(p2, "Write unit tests for auth service", TODO, NORMAL, DateUtils.daysFromToday(-1));
        addTask(p2, "Document the endpoints", TODO, LOW, NONE);
        addTask(p2, "Add forgot password email", TODO, LOW, DateUtils.daysFromToday(10));
        addTask(p2, "Code review with team", PROGRESS, NORMAL, DateUtils.daysFromToday(0));
        p2.addAttachment(AttachmentCatalog.findById("cat_6"));
        p2.addAttachment(AttachmentCatalog.findById("cat_5"));
        p2.setCommentCount(2);
        list.add(p2);

        // 3) 2/8 tasks
        Project p3 = new Project("Mobile Dashboard", "Frontend",
                "Responsive overview widgets and telemetry cards");
        addTask(p3, "Sketch dashboard layout", DONE, NORMAL, DateUtils.daysFromToday(-5));
        addTask(p3, "Pick chart library", DONE, LOW, DateUtils.daysFromToday(-4));
        addTask(p3, "Build stats card widget", PROGRESS, HIGH, DateUtils.daysFromToday(2));
        addTask(p3, "Build telemetry list", TODO, NORMAL, DateUtils.daysFromToday(4));
        addTask(p3, "Dark mode colors", TODO, LOW, NONE);
        addTask(p3, "Tablet layout", TODO, NORMAL, DateUtils.daysFromToday(8));
        addTask(p3, "Loading skeletons", TODO, LOW, NONE);
        addTask(p3, "Connect to live data", TODO, HIGH, DateUtils.daysFromToday(12));
        p3.addAttachment(AttachmentCatalog.findById("cat_4"));
        p3.setCommentCount(1);
        list.add(p3);

        // 4) 4/4 tasks (100%)
        Project p4 = new Project("Release Checklist", "QA & Ops",
                "Pre-production verification and smoke tests");
        addTask(p4, "Run smoke tests", DONE, HIGH, DateUtils.daysFromToday(-3));
        addTask(p4, "Verify environment variables", DONE, HIGH, DateUtils.daysFromToday(-3));
        addTask(p4, "Backup production database", DONE, NORMAL, DateUtils.daysFromToday(-2));
        addTask(p4, "Tag release in git", DONE, LOW, DateUtils.daysFromToday(-1));
        p4.addAttachment(AttachmentCatalog.findById("cat_5"));
        p4.setCommentCount(2);
        list.add(p4);

        // 5) Empty project (0/0, 0%)
        Project p5 = new Project("Design Assets", "Design",
                "Vector icons and illustration library for landing pages");
        list.add(p5);

        return list;
    }

    // Adds a task and gives each one a slightly different creation time
    private static void addTask(Project p, String description, int status, int priority, long dueDate) {
        Task t = new Task(description, status, priority, dueDate);
        t.setCreatedAt(System.currentTimeMillis() - (100 - p.getTotalTasks()) * 60_000L);
        p.addTask(t);
    }
}
