package com.example.taskflow;

// One file in the local attachment catalog (see AttachmentCatalog).
public class CatalogItem {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_TEXT = 1;

    private String id;
    private String name;
    private int type;
    private int resId;
    private String sizeLabel;

    public CatalogItem(String id, String name, int type, int resId, String sizeLabel) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.resId = resId;
        this.sizeLabel = sizeLabel;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getType() { return type; }
    public int getResId() { return resId; }
    public String getSizeLabel() { return sizeLabel; }

    public String getTypeLabel() { return type == TYPE_IMAGE ? "Image" : "Text"; }
}
