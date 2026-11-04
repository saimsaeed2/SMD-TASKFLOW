# TaskFlow – Assignment 3

Java, same style as the class projects (`init()` methods, `findViewById`,
ViewPager2 + TabLayoutMediator + FragmentStateAdapter, fragment → activity callback interface).

## Screens / flow
MainActivity (splash) → **Get Started** → HomeActivity → tap a project → ProjectActivity
(tabs: Overview, Tasks, Files, Discussion). Back arrow / system Back return the updated
project to the same HomeActivity.

## Files (app/src/main/java/com/example/taskflow)
| File | What it does |
|---|---|
| `Project`, `Task`, `AttachmentLink` | Data records. Each has a UUID `id`. All `Serializable` so they go through intent extras. |
| `CatalogItem`, `AttachmentCatalog` | Local file catalog (PNG/JPG in `res/drawable-nodpi`, text in `res/raw`). Links store catalog id + resource id, never bytes. |
| `SampleData` | Starting projects, created **once** in HomeActivity. |
| `DateUtils` | Today = device local date. Overdue = due before today AND not Done. |
| `TaskQuery`, `FileQuery` | Search + filters + sort. Live in ProjectActivity so they survive tab switches. |
| `HomeActivity`, `ProjectAdapter` | Full project list, search (name or tag), New Project, result handling (replace by ID). |
| `ProjectActivity`, `ProjectPagerAdapter` | One working project, header, tabs with counts, returns result on Back. |
| `ProjectHost` | Callback interface the fragments use to talk to ProjectActivity. |
| `OverviewFragment` / `TasksFragment` / `FilesFragment` / `DiscussionFragment` | The four tabs. Each gets the project ID in its arguments and reads `host.getProject()`. |
| `TaskAdapter`, `AttachmentAdapter` | Custom ListView rows. |
| `ProjectDialog`, `TaskDialog`, `AttachmentDialogs` | AlertDialogs (validation, DatePicker, catalog picker, preview, remove confirm). |

## How data stays consistent
1. Home sends the project to ProjectActivity in a Bundle (Serializable).
2. ProjectActivity keeps that one object; every fragment reads it through `ProjectHost.getProject()`.
3. After any change a fragment refreshes itself and calls `host.onProjectChanged()` (header + tab counts).
   Hidden tabs re-read the project in `onResume()` when they become visible.
4. On Back, ProjectActivity puts the project in the result intent; Home replaces the project
   **with the same ID** and refreshes the adapter (no duplicate, no new HomeActivity).

All counts/percentages are calculated from the records (`done * 100 / total`, integer division).

## Notes
- Tapping an Overview breakdown card clears the task search and priority filter, then sets the
  status (or Overdue date filter) so the list matches the number on the card. Sort is kept.
- Unticking a Done task's checkbox sets it back to To do.
- Sample dates are relative to today, so "Due today" and "Overdue" always show correctly.
