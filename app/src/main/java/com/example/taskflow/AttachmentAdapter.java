package com.example.taskflow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

// Custom RecyclerView ListAdapter for the Files tab (uses row_attachment.xml).
public class AttachmentAdapter extends ListAdapter<AttachmentLink, AttachmentAdapter.AttachmentViewHolder> {

    public interface OnAttachmentActionListener {
        void onPreview(AttachmentLink link);

        void onRemove(AttachmentLink link);
    }

    private final OnAttachmentActionListener listener;

    private static final DiffUtil.ItemCallback<AttachmentLink> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AttachmentLink>() {
                @Override
                public boolean areItemsTheSame(@NonNull AttachmentLink oldItem,
                                               @NonNull AttachmentLink newItem) {
                    return Objects.equals(oldItem.getId(), newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull AttachmentLink oldItem,
                                                  @NonNull AttachmentLink newItem) {
                    return Objects.equals(oldItem.getCatalogId(), newItem.getCatalogId())
                            && oldItem.getResId() == newItem.getResId()
                            && Objects.equals(oldItem.getName(), newItem.getName())
                            && oldItem.getType() == newItem.getType()
                            && Objects.equals(oldItem.getSizeLabel(), newItem.getSizeLabel())
                            && oldItem.getLinkedAt() == newItem.getLinkedAt();
                }
            };

    public AttachmentAdapter(OnAttachmentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public AttachmentLink getAttachmentAt(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return getItem(position);
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class AttachmentViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivThumb;
        final TextView tvFileName;
        final TextView tvFileMeta;
        final Button btnPreview;
        final ImageButton btnRemove;

        AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileMeta = itemView.findViewById(R.id.tvFileMeta);
            btnPreview = itemView.findViewById(R.id.btnPreview);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        void bind(AttachmentLink link) {
            Context context = itemView.getContext();
            int pad;
            if (link.isImage()) {
                // real thumbnail of the image
                ivThumb.setImageResource(link.getResId());
                ivThumb.setImageTintList(null);
                ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivThumb.setBackgroundResource(R.drawable.bg_icon_box_image);
                pad = dp(context, 2);
            } else {
                // document icon for text files
                ivThumb.setImageResource(R.drawable.ic_doc);
                ivThumb.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.due_today)));
                ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivThumb.setBackgroundResource(R.drawable.bg_icon_box_text);
                pad = dp(context, 10);
            }
            ivThumb.setPadding(pad, pad, pad, pad);

            tvFileName.setText(link.getName());
            tvFileMeta.setText(link.getTypeLabel() + " • " + link.getSizeLabel()
                    + " • Linked " + DateUtils.format(link.getLinkedAt()));

            btnPreview.setOnClickListener(b -> {
                if (listener != null) listener.onPreview(link);
            });
            btnRemove.setOnClickListener(b -> {
                if (listener != null) listener.onRemove(link);
            });
        }

        private int dp(Context context, int value) {
            return Math.round(value * context.getResources().getDisplayMetrics().density);
        }
    }
}
