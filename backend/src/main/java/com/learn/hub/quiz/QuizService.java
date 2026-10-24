package com.learn.hub.quiz;

import com.learn.hub.content.CatalogStore;
import com.learn.hub.content.ContentLoader;
import com.learn.hub.content.ContentModels;
import com.learn.hub.topic.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目服务：读取、校验与整组替换。
 */
@Service
public class QuizService {

    private static final List<String> TYPES = List.of("choice", "text");

    private final QuizQuestionRepository quizQuestionRepository;
    private final ContentLoader contentLoader;
    private final CatalogStore catalogStore;

    public QuizService(
            QuizQuestionRepository quizQuestionRepository,
            ContentLoader contentLoader,
            CatalogStore catalogStore
    ) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.contentLoader = contentLoader;
        this.catalogStore = catalogStore;
    }

    /**
     * 公开读取：只允许已发布学习项的题目，草稿与未知 slug 返回 404。
     */
    @Transactional(readOnly = true)
    public List<QuizQuestionView> listPublished(String slug) {
        ContentModels.TopicDocument topic = catalogStore.get().topic(slug)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug));
        if (!topic.published()) {
            throw new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug);
        }
        return list(slug);
    }

    /**
     * 按排序号读取某个学习项的全部题目（管理端也复用）。
     */
    @Transactional(readOnly = true)
    public List<QuizQuestionView> list(String slug) {
        return quizQuestionRepository.findByTopicSlugOrderBySortOrderAscIdAsc(slug).stream()
                .map(entity -> QuizQuestionView.of(entity, contentLoader.parseQuiz(entity.getPayload())))
                .toList();
    }

    /**
     * 管理端整组替换：先清空该学习项的题目，再按顺序写入。
     */
    /**
     * 管理端整组替换：先校验全部题目，再删旧插新，避免部分写入。
     */
    @Transactional
    public List<QuizQuestionView> replace(String slug, List<ContentModels.QuizQuestion> questions) {
        if (catalogStore.get().topic(slug).isEmpty()) {
            throw new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug);
        }
        List<ContentModels.QuizQuestion> normalized = new ArrayList<>();
        if (questions != null) {
            for (ContentModels.QuizQuestion question : questions) {
                ContentModels.QuizQuestion value = question == null ? null : question.normalized();
                validate(value);
                normalized.add(value);
            }
        }

        quizQuestionRepository.deleteByTopicSlug(slug);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < normalized.size(); i++) {
            ContentModels.QuizQuestion question = normalized.get(i);
            quizQuestionRepository.save(new QuizQuestionEntity(
                    slug,
                    question.type(),
                    contentLoader.writeJson(question),
                    i,
                    now
            ));
        }
        return list(slug);
    }

    /**
     * 首次启动导入种子题目。
     */
    @Transactional
    public void seed(String slug, List<ContentModels.QuizQuestion> questions) {
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < questions.size(); i++) {
            ContentModels.QuizQuestion question = questions.get(i).normalized();
            validate(question);
            quizQuestionRepository.save(new QuizQuestionEntity(
                    slug,
                    question.type(),
                    contentLoader.writeJson(question),
                    i,
                    now
            ));
        }
    }

    /**
     * 校验题型与必填项：选择题要求 2-6 个选项与正确下标，自答题要求参考答案。
     */
    private void validate(ContentModels.QuizQuestion question) {
        if (question == null) {
            throw new IllegalArgumentException("题目不能为空");
        }
        if (!TYPES.contains(question.type())) {
            throw new IllegalArgumentException("题型只能是 " + TYPES);
        }
        if (isBlank(question.stem())) {
            throw new IllegalArgumentException("题干不能为空");
        }
        if (question.stem().length() > 1000) {
            throw new IllegalArgumentException("题干不能超过 1000 字");
        }
        if (isBlank(question.explanation())) {
            throw new IllegalArgumentException("每道题都必须有知识点说明");
        }
        if (question.isChoice()) {
            List<String> options = question.options();
            if (options.size() < 2 || options.size() > 6) {
                throw new IllegalArgumentException("选择题需要 2-6 个选项");
            }
            for (String option : options) {
                if (isBlank(option)) {
                    throw new IllegalArgumentException("选项内容不能为空");
                }
            }
            Integer correct = question.correctIndex();
            if (correct == null || correct < 0 || correct >= options.size()) {
                throw new IllegalArgumentException("选择题必须指定正确的选项下标");
            }
        } else if (isBlank(question.referenceAnswer())) {
            throw new IllegalArgumentException("自答题必须提供参考答案");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
