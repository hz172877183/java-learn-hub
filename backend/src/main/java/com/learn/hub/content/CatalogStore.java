package com.learn.hub.content;

import org.springframework.stereotype.Component;

/**
 * 持有当前内容快照。所有写操作落库后调用 {@link #refresh()} 重建快照。
 */
@Component
public class CatalogStore {

    private final ContentLoader contentLoader;
    private volatile ContentCatalog catalog;

    public CatalogStore(ContentLoader contentLoader) {
        this.contentLoader = contentLoader;
    }

    public ContentCatalog get() {
        ContentCatalog current = catalog;
        if (current == null) {
            synchronized (this) {
                if (catalog == null) {
                    catalog = contentLoader.load();
                }
                current = catalog;
            }
        }
        return current;
    }

    public void refresh() {
        synchronized (this) {
            catalog = contentLoader.load();
        }
    }
}
