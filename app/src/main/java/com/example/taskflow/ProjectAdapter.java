package com.example.taskflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

// Custom RecyclerView ListAdapter for HomeActivity using DiffUtil (uses row_project.xml).
public class ProjectAdapter extends ListAdapter<Project, ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    private final OnProjectClickListener listener;

    private static final DiffUtil.ItemCallback<Project> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Project>() {
                @Override
                public boolean areItemsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Project oldItem, @NonNull Project newItem) {
                    return Objects.equals(oldItem.getTag(), newItem.getTag())
                            && Objects.equals(oldItem.getName(), newItem.getName())
                            && Objects.equals(oldItem.getDescription(), newItem.getDescription())
                            && oldItem.getDoneCount() == newItem.getDoneCount()
                            && oldItem.getTotalTasks() == newItem.getTotalTasks()
                            && oldItem.getProgressPercent() == newItem.getProgressPercent()
                            && oldItem.getAttachmentCount() == newItem.getAttachmentCount()
                            && oldItem.getCommentCount() == newItem.getCommentCount();
                }
            };

    public ProjectAdapter(OnProjectClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTag;
        final TextView tvName;
        final TextView tvDescription;
        final TextView tvTasks;
        final TextView tvPercent;
        final ProgressBar pbProgress;
        final TextView tvAttachments;
        final TextView tvComments;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvTag);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTasks = itemView.findViewById(R.id.tvTasks);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            pbProgress = itemView.findViewById(R.id.pbProgress);
            tvAttachments = itemView.findViewById(R.id.tvAttachments);
            tvComments = itemView.findViewById(R.id.tvComments);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProjectClick(getItem(pos));
                }
            });
        }

        void bind(Project p) {
            Context context = itemView.getContext();
            int percent = p.getProgressPercent();
            int attachments = p.getAttachmentCount();
            int comments = p.getCommentCount();

            tvTag.setText(p.getTag());
            tvName.setText(p.getName());
            tvDescription.setText(p.getDescription());
            tvTasks.setText(p.getDoneCount() + "/" + p.getTotalTasks() + " tasks");
            tvPercent.setText(percent + "%");
            tvPercent.setTextColor(ContextCompat.getColor(context,
                    percent == 100 ? R.color.primary : R.color.text_body));
            pbProgress.setProgress(percent);
            tvAttachments.setText(attachments + (attachments == 1 ? " Attachment" : " Attachments"));
            tvComments.setText(comments + (comments == 1 ? " Comment" : " Comments"));
        }
    }
}
