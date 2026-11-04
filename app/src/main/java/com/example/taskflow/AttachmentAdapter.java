package com.example.taskflow;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// Custom adapter for the Files ListView (uses row_attachment.xml)
public class AttachmentAdapter extends ArrayAdapter<AttachmentLink> {

    public interface OnAttachmentActionListener {
        void onPreview(AttachmentLink link);

        void onRemove(AttachmentLink link);
    }

    private final OnAttachmentActionListener listener;

    public AttachmentAdapter(@NonNull Context context, ArrayList<AttachmentLink> links,
                             OnAttachmentActionListener listener) {
        super(context, 0, links);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.row_attachment, parent, false);
        }

        AttachmentLink link = getItem(position);

        ImageView ivThumb = v.findViewById(R.id.ivThumb);
        TextView tvFileName = v.findViewById(R.id.tvFileName);
        TextView tvFileMeta = v.findViewById(R.id.tvFileMeta);
        Button btnPreview = v.findViewById(R.id.btnPreview);
        ImageButton btnRemove = v.findViewById(R.id.btnRemove);

        int pad;
        if (link.isImage()) {
            // real thumbnail of the image
            ivThumb.setImageResource(link.getResId());
            ivThumb.setImageTintList(null);
            ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivThumb.setBackgroundResource(R.drawable.bg_icon_box_image);
            pad = dp(2);
        } else {
            // document icon for text files
            ivThumb.setImageResource(R.drawable.ic_doc);
            ivThumb.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.due_today)));
            ivThumb.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivThumb.setBackgroundResource(R.drawable.bg_icon_box_text);
            pad = dp(10);
        }
        ivThumb.setPadding(pad, pad, pad, pad);

        tvFileName.setText(link.getName());
        tvFileMeta.setText(link.getTypeLabel() + " • " + link.getSizeLabel()
                + " • Linked " + DateUtils.format(link.getLinkedAt()));

        btnPreview.setOnClickListener(b -> listener.onPreview(link));
        btnRemove.setOnClickListener(b -> listener.onRemove(link));

        return v;
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }
}
