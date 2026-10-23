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

/**
 * 只读查询服务：列表筛选与分页、详情组装、路线展开、分类统计。
 */
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

    /**
     * 返回当前内容快照；快照为空时懒加载（首次请求的兜底）。
     */
    private ContentCatalog catalog() {
        return fixedCatalog != null ? fixedCatalog : catalogStore.get();
    }

    /**
     * 分页查询已发布学习项，支持分类、难度、接入状态与关键词组合筛选。
     */
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

    /**
     * 查询单个学习项详情；草稿与不存在的 slug 一律按 404 处理。
     */
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

    /**
     * 全部路线摘要，供前台选择要查看的路线。
     */
    public List<TopicDtos.TrackSummary> tracks() {
        return catalog().tracks().stream()
                .map(track -> new TopicDtos.TrackSummary(track.slug(), track.title(), track.audience()))
                .sorted(Comparator.comparing(TopicDtos.TrackSummary::title))
                .toList();
    }

    /**
     * 展开路线：把阶段里引用的学习项解析为摘要，自动跳过草稿与失效引用。
     */
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

    /**
     * 按分类统计已发布学习项数量，供目录页筛选器使用。
     */
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
