package com.learn.hub.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 内容模型：与 content/*.json 对应的记录类型，缺失字段统一归一化。
 */
public final class ContentModels {

    private ContentModels() {
    }

    /** 与 content/topics/*.json 对应的完整学习项。 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TopicDocument(
            String slug,
            String title,
            String category,
            String summary,
            String difficulty,
            Integer estimatedHours,
            List<String> tags,
            String status,
            String integrationStatus,
            String officialDocsUrl,
            List<String> prerequisites,
            List<String> nextTopics,
            List<String> relatedTopics,
            String overviewMd,
            List<Concept> concepts,
            List<PathStep> pathSteps,
            List<Sample> samples,
            List<Pitfall> pitfalls,
            List<RepoPointer> repoPointers,
            List<Scenario> scenarios
    ) {
        public TopicDocument normalized() {
            return new TopicDocument(
                    slug,
                    title,
                    category,
                    summary,
                    difficulty,
                    estimatedHours == null ? 0 : estimatedHours,
                    nonNull(tags),
                    status,
                    integrationStatus,
                    officialDocsUrl,
                    nonNull(prerequisites),
                    nonNull(nextTopics),
                    nonNull(relatedTopics),
                    overviewMd == null ? "" : overviewMd,
                    nonNull(concepts),
                    nonNull(pathSteps),
                    nonNull(samples),
                    nonNull(pitfalls),
                    nonNull(repoPointers),
                    nonNull(scenarios)
            );
        }

        public boolean published() {
            return "published".equals(status);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Concept(String term, String explanation) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PathStep(String title, String goal, Double hours, List<String> resources) {
        public PathStep {
            resources = resources == null ? List.of() : resources;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sample(String title, String language, String description, String code) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pitfall(String title, String detail) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepoPointer(String label, String path) {
    }

    /**
     * 技术栈的典型使用场景：什么信号出现时，应该想到用它。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Scenario(String title, String signal, String approach) {
    }

    /**
     * 面试真题。type = choice（单选）或 text（自答）。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QuizQuestion(
            String type,
            String stem,
            List<String> options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation
    ) {
        public QuizQuestion normalized() {
            return new QuizQuestion(
                    type == null || type.isBlank() ? "text" : type,
                    stem,
                    options == null ? List.of() : List.copyOf(options),
                    correctIndex,
                    referenceAnswer,
                    explanation
            );
        }

        @JsonIgnore
        public boolean isChoice() {
            return "choice".equals(type);
        }
    }

    /** 与 content/tracks/*.json 对应的路线。 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrackDocument(
            String slug,
            String title,
            String audience,
            List<Stage> stages
    ) {
        public TrackDocument normalized() {
            return new TrackDocument(slug, title, audience, stages == null ? List.of() : stages);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stage(String title, String goal, List<String> topicSlugs) {
        public Stage {
            topicSlugs = topicSlugs == null ? List.of() : topicSlugs;
        }
    }

    /**
     * 面试题种子：content/interview/questions.json 的数组元素。
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InterviewSeed(
            String category,
            String level,
            String type,
            String stem,
            List<String> options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation
    ) {
    }

    private static <T> List<T> nonNull(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }
}
