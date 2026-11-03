package com.example.taskflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// Custom adapter for the Home ListView (uses row_project.xml)
public class ProjectAdapter extends ArrayAdapter<Project> {

    public ProjectAdapter(@NonNull Context context, ArrayList<Project> projects) {
        super(context, 0, projects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.row_project, parent, false);
        }

        Project p = getItem(position);

        TextView tvTag = v.findViewById(R.id.tvTag);
        TextView tvName = v.findViewById(R.id.tvName);
        TextView tvDescription = v.findViewById(R.id.tvDescription);
        TextView tvTasks = v.findViewById(R.id.tvTasks);
        TextView tvPercent = v.findViewById(R.id.tvPercent);
        ProgressBar pbProgress = v.findViewById(R.id.pbProgress);
        TextView tvAttachments = v.findViewById(R.id.tvAttachments);
        TextView tvComments = v.findViewById(R.id.tvComments);

        int percent = p.getProgressPercent();
        int attachments = p.getAttachmentCount();
        int comments = p.getCommentCount();

        tvTag.setText(p.getTag());
        tvName.setText(p.getName());
        tvDescription.setText(p.getDescription());
        tvTasks.setText(p.getDoneCount() + "/" + p.getTotalTasks() + " tasks");
        tvPercent.setText(percent + "%");
        tvPercent.setTextColor(ContextCompat.getColor(getContext(),
                percent == 100 ? R.color.primary : R.color.text_body));
        pbProgress.setProgress(percent);
        tvAttachments.setText(attachments + (attachments == 1 ? " Attachment" : " Attachments"));
        tvComments.setText(comments + (comments == 1 ? " Comment" : " Comments"));

        return v;
    }
}
