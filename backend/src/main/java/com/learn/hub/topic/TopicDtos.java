package com.learn.hub.topic;

import com.learn.hub.content.ContentModels;

import java.util.List;

public final class TopicDtos {

    private TopicDtos() {
    }

    public record TopicSummary(
            String slug,
            String title,
            String category,
            String summary,
            String difficulty,
            int estimatedHours,
            List<String> tags,
            String integrationStatus
    ) {
        static TopicSummary from(ContentModels.TopicDocument doc) {
            return new TopicSummary(
                    doc.slug(),
                    doc.title(),
                    doc.category(),
                    doc.summary(),
                    doc.difficulty(),
                    doc.estimatedHours(),
                    doc.tags(),
                    doc.integrationStatus()
            );
        }
    }

    public record TopicRef(String slug, String title) {
    }

    public record TopicDetail(
            String slug,
            String title,
            String category,
            String summary,
            String difficulty,
            int estimatedHours,
            List<String> tags,
            String integrationStatus,
            String officialDocsUrl,
            String overviewMd,
            List<ContentModels.Concept> concepts,
            List<ContentModels.PathStep> pathSteps,
            List<ContentModels.Sample> samples,
            List<ContentModels.Pitfall> pitfalls,
            List<TopicRef> prerequisites,
            List<TopicRef> nextTopics,
            List<TopicRef> relatedTopics,
            List<ContentModels.RepoPointer> repoPointers,
            List<ContentModels.Scenario> scenarios
    ) {
    }

    public record TopicListResponse(
            List<TopicSummary> items,
            int page,
            int size,
            long total
    ) {
    }

    public record CategoryStat(String id, long count) {
    }

    public record MetaResponse(List<CategoryStat> categories, long publishedTotal) {
    }

    public record TrackStage(String title, String goal, List<TopicSummary> topics) {
    }

    public record TrackResponse(String slug, String title, String audience, List<TrackStage> stages) {
    }
}
