package com.example.taskflow;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

// The ONE task dialog, used by Overview (Add Task) and Tasks (Add + Edit).
// existing == null -> add a new task, otherwise edit that task.
public class TaskDialog {

    public interface OnTaskSaved {
        void onTaskSaved();
    }

    public static void show(Context context, Project project, Task existing, OnTaskSaved listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_task, null);

        EditText etDescription = view.findViewById(R.id.etTaskDescription);
        TextView tvCounter = view.findViewById(R.id.tvCharCounter);
        Spinner spPriority = view.findViewById(R.id.spTaskPriority);
        Spinner spStatus = view.findViewById(R.id.spTaskStatus);
        TextView tvDueDate = view.findViewById(R.id.tvTaskDueDate);
        Button btnPickDate = view.findViewById(R.id.btnPickDate);
        Button btnClearDate = view.findViewById(R.id.btnClearDate);

        spPriority.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, Task.PRIORITY_LABELS));
        spStatus.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, Task.STATUS_LABELS));

        boolean isEdit = existing != null;
        long[] dueDate = {Task.NO_DUE_DATE};   // array so the lambdas can change it

        if (isEdit) {
            etDescription.setText(existing.getDescription());   // full description
            spPriority.setSelection(existing.getPriority());
            spStatus.setSelection(existing.getStatus());
            dueDate[0] = existing.getDueDate();
        } else {
            // defaults: Normal, To do, no due date
            spPriority.setSelection(Task.PRIORITY_NORMAL);
            spStatus.setSelection(Task.STATUS_TODO);
        }

        // live 0/500 counter
        tvCounter.setText(etDescription.length() + "/500");
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                tvCounter.setText(s.length() + "/500");
            }
        });

        showDueDate(tvDueDate, btnClearDate, dueDate[0]);

        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            if (dueDate[0] != Task.NO_DUE_DATE) {
                c.setTimeInMillis(dueDate[0]);
            }
            // past dates are allowed, so no min date is set
            new DatePickerDialog(context, (picker, year, month, day) -> {
                dueDate[0] = DateUtils.dateOf(year, month, day);
                showDueDate(tvDueDate, btnClearDate, dueDate[0]);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnClearDate.setOnClickListener(v -> {
            dueDate[0] = Task.NO_DUE_DATE;
            showDueDate(tvDueDate, btnClearDate, dueDate[0]);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(isEdit ? "Edit Task" : "New Task")
                .setView(view)
                .setPositiveButton(isEdit ? "Save" : "Add", null)
                .setNegativeButton("Cancel", null);
        if (isEdit) {
            builder.setNeutralButton("Delete", null);
        }
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String description = etDescription.getText().toString().trim();
                if (description.isEmpty() || description.length() > 500) {
                    etDescription.setError("Description must be 1-500 characters");
                    return;
                }
                int priority = spPriority.getSelectedItemPosition();
                int status = spStatus.getSelectedItemPosition();

                if (isEdit) {
                    // find the task by its ID inside the working project
                    Task task = project.findTaskById(existing.getId());
                    if (task != null) {
                        task.setDescription(description);
                        task.setPriority(priority);
                        task.setStatus(status);
                        task.setDueDate(dueDate[0]);
                    }
                } else {
                    project.addTask(new Task(description, status, priority, dueDate[0]));
                }
                listener.onTaskSaved();
                dialog.dismiss();
            });

            if (isEdit) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                        new AlertDialog.Builder(context)
                                .setTitle("Delete task?")
                                .setMessage("This task will be removed from the project.")
                                .setPositiveButton("Delete", (d2, which) -> {
                                    project.removeTaskById(existing.getId());
                                    listener.onTaskSaved();
                                    dialog.dismiss();
                                })
                                .setNegativeButton("Cancel", null)
                                .show());
            }
        });

        dialog.show();
    }

    private static void showDueDate(TextView tvDueDate, Button btnClear, long dueDate) {
        if (dueDate == Task.NO_DUE_DATE) {
            tvDueDate.setText("No due date");
            btnClear.setEnabled(false);
        } else {
            tvDueDate.setText(DateUtils.format(dueDate));
            btnClear.setEnabled(true);
        }
    }
}
