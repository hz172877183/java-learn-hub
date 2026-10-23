package com.learn.hub.topic;

import com.learn.hub.content.CatalogStore;
import com.learn.hub.content.ContentCatalog;
import com.learn.hub.content.ContentModels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class TopicService {

    private final CatalogStore catalogStore;
    private final ContentCatalog fixedCatalog;

    @Autowired
    public TopicService(CatalogStore catalogStore) {
        this.catalogStore = catalogStore;
        this.fixedCatalog = null;
    }

    TopicService(ContentCatalog catalog) {
        this.catalogStore = null;
        this.fixedCatalog = catalog;
    }

    private ContentCatalog catalog() {
        return fixedCatalog != null ? fixedCatalog : catalogStore.get();
    }

    public TopicDtos.TopicListResponse list(
            String category,
            String difficulty,
            String integrationStatus,
            String q,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 从 0 起，size 范围 1-100");
        }
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        List<TopicDtos.TopicSummary> all = catalog().topics().stream()
                .filter(ContentModels.TopicDocument::published)
                .filter(doc -> category == null || category.isBlank() || category.equals(doc.category()))
                .filter(doc -> difficulty == null || difficulty.isBlank() || difficulty.equals(doc.difficulty()))
                .filter(doc -> integrationStatus == null || integrationStatus.isBlank()
                        || integrationStatus.equals(doc.integrationStatus()))
                .filter(doc -> needle.isEmpty() || matches(doc, needle))
                .sorted(Comparator.comparing(ContentModels.TopicDocument::title))
                .map(TopicDtos.TopicSummary::from)
                .toList();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return new TopicDtos.TopicListResponse(all.subList(from, to), page, size, all.size());
    }

    public TopicDtos.TopicDetail detail(String slug) {
        ContentModels.TopicDocument doc = catalog().topic(slug)
                .filter(ContentModels.TopicDocument::published)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug));
        return new TopicDtos.TopicDetail(
                doc.slug(),
                doc.title(),
                doc.category(),
                doc.summary(),
                doc.difficulty(),
                doc.estimatedHours(),
                doc.tags(),
                doc.integrationStatus(),
                doc.officialDocsUrl(),
                doc.overviewMd(),
                doc.concepts(),
                doc.pathSteps(),
                doc.samples(),
                doc.pitfalls(),
                refs(doc.prerequisites()),
                refs(doc.nextTopics()),
                refs(doc.relatedTopics()),
                doc.repoPointers(),
                doc.scenarios()
        );
    }

    public TopicDtos.TrackResponse track(String slug) {
        ContentModels.TrackDocument track = catalog().track(slug)
                .orElseThrow(() -> new NotFoundException("TRACK_NOT_FOUND", "未找到学习路线: " + slug));
        List<TopicDtos.TrackStage> stages = track.stages().stream()
                .map(stage -> new TopicDtos.TrackStage(
                        stage.title(),
                        stage.goal(),
                        stage.topicSlugs().stream()
                                .map(topicSlug -> catalog().topic(topicSlug).orElse(null))
                                .filter(Objects::nonNull)
                                .filter(ContentModels.TopicDocument::published)
                                .map(TopicDtos.TopicSummary::from)
                                .toList()
                ))
                .toList();
        return new TopicDtos.TrackResponse(track.slug(), track.title(), track.audience(), stages);
    }

    public TopicDtos.MetaResponse meta() {
        List<ContentModels.TopicDocument> published = catalog().topics().stream()
                .filter(ContentModels.TopicDocument::published)
                .toList();
        List<TopicDtos.CategoryStat> categories = published.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ContentModels.TopicDocument::category,
                        java.util.TreeMap::new,
                        java.util.stream.Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(e -> new TopicDtos.CategoryStat(e.getKey(), e.getValue()))
                .toList();
        return new TopicDtos.MetaResponse(categories, published.size());
    }

    private List<TopicDtos.TopicRef> refs(List<String> slugs) {
        return slugs.stream()
                .map(slug -> catalog().topic(slug).orElse(null))
                .filter(Objects::nonNull)
                .filter(ContentModels.TopicDocument::published)
                .map(doc -> new TopicDtos.TopicRef(doc.slug(), doc.title()))
                .toList();
    }

    private boolean matches(ContentModels.TopicDocument doc, String needle) {
        if (contains(doc.title(), needle) || contains(doc.summary(), needle) || contains(doc.slug(), needle)) {
            return true;
        }
        return doc.tags().stream().anyMatch(tag -> contains(tag, needle));
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
