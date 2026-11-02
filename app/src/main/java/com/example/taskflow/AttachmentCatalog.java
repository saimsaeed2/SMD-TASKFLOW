package com.example.taskflow;

import java.util.ArrayList;

// The local catalog of files that can be attached to a project.
// Images live in res/drawable (PNG/JPG), text documents live in res/raw (.txt).
public class AttachmentCatalog {

    private static ArrayList<CatalogItem> items;

    public static ArrayList<CatalogItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
            items.add(new CatalogItem("cat_1", "sports-project-v2.png", CatalogItem.TYPE_IMAGE, R.drawable.img_sports_project, "2.4 MB"));
            items.add(new CatalogItem("cat_2", "client-brief.txt", CatalogItem.TYPE_TEXT, R.raw.client_brief, "4 KB"));
            items.add(new CatalogItem("cat_3", "mount-asset.png", CatalogItem.TYPE_IMAGE, R.drawable.img_mount_asset, "1.1 MB"));
            items.add(new CatalogItem("cat_4", "home-wireframe.jpg", CatalogItem.TYPE_IMAGE, R.drawable.img_home_wireframe, "860 KB"));
            items.add(new CatalogItem("cat_5", "meeting-notes.txt", CatalogItem.TYPE_TEXT, R.raw.meeting_notes, "2 KB"));
            items.add(new CatalogItem("cat_6", "api-endpoints.txt", CatalogItem.TYPE_TEXT, R.raw.api_endpoints, "3 KB"));
            items.add(new CatalogItem("cat_7", "color-palette.png", CatalogItem.TYPE_IMAGE, R.drawable.img_color_palette, "640 KB"));
        }
        return items;
    }

    public static CatalogItem findById(String catalogId) {
        for (CatalogItem item : getItems()) {
            if (item.getId().equals(catalogId)) {
                return item;
            }
        }
        return null;
    }
}
