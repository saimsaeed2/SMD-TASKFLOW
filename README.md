# TaskFlow – Assignment 4

Java Android app continuing from Assignment 3. Every `ListView` has been replaced with a custom `RecyclerView` using `DiffUtil` (no `notifyDataSetChanged`), grouped multi-view-type tasks, and `ItemTouchHelper` swipe and drag gestures.

## Screens / flow
MainActivity (splash) → **Get Started** → HomeActivity (`RecyclerView` + `ListAdapter`/`DiffUtil`) → tap a project → ProjectActivity
(tabs: Overview, Tasks, Files, Discussion). Back arrow / system Back return the updated
project to the same HomeActivity and update by project ID.

## Assignment 4 Features
- **HomeActivity**: Replaces `ListView` with `RecyclerView` + `ProjectAdapter` extending `ListAdapter` with `DiffUtil.ItemCallback`. Clicking a project opens `ProjectActivity` via intent extras and updates the existing project by ID on return without calling `notifyDataSetChanged`.
- **TasksFragment**: Uses a single `RecyclerView` with multi-view-type `TaskAdapter` (`VIEW_TYPE_HEADER` and `VIEW_TYPE_TASK`) powered by `TaskListItem` and `DiffUtil`.
  - Fixed non-empty group order: **Overdue**, **In Progress**, **To Do**, **Done**.
  - **Swipe Left**: Marks an unfinished task as Done (moving it to Done and updating header counts immediately); swiping an already-Done task snaps it back and shows `"Task already completed"`.
  - **Swipe Right**: Opens a delete confirmation `AlertDialog`; confirming removes the task and refreshes counts, while cancelling restores the swiped row.
  - **Long-Press Drag-to-Reorder**: Reorders tasks strictly within the same status group (cannot drag headers or move across groups) and persists manual order via `Project.syncTaskOrder` and `TaskQuery.manualOrder`.
- **FilesFragment**: Replaces `ListView` with `RecyclerView` + `AttachmentAdapter` extending `ListAdapter` with `DiffUtil`.
  - Keeps thumbnail/icon, display name, type metadata, real content Preview dialog (`ImageView` / `TextView`), and remove action.
  - **Swipe Right**: Prompts the user to type the exact attachment name to confirm removal; cancelling or entering a wrong name keeps the attachment linked and restores the row.

## Key Files (`app/src/main/java/com/example/taskflow`)
| File | What it does |
|---|---|
| `Project`, `Task`, `AttachmentLink` | Data records with UUID `id` (`Serializable`), plus `Project.syncTaskOrder` for manual drag order. |
| `TaskListItem` | Multi-view-type item model (section headers + task snapshots) and `buildGroupedList` grouping helper. |
| `HomeActivity`, `ProjectAdapter` | Project `RecyclerView` with `ListAdapter` + `DiffUtil`, search, New Project, replace-by-ID result handling. |
| `TasksFragment`, `TaskAdapter` | Single grouped `RecyclerView`, swipe-left Done, swipe-right Delete confirmation, same-group drag-to-reorder. |
| `FilesFragment`, `AttachmentAdapter` | Attachment `RecyclerView` with `ListAdapter` + `DiffUtil`, preview dialog, and swipe-right name-confirm delete. |
| `SwipeBackgroundDrawer` | Custom `ItemTouchHelper` canvas helper for rounded swipe backgrounds, icons, and labels. |
| `AttachmentDialogs`, `TaskDialog`, `ProjectDialog` | AlertDialogs with validation, preview, and name-match removal confirmation. |
| `TaskQuery`, `FileQuery` | Search, filters, sort, and manual-order state preserved in `ProjectActivity`. |
