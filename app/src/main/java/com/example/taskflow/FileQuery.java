package com.example.taskflow;

import java.io.Serializable;
import java.util.ArrayList;

// Holds the search text and type chip of the Files tab.
public class FileQuery implements Serializable {

    public static final int TYPE_ALL = -1;   // otherwise CatalogItem.TYPE_IMAGE / TYPE_TEXT

    public String search;
    public int type;

    public FileQuery() {
        reset();
    }

    public void reset() {
        search = "";
        type = TYPE_ALL;
    }

    public boolean matches(AttachmentLink link) {
        String s = search.trim().toLowerCase();
        if (!s.isEmpty() && !link.getName().toLowerCase().contains(s)) return false;
        if (type != TYPE_ALL && link.getType() != type) return false;
        return true;
    }

    // Keeps link-insertion order (no sorting)
    public ArrayList<AttachmentLink> apply(ArrayList<AttachmentLink> all) {
        ArrayList<AttachmentLink> result = new ArrayList<>();
        for (AttachmentLink link : all) {
            if (matches(link)) result.add(link);
        }
        return result;
    }
}
