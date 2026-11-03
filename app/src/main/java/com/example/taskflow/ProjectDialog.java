package com.example.taskflow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

// AlertDialog for "+ New Project" (existing == null) and "Edit Project" (existing != null)
public class ProjectDialog {

    public interface OnProjectSaved {
        // attachment is null when nothing was selected (always null for Edit)
        void onSaved(String name, String tag, String description, CatalogItem attachment);
    }

    public static void show(Context context, Project existing, OnProjectSaved listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_project, null);

        EditText etName = view.findViewById(R.id.etProjectName);
        EditText etTag = view.findViewById(R.id.etProjectTag);
        EditText etDescription = view.findViewById(R.id.etProjectDescription);
        LinearLayout layoutAttachment = view.findViewById(R.id.layoutAttachment);
        TextView tvSelected = view.findViewById(R.id.tvSelectedAttachment);
        Button btnChoose = view.findViewById(R.id.btnChooseAttachment);
        Button btnClear = view.findViewById(R.id.btnClearAttachment);

        boolean isEdit = existing != null;

        // array with one item so the lambdas below can change it
        CatalogItem[] selected = {null};

        if (isEdit) {
            etName.setText(existing.getName());
            etTag.setText(existing.getTag());
            etDescription.setText(existing.getDescription());
            layoutAttachment.setVisibility(View.GONE);
        } else {
            btnChoose.setOnClickListener(v ->
                    AttachmentDialogs.showCatalogPicker(context, null, item -> {
                        selected[0] = item;
                        tvSelected.setText(item.getName() + " (" + item.getTypeLabel() + ")");
                        btnClear.setVisibility(View.VISIBLE);
                    }));
            btnClear.setOnClickListener(v -> {
                selected[0] = null;
                tvSelected.setText("No attachment selected");
                btnClear.setVisibility(View.GONE);
            });
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(isEdit ? "Edit Project" : "New Project")
                .setView(view)
                .setPositiveButton(isEdit ? "Save" : "Add", null)   // listener set below so we can validate
                .setNegativeButton("Cancel", null)                  // Cancel just closes the dialog
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    // trim spaces first; whitespace-only = empty = invalid
                    String name = etName.getText().toString().trim();
                    String tag = etTag.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    boolean valid = true;
                    if (name.isEmpty() || name.length() > 60) {
                        etName.setError("Name must be 1-60 characters");
                        valid = false;
                    }
                    if (tag.isEmpty() || tag.length() > 20) {
                        etTag.setError("Tag must be 1-20 characters");
                        valid = false;
                    }
                    if (description.isEmpty() || description.length() > 500) {
                        etDescription.setError("Description must be 1-500 characters");
                        valid = false;
                    }
                    if (!valid) return;   // keep the dialog open

                    listener.onSaved(name, tag, description, selected[0]);
                    dialog.dismiss();
                }));

        dialog.show();
    }
}
