package com.example.taskflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

// Discussion tab: just "Coming Soon"
public class DiscussionFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    public DiscussionFragment() {
        // Required empty public constructor
    }

    public static DiscussionFragment newInstance(String projectId) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discussion, container, false);
    }
}
