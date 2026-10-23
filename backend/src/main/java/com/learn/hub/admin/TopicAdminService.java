package com.learn.hub.admin;

import com.learn.hub.content.CatalogStore;
import com.learn.hub.content.ContentLoader;
import com.learn.hub.content.ContentModels;
import com.learn.hub.persistence.TopicEntity;
import com.learn.hub.persistence.TopicRepository;
import com.learn.hub.topic.ConflictException;
import com.learn.hub.topic.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 管理端写服务：校验输入、写入数据库并刷新内存内容快照。
 */
@Service
public class TopicAdminService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]{1,79}$");
    private static final List<String> STATUSES = List.of("draft", "published");
    private static final List<String> INTEGRATION_STATUSES = List.of("docs_only", "snippet", "runtime");

    private final TopicRepository topicRepository;
    private final ContentLoader contentLoader;
    private final CatalogStore catalogStore;

    public TopicAdminService(
            TopicRepository topicRepository,
            ContentLoader contentLoader,
            CatalogStore catalogStore
    ) {
        this.topicRepository = topicRepository;
        this.contentLoader = contentLoader;
        this.catalogStore = catalogStore;
    }

    /**
     * 管理端列表（包含草稿），按 slug / 标题 / 分类做简单过滤。
     */
    @Transactional(readOnly = true)
    public List<ContentModels.TopicDocument> list(String q) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return topicRepository.findAll().stream()
                .map(entity -> contentLoader.parseTopic(entity.getPayload()))
                .filter(doc -> needle.isEmpty()
                        || contains(doc.slug(), needle)
                        || contains(doc.title(), needle)
                        || contains(doc.category(), needle))
                .sorted(Comparator.comparing(ContentModels.TopicDocument::slug))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentModels.TopicDocument get(String slug) {
        return contentLoader.parseTopic(entity(slug).getPayload());
    }

    /**
     * 新建学习项：校验通过后入库，并立即刷新内存快照。
     */
    @Transactional
    public ContentModels.TopicDocument create(ContentModels.TopicDocument input) {
        ContentModels.TopicDocument doc = normalize(input);
        validate(doc);
        if (topicRepository.existsBySlug(doc.slug())) {
            throw new ConflictException("TOPIC_EXISTS", "学习项已存在: " + doc.slug());
        }
        LocalDateTime now = LocalDateTime.now();
        topicRepository.save(new TopicEntity(
                doc.slug(),
                doc.title(),
                doc.category(),
                doc.difficulty(),
                doc.status(),
                doc.integrationStatus(),
                doc.estimatedHours(),
                doc.summary(),
                contentLoader.writeJson(doc),
                now
        ));
        catalogStore.refresh();
        return get(doc.slug());
    }

    /**
     * 更新学习项：路径里的 slug 优先，payload 中的 slug 会被覆盖。
     */
    @Transactional
    public ContentModels.TopicDocument update(String slug, ContentModels.TopicDocument input) {
        TopicEntity entity = entity(slug);
        ContentModels.TopicDocument doc = normalize(forceSlug(input, slug));
        validate(doc);
        entity.apply(
                doc.title(),
                doc.category(),
                doc.difficulty(),
                doc.status(),
                doc.integrationStatus(),
                doc.estimatedHours(),
                doc.summary(),
                contentLoader.writeJson(doc),
                LocalDateTime.now()
        );
        topicRepository.save(entity);
        catalogStore.refresh();
        return get(slug);
    }

    /**
     * 物理删除；关联的进度与笔记由外键级联清理。
     */
    @Transactional
    public void delete(String slug) {
        TopicEntity entity = entity(slug);
        topicRepository.delete(entity);
        catalogStore.refresh();
    }

    private TopicEntity entity(String slug) {
        return topicRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug));
    }

    private ContentModels.TopicDocument forceSlug(ContentModels.TopicDocument input, String slug) {
        return new ContentModels.TopicDocument(
                slug,
                input.title(),
                input.category(),
                input.summary(),
                input.difficulty(),
                input.estimatedHours(),
                input.tags(),
                input.status(),
                input.integrationStatus(),
                input.officialDocsUrl(),
                input.prerequisites(),
                input.nextTopics(),
                input.relatedTopics(),
                input.overviewMd(),
                input.concepts(),
                input.pathSteps(),
                input.samples(),
                input.pitfalls(),
                input.repoPointers(),
                input.scenarios()
        ).normalized();
    }

    private ContentModels.TopicDocument normalize(ContentModels.TopicDocument input) {
        if (input == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        return new ContentModels.TopicDocument(
                trim(input.slug()),
                trim(input.title()),
                trim(input.category()),
                trim(input.summary()),
                trim(input.difficulty()),
                input.estimatedHours(),
                input.tags(),
                trim(input.status()),
                trim(input.integrationStatus()),
                trim(input.officialDocsUrl()),
                input.prerequisites(),
                input.nextTopics(),
                input.relatedTopics(),
                input.overviewMd(),
                input.concepts(),
                input.pathSteps(),
                input.samples(),
                input.pitfalls(),
                input.repoPointers(),
                input.scenarios()
        ).normalized();
    }

    /**
     * 管理端写入门槛：slug 格式、必填字段、枚举值，以及已发布内容的完整性。
     */
    private void validate(ContentModels.TopicDocument doc) {
        if (doc.slug() == null || !SLUG_PATTERN.matcher(doc.slug()).matches()) {
            throw new IllegalArgumentException("slug 必须为 2-80 位小写字母、数字或连字符，且以字母数字开头");
        }
        if (isBlank(doc.title())) {
            throw new IllegalArgumentException("title 不能为空");
        }
        if (doc.title().length() > 200) {
            throw new IllegalArgumentException("title 不能超过 200 字");
        }
        if (isBlank(doc.category())) {
            throw new IllegalArgumentException("category 不能为空");
        }
        if (isBlank(doc.difficulty())) {
            throw new IllegalArgumentException("difficulty 不能为空");
        }
        if (!STATUSES.contains(doc.status())) {
            throw new IllegalArgumentException("status 只能是 " + STATUSES);
        }
        if (!INTEGRATION_STATUSES.contains(doc.integrationStatus())) {
            throw new IllegalArgumentException("integrationStatus 只能是 " + INTEGRATION_STATUSES);
        }
        if (doc.estimatedHours() < 0) {
            throw new IllegalArgumentException("estimatedHours 不能为负数");
        }
        try {
            contentLoader.validateOne(doc);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
