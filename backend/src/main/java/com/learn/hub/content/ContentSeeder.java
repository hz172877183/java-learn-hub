package com.learn.hub.content;

import com.learn.hub.persistence.TopicEntity;
import com.learn.hub.persistence.TopicRepository;
import com.learn.hub.persistence.TrackEntity;
import com.learn.hub.persistence.TrackRepository;
import com.learn.hub.quiz.QuizQuestionRepository;
import com.learn.hub.quiz.QuizService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 首次启动时把 classpath 里的 content/*.json 导入数据库作为种子数据。
 * 库中已有数据时不再覆盖。
 */
@Component
public class ContentSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentSeeder.class);

    private final ContentLoader contentLoader;
    private final CatalogStore catalogStore;
    private final TopicRepository topicRepository;
    private final TrackRepository trackRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizService quizService;

    public ContentSeeder(
            ContentLoader contentLoader,
            CatalogStore catalogStore,
            TopicRepository topicRepository,
            TrackRepository trackRepository,
            QuizQuestionRepository quizQuestionRepository,
            QuizService quizService
    ) {
        this.contentLoader = contentLoader;
        this.catalogStore = catalogStore;
        this.topicRepository = topicRepository;
        this.trackRepository = trackRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizService = quizService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LocalDateTime now = LocalDateTime.now();

        if (topicRepository.count() == 0) {
            List<ContentModels.TopicDocument> topics = contentLoader.readSeedTopics();
            for (ContentModels.TopicDocument doc : topics) {
                topicRepository.save(new TopicEntity(
                        doc.slug(),
                        doc.title(),
                        orDefault(doc.category(), "uncategorized"),
                        orDefault(doc.difficulty(), "beginner"),
                        orDefault(doc.status(), "draft"),
                        orDefault(doc.integrationStatus(), "docs_only"),
                        doc.estimatedHours(),
                        doc.summary(),
                        contentLoader.writeJson(doc),
                        now
                ));
            }
            log.info("已导入种子学习项 {} 条", topics.size());
        }

        if (trackRepository.count() == 0) {
            List<ContentModels.TrackDocument> tracks = contentLoader.readSeedTracks();
            for (ContentModels.TrackDocument doc : tracks) {
                trackRepository.save(new TrackEntity(
                        doc.slug(),
                        doc.title(),
                        doc.audience(),
                        contentLoader.writeJson(doc),
                        now
                ));
            }
            log.info("已导入种子路线 {} 条", tracks.size());
        }

        catalogStore.refresh();

        if (quizQuestionRepository.count() == 0) {
            int imported = 0;
            for (ContentLoader.SeedQuiz seed : contentLoader.readSeedQuizzes()) {
                if (topicRepository.existsBySlug(seed.topicSlug())) {
                    quizService.seed(seed.topicSlug(), seed.questions());
                    imported += seed.questions().size();
                } else {
                    log.warn("跳过题目种子：学习项不存在 {}", seed.topicSlug());
                }
            }
            log.info("已导入面试题 {} 道", imported);
        }
    }

    private String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
