package com.example.taskflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

// Same pattern as ViewPagerFragmentAdapter from class.
// Each fragment gets the project ID in its arguments.
public class ProjectPagerAdapter extends FragmentStateAdapter {

    private final String projectId;

    public ProjectPagerAdapter(@NonNull FragmentActivity fragmentActivity, String projectId) {
        super(fragmentActivity);
        this.projectId = projectId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == ProjectActivity.TAB_OVERVIEW)
            return OverviewFragment.newInstance(projectId);
        else if (position == ProjectActivity.TAB_TASKS)
            return TasksFragment.newInstance(projectId);
        else if (position == ProjectActivity.TAB_FILES)
            return FilesFragment.newInstance(projectId);
        else
            return DiscussionFragment.newInstance(projectId);
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
