package com.learn.hub.content;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 内容只读快照：按 slug 索引学习项与路线，供读接口使用。
 */
public final class ContentCatalog {

    private final Map<String, ContentModels.TopicDocument> topics;
    private final Map<String, ContentModels.TrackDocument> tracks;

    public ContentCatalog(
            Map<String, ContentModels.TopicDocument> topics,
            Map<String, ContentModels.TrackDocument> tracks
    ) {
        this.topics = Map.copyOf(new LinkedHashMap<>(topics));
        this.tracks = Map.copyOf(new LinkedHashMap<>(tracks));
    }

    public Collection<ContentModels.TopicDocument> topics() {
        return topics.values();
    }

    public Optional<ContentModels.TopicDocument> topic(String slug) {
        return Optional.ofNullable(topics.get(slug));
    }

    public Collection<ContentModels.TrackDocument> tracks() {
        return tracks.values();
    }

    public Optional<ContentModels.TrackDocument> track(String slug) {
        return Optional.ofNullable(tracks.get(slug));
    }
}
