package com.example.taskflow;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

// Files tab: search + type chips + ListView of attachment links
public class FilesFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    private String projectId;
    private ProjectHost host;

    EditText etSearchFiles;
    ChipGroup chipGroupType;
    Chip chipAll, chipImages, chipText;
    TextView tvShowingFiles, tvEmptyFilesMessage;
    ListView lvFiles;
    View layoutEmptyFiles;
    Button btnEmptyFilesAction, btnAddAttachment;

    ArrayList<AttachmentLink> shownFiles;
    AttachmentAdapter adapter;

    private boolean updatingControls = false;

    public FilesFragment() {
        // Required empty public constructor
    }

    public static FilesFragment newInstance(String projectId) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        host = (ProjectHost) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        syncControlsFromQuery();
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncControlsFromQuery();
        refreshList();
    }

    private Project getProject() {
        Project p = host.getProject();
        return (p != null && p.getId().equals(projectId)) ? p : null;
    }

    private void init(View view) {
        etSearchFiles = view.findViewById(R.id.etSearchFiles);
        chipGroupType = view.findViewById(R.id.chipGroupType);
        chipAll = view.findViewById(R.id.chipAll);
        chipImages = view.findViewById(R.id.chipImages);
        chipText = view.findViewById(R.id.chipText);
        tvShowingFiles = view.findViewById(R.id.tvShowingFiles);
        lvFiles = view.findViewById(R.id.lvFiles);
        layoutEmptyFiles = view.findViewById(R.id.layoutEmptyFiles);
        tvEmptyFilesMessage = view.findViewById(R.id.tvEmptyFilesMessage);
        btnEmptyFilesAction = view.findViewById(R.id.btnEmptyFilesAction);
        btnAddAttachment = view.findViewById(R.id.btnAddAttachment);

        shownFiles = new ArrayList<>();
        adapter = new AttachmentAdapter(requireContext(), shownFiles, new AttachmentAdapter.OnAttachmentActionListener() {
            @Override
            public void onPreview(AttachmentLink link) {
                AttachmentDialogs.showPreview(requireContext(), link);
            }

            @Override
            public void onRemove(AttachmentLink link) {
                removeAttachment(link);
            }
        });
        lvFiles.setAdapter(adapter);

        etSearchFiles.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (updatingControls) return;
                host.getFileQuery().search = s.toString();
                refreshList();
            }
        });

        chipGroupType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (updatingControls || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            FileQuery q = host.getFileQuery();
            if (id == R.id.chipImages) q.type = CatalogItem.TYPE_IMAGE;
            else if (id == R.id.chipText) q.type = CatalogItem.TYPE_TEXT;
            else q.type = FileQuery.TYPE_ALL;
            refreshList();
        });

        btnAddAttachment.setOnClickListener(v -> addAttachment());

        // "Add Attachment" when empty, "Reset" when only the search/type hides everything
        btnEmptyFilesAction.setOnClickListener(v -> {
            Project p = getProject();
            if (p != null && p.getAttachmentCount() == 0) {
                addAttachment();
            } else {
                host.getFileQuery().reset();
                syncControlsFromQuery();
                refreshList();
            }
        });
    }

    private void syncControlsFromQuery() {
        FileQuery q = host.getFileQuery();
        updatingControls = true;
        if (!etSearchFiles.getText().toString().equals(q.search)) {
            etSearchFiles.setText(q.search);
        }
        if (q.type == CatalogItem.TYPE_IMAGE) chipGroupType.check(R.id.chipImages);
        else if (q.type == CatalogItem.TYPE_TEXT) chipGroupType.check(R.id.chipText);
        else chipGroupType.check(R.id.chipAll);
        updatingControls = false;
    }

    private void addAttachment() {
        Project p = getProject();
        if (p == null) return;
        AttachmentDialogs.showCatalogPicker(requireContext(), p, item -> {
            p.addAttachment(item);
            host.onProjectChanged();   // Files (n) tab title
            refreshList();
        });
    }

    private void removeAttachment(AttachmentLink link) {
        Project p = getProject();
        if (p == null) return;
        AttachmentDialogs.showRemoveConfirm(requireContext(), link, () -> {
            p.removeAttachmentById(link.getId());
            host.onProjectChanged();
            refreshList();
        });
    }

    private void refreshList() {
        Project p = getProject();
        if (p == null || adapter == null) return;

        // chip counts are for the WHOLE project collection
        chipAll.setText("All (" + p.getAttachmentCount() + ")");
        chipImages.setText("Images (" + p.getAttachmentCountByType(CatalogItem.TYPE_IMAGE) + ")");
        chipText.setText("Text (" + p.getAttachmentCountByType(CatalogItem.TYPE_TEXT) + ")");

        ArrayList<AttachmentLink> result = host.getFileQuery().apply(p.getAttachments());
        shownFiles.clear();
        shownFiles.addAll(result);
        adapter.notifyDataSetChanged();

        tvShowingFiles.setText("Showing " + result.size() + " of " + p.getAttachmentCount() + " attachments");

        if (p.getAttachmentCount() == 0) {
            layoutEmptyFiles.setVisibility(View.VISIBLE);
            tvEmptyFilesMessage.setText("No attachments yet");
            btnEmptyFilesAction.setText("Add Attachment");
        } else if (result.isEmpty()) {
            layoutEmptyFiles.setVisibility(View.VISIBLE);
            tvEmptyFilesMessage.setText("No attachments match your search or type");
            btnEmptyFilesAction.setText("Reset");
        } else {
            layoutEmptyFiles.setVisibility(View.GONE);
        }
    }
}
