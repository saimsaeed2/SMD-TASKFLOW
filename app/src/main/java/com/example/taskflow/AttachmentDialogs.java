package com.example.taskflow;

import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

// All dialogs related to attachments: catalog picker, preview and remove confirmation.
public class AttachmentDialogs {

    public interface OnCatalogItemPicked {
        void onPicked(CatalogItem item);
    }

    // Single-choice list of catalog items that are NOT already linked.
    // project can be null (New Project dialog: nothing is linked yet).
    public static void showCatalogPicker(Context context, Project project, OnCatalogItemPicked listener) {
        ArrayList<CatalogItem> eligible = new ArrayList<>();
        for (CatalogItem item : AttachmentCatalog.getItems()) {
            if (project == null || !project.hasCatalogItem(item.getId())) {
                eligible.add(item);
            }
        }

        if (eligible.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle("Add Attachment")
                    .setMessage("All catalog files are already linked to this project.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] names = new String[eligible.size()];
        for (int i = 0; i < eligible.size(); i++) {
            names[i] = eligible.get(i).getName() + "  (" + eligible.get(i).getTypeLabel() + ")";
        }

        int[] selected = {-1};   // nothing selected at first

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Choose from catalog")
                .setSingleChoiceItems(names, -1, (d, which) -> {
                    selected[0] = which;
                    // enable Add once an item is selected
                    ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                })
                .setPositiveButton("Add", (d, which) -> {
                    if (selected[0] >= 0) {
                        listener.onPicked(eligible.get(selected[0]));
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        // Add is disabled until an eligible item is selected
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));
        dialog.show();
    }

    // Shows the REAL content: the image in an ImageView, or the text in a TextView
    public static void showPreview(Context context, AttachmentLink link) {
        int pad = dp(context, 16);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(link.getName())
                .setPositiveButton("Close", null);

        if (link.isImage()) {
            ImageView iv = new ImageView(context);
            iv.setImageResource(link.getResId());
            iv.setAdjustViewBounds(true);
            iv.setPadding(pad, pad, pad, pad);
            iv.setContentDescription(link.getName());
            builder.setView(iv);
        } else {
            TextView tv = new TextView(context);
            tv.setText(readRawText(context, link.getResId()));
            tv.setTextSize(14);
            tv.setTextColor(ContextCompat.getColor(context, R.color.text_dark));
            tv.setTextIsSelectable(true);
            tv.setPadding(pad + dp(context, 8), pad, pad + dp(context, 8), pad);

            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(tv);
            builder.setView(scrollView);
        }
        builder.show();
    }

    // The user must type the attachment name to confirm removing it
    public static void showRemoveConfirm(Context context, AttachmentLink link, Runnable onConfirmed) {
        showRemoveConfirm(context, link, onConfirmed, null);
    }

    // Prompts the user to type the exact attachment name before removing it.
    // Calls onCancelled whenever the user cancels, dismisses, or enters a non-matching name.
    public static void showRemoveConfirm(Context context, AttachmentLink link,
                                         Runnable onConfirmed, @Nullable Runnable onCancelled) {
        int pad = dp(context, 24);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, dp(context, 8), pad, 0);

        TextView tvMessage = new TextView(context);
        tvMessage.setText("Type \"" + link.getName() + "\" to confirm removing this attachment.");
        tvMessage.setTextColor(ContextCompat.getColor(context, R.color.text_body));

        EditText etConfirm = new EditText(context);
        etConfirm.setHint(link.getName());
        etConfirm.setSingleLine(true);

        layout.addView(tvMessage);
        layout.addView(etConfirm);

        final boolean[] confirmed = {false};

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Remove attachment?")
                .setView(layout)
                .setPositiveButton("Remove", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String entered = etConfirm.getText().toString().trim();
                    if (entered.equals(link.getName())) {
                        confirmed[0] = true;
                        onConfirmed.run();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Attachment name does not match", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }));

        dialog.setOnDismissListener(d -> {
            if (!confirmed[0] && onCancelled != null) {
                onCancelled.run();
            }
        });

        dialog.show();
    }

    // Reads a text file from res/raw
    private static String readRawText(Context context, int resId) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            return "Could not open this file.";
        }
        return sb.toString();
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
