package com.learn.hub.content;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public final class ContentModels {

    private ContentModels() {
    }

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
            List<RepoPointer> repoPointers
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
                    nonNull(repoPointers)
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

    private static <T> List<T> nonNull(List<T> value) {
        return value == null ? List.of() : List.copyOf(value);
    }
}
