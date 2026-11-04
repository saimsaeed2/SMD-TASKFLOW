package com.example.taskflow;

// Callback interface implemented by ProjectActivity.
// Fragments talk to the activity through this (same idea as FragmentA.SendPosition in class).
public interface ProjectHost {

    // The ONE working project object. Fragments always read from here.
    Project getProject();

    // Search / filters / sort of the Tasks and Files tabs (kept for the whole visit)
    TaskQuery getTaskQuery();

    FileQuery getFileQuery();

    // Call after any change so the header and tab counts are refreshed
    void onProjectChanged();

    // Switch to a tab (ProjectActivity.TAB_...)
    void openTab(int position);

    // Used by the Overview breakdown cards
    void showTasksWithFilter(int status, int dateFilter);
}
