package com.example.taskflow;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Single multi-view-type RecyclerView adapter for the grouped Tasks list.
// Displays both section header rows (VIEW_TYPE_HEADER) and task rows (VIEW_TYPE_TASK).
public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = TaskListItem.VIEW_TYPE_HEADER;
    public static final int VIEW_TYPE_TASK = TaskListItem.VIEW_TYPE_TASK;

    public interface OnTaskInteractionListener {
        void onTaskClick(Task task);

        void onTaskChecked(Task task, boolean checked);
    }

    private final ArrayList<TaskListItem> items = new ArrayList<>();
    private final OnTaskInteractionListener listener;

    public TaskAdapter(OnTaskInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<TaskListItem> newItems) {
        final ArrayList<TaskListItem> oldCopy = new ArrayList<>(items);
        final ArrayList<TaskListItem> newCopy = newItems == null
                ? new ArrayList<>()
                : new ArrayList<>(newItems);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldCopy.size();
            }

            @Override
            public int getNewListSize() {
                return newCopy.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldCopy.get(oldItemPosition).isSameItem(newCopy.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldCopy.get(oldItemPosition).hasSameContent(newCopy.get(newItemPosition));
            }
        });

        items.clear();
        items.addAll(newCopy);
        diffResult.dispatchUpdatesTo(this);
    }

    public TaskListItem getItemAt(int position) {
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    // Returns only the task objects in their current on-screen order (skipping section headers).
    public ArrayList<Task> getTasksInDisplayOrder() {
        ArrayList<Task> ordered = new ArrayList<>();
        for (TaskListItem item : items) {
            if (!item.isHeader() && item.getTask() != null) {
                ordered.add(item.getTask());
            }
        }
        return ordered;
    }

    // Reorders a task row within the same status group only.
    public boolean moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0
                || fromPosition >= items.size() || toPosition >= items.size()
                || fromPosition == toPosition) {
            return false;
        }
        TaskListItem fromItem = items.get(fromPosition);
        TaskListItem toItem = items.get(toPosition);
        if (fromItem.isHeader() || toItem.isHeader() || fromItem.getGroup() != toItem.getGroup()) {
            return false;
        }

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_HEADER) {
            View v = inflater.inflate(R.layout.row_task_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.row_task, parent, false);
            return new TaskViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskListItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind(item.getTask());
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView tvGroupTitle;
        final TextView tvGroupCount;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupTitle = itemView.findViewById(R.id.tvGroupTitle);
            tvGroupCount = itemView.findViewById(R.id.tvGroupCount);
        }

        void bind(TaskListItem item) {
            int color = ContextCompat.getColor(itemView.getContext(), item.getHeaderColorRes());
            tvGroupTitle.setText(item.getHeaderTitle());
            tvGroupTitle.setTextColor(color);
            tvGroupCount.setText(String.valueOf(item.getHeaderCount()));
            tvGroupCount.setTextColor(color);
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        final CheckBox cbDone;
        final TextView tvDescription;
        final TextView tvMeta;
        final TextView tvDue;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbDone = itemView.findViewById(R.id.cbDone);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvMeta = itemView.findViewById(R.id.tvTaskMeta);
            tvDue = itemView.findViewById(R.id.tvTaskDue);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                TaskListItem item = getItemAt(pos);
                if (item != null && !item.isHeader() && listener != null) {
                    listener.onTaskClick(item.getTask());
                }
            });
        }

        void bind(Task t) {
            Context context = itemView.getContext();

            // Clear old listener before updating checked state on recycled rows
            cbDone.setOnCheckedChangeListener(null);
            cbDone.setChecked(t.isDone());
            cbDone.setOnCheckedChangeListener((button, isChecked) -> {
                if (listener != null) {
                    listener.onTaskChecked(t, isChecked);
                }
            });

            tvDescription.setText(t.getDescription());
            if (t.isDone()) {
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescription.setTextColor(ContextCompat.getColor(context, R.color.done_text));
            } else {
                tvDescription.setPaintFlags(tvDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
            }

            tvMeta.setText(t.getStatusLabel() + " • " + t.getPriorityLabel() + " • ");

            if (!t.hasDueDate()) {
                tvDue.setText("No due date");
                tvDue.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            } else if (t.isOverdue()) {
                tvDue.setText(DateUtils.format(t.getDueDate()) + " (Overdue)");
                tvDue.setTextColor(ContextCompat.getColor(context, R.color.overdue));
            } else if (t.isDueToday() && !t.isDone()) {
                tvDue.setText("Due today");
                tvDue.setTextColor(ContextCompat.getColor(context, R.color.due_today));
            } else {
                tvDue.setText(DateUtils.format(t.getDueDate()));
                tvDue.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            }
        }
    }
}
