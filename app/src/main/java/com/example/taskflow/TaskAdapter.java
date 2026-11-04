package com.example.taskflow;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// Custom adapter for the Tasks ListView (uses row_task.xml)
public class TaskAdapter extends ArrayAdapter<Task> {

    public interface OnTaskCheckListener {
        void onTaskChecked(Task task, boolean checked);
    }

    private final OnTaskCheckListener listener;

    public TaskAdapter(@NonNull Context context, ArrayList<Task> tasks, OnTaskCheckListener listener) {
        super(context, 0, tasks);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.row_task, parent, false);
        }

        Task t = getItem(position);

        CheckBox cbDone = v.findViewById(R.id.cbDone);
        TextView tvDescription = v.findViewById(R.id.tvTaskDescription);
        TextView tvMeta = v.findViewById(R.id.tvTaskMeta);
        TextView tvDue = v.findViewById(R.id.tvTaskDue);

        // remove the old listener first, because rows are reused
        cbDone.setOnCheckedChangeListener(null);
        cbDone.setChecked(t.isDone());
        cbDone.setOnCheckedChangeListener((button, isChecked) -> listener.onTaskChecked(t, isChecked));

        tvDescription.setText(t.getDescription());
        if (t.isDone()) {
            // strike-through but still readable grey
            tvDescription.setPaintFlags(tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDescription.setTextColor(color(R.color.done_text));
        } else {
            tvDescription.setPaintFlags(tvDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tvDescription.setTextColor(color(R.color.text_dark));
        }

        tvMeta.setText(t.getStatusLabel() + " • " + t.getPriorityLabel() + " • ");

        if (!t.hasDueDate()) {
            tvDue.setText("No due date");
            tvDue.setTextColor(color(R.color.text_gray));
        } else if (t.isOverdue()) {
            tvDue.setText(DateUtils.format(t.getDueDate()) + " • OVERDUE");
            tvDue.setTextColor(color(R.color.overdue));
        } else if (t.isDueToday() && !t.isDone()) {
            tvDue.setText("Due today");
            tvDue.setTextColor(color(R.color.due_today));
        } else {
            tvDue.setText(DateUtils.format(t.getDueDate()));
            tvDue.setTextColor(color(R.color.text_gray));
        }

        return v;
    }

    private int color(int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }
}
