package com.example.taskflow;

import java.io.Serializable;
import java.util.UUID;

// A link between a project and one item of the attachment catalog.
// We only store the catalog id and the resource id (a reference), never the image bytes.
public class AttachmentLink implements Serializable {

    private String id;          // unique link id
    private String catalogId;   // which catalog item is linked
    private int resId;          // R.drawable.xxx for images, R.raw.xxx for text files
    private String name;
    private int type;           // CatalogItem.TYPE_IMAGE or CatalogItem.TYPE_TEXT
    private String sizeLabel;   // sample metadata only
    private long linkedAt;

    public AttachmentLink(CatalogItem item) {
        this.id = UUID.randomUUID().toString();
        this.catalogId = item.getId();
        this.resId = item.getResId();
        this.name = item.getName();
        this.type = item.getType();
        this.sizeLabel = item.getSizeLabel();
        this.linkedAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public String getCatalogId() { return catalogId; }
    public int getResId() { return resId; }
    public String getName() { return name; }
    public int getType() { return type; }
    public String getSizeLabel() { return sizeLabel; }
    public long getLinkedAt() { return linkedAt; }

    public boolean isImage() { return type == CatalogItem.TYPE_IMAGE; }

    public String getTypeLabel() { return isImage() ? "Image" : "Text"; }
}
