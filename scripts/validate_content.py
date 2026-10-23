#!/usr/bin/env python3
"""校验 content/ 下的学习项、随堂题、路线与面试题库。

用法：
    python scripts/validate_content.py
"""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTENT = ROOT / "content"
TOPICS = CONTENT / "topics"
QUIZZES = CONTENT / "quizzes"
TRACKS = CONTENT / "tracks"
INTERVIEW = CONTENT / "interview" / "questions.json"

CATEGORIES = {
    "spring-core", "data", "cache", "messaging", "security", "engineering",
    "frontend", "cloud", "search", "observability", "ai",
}
DIFFICULTIES = {"beginner", "intermediate", "practical"}
INTEGRATIONS = {"docs_only", "snippet", "runtime"}
LEVELS = {"junior", "mid", "senior", "architect"}
TYPES = {"choice", "text"}

errors: list[str] = []
warnings: list[str] = []


def error(message: str) -> None:
    errors.append(message)


def warning(message: str) -> None:
    warnings.append(message)


def load_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:  # noqa: BLE001
        error(f"{path.relative_to(ROOT)} JSON 解析失败: {exc}")
        return None


def validate_topics() -> dict[str, dict]:
    topics: dict[str, dict] = {}
    refs: list[tuple[str, str, str]] = []
    for path in sorted(TOPICS.glob("*.json")):
        doc = load_json(path)
        if not isinstance(doc, dict):
            continue
        slug = doc.get("slug")
        if not slug:
            error(f"{path.name} 缺少 slug")
            continue
        if slug in topics:
            error(f"学习项 slug 重复: {slug}")
        topics[slug] = doc

        if doc.get("status") not in {"published", "draft"}:
            error(f"{slug} status 非法: {doc.get('status')!r}")
        category = doc.get("category")
        if category not in CATEGORIES:
            error(f"{slug} category 非法: {category!r}")
        if doc.get("difficulty") not in DIFFICULTIES:
            error(f"{slug} difficulty 非法: {doc.get('difficulty')!r}")
        if doc.get("integrationStatus") not in INTEGRATIONS:
            error(f"{slug} integrationStatus 非法: {doc.get('integrationStatus')!r}")

        if doc.get("status") == "published":
            if not (doc.get("overviewMd") or "").strip():
                error(f"{slug} 已发布但 overviewMd 为空")
            if not doc.get("pathSteps"):
                error(f"{slug} 已发布但 pathSteps 为空")
            if not doc.get("samples"):
                error(f"{slug} 已发布但 samples 为空")
            if len(doc.get("pitfalls") or []) < 3:
                error(f"{slug} 已发布但 pitfalls 少于 3 条")

        for field in ("concepts", "pathSteps", "samples", "pitfalls", "scenarios"):
            if not doc.get(field):
                warning(f"{slug} {field} 为空")

        for ref_field in ("prerequisites", "nextTopics", "relatedTopics"):
            for ref in doc.get(ref_field) or []:
                refs.append((slug, ref_field, ref))

    for slug, ref_field, ref in refs:
        if ref not in topics:
            warning(f"{slug} {ref_field} 引用了不存在的 slug: {ref}")
    return topics


def validate_quizzes(topics: dict[str, dict]) -> int:
    total = 0
    quiz_slugs: set[str] = set()
    for path in sorted(QUIZZES.glob("*.json")):
        slug = path.stem
        quiz_slugs.add(slug)
        if slug not in topics:
            error(f"随堂题 {path.name} 没有对应学习项")
        data = load_json(path)
        if not isinstance(data, list):
            error(f"{path.name} 顶层必须是数组")
            continue
        if not data:
            warning(f"{path.name} 题目为空")
        for index, item in enumerate(data):
            total += 1
            where = f"{path.name}[{index}]"
            if item.get("type") not in TYPES:
                error(f"{where} type 非法: {item.get('type')!r}")
                continue
            if not (item.get("stem") or "").strip():
                error(f"{where} 题干为空")
            if not (item.get("explanation") or "").strip():
                error(f"{where} explanation 为空")
            if item["type"] == "choice":
                options = item.get("options") or []
                if not 2 <= len(options) <= 6:
                    error(f"{where} 选项数量非法: {len(options)}")
                correct = item.get("correctIndex")
                if not isinstance(correct, int) or not 0 <= correct < len(options):
                    error(f"{where} correctIndex 非法: {correct!r}")
            elif not (item.get("referenceAnswer") or "").strip():
                error(f"{where} 自答题缺少参考答案")

    for slug in topics:
        if slug not in quiz_slugs:
            warning(f"学习项 {slug} 没有随堂题")
    return total


def validate_tracks(topics: dict[str, dict]) -> tuple[int, int]:
    tracks = 0
    stages = 0
    for path in sorted(TRACKS.glob("*.json")):
        doc = load_json(path)
        if not isinstance(doc, dict):
            continue
        tracks += 1
        if not doc.get("slug"):
            error(f"{path.name} 缺少 slug")
        if not doc.get("stages"):
            error(f"{path.name} 缺少 stages")
        for stage in doc.get("stages") or []:
            stages += 1
            if not stage.get("title"):
                error(f"{path.name} 存在无标题阶段")
            for slug in stage.get("topicSlugs") or []:
                if slug not in topics:
                    warning(f"{path.name} 阶段引用不存在的学习项: {slug}")
    return tracks, stages


def validate_interview() -> tuple[int, Counter, Counter, Counter]:
    data = load_json(INTERVIEW)
    if not isinstance(data, list):
        error("interview/questions.json 顶层必须是数组")
        return 0, Counter(), Counter(), Counter()

    seen: dict[str, int] = {}
    categories: Counter = Counter()
    levels: Counter = Counter()
    types: Counter = Counter()
    for index, item in enumerate(data):
        where = f"interview[{index}]"
        category = item.get("category")
        level = item.get("level")
        qtype = item.get("type")
        if not category:
            error(f"{where} 缺少 category")
        if level not in LEVELS:
            error(f"{where} level 非法: {level!r}")
        if qtype not in TYPES:
            error(f"{where} type 非法: {qtype!r}")
        stem = (item.get("stem") or "").strip()
        if not stem:
            error(f"{where} 题干为空")
        if not (item.get("explanation") or "").strip():
            error(f"{where} explanation 为空")
        key = "".join(stem.split()).lower()
        if key in seen:
            error(f"{where} 题干重复，与第 {seen[key]} 条相同")
        seen[key] = index
        if qtype == "choice":
            options = item.get("options") or []
            if not 2 <= len(options) <= 6:
                error(f"{where} 选项数量非法: {len(options)}")
            correct = item.get("correctIndex")
            if not isinstance(correct, int) or not 0 <= correct < len(options):
                error(f"{where} correctIndex 非法: {correct!r}")
        elif qtype == "text":
            if not (item.get("referenceAnswer") or "").strip():
                error(f"{where} 自答题缺少参考答案")
        categories[category] += 1
        levels[level] += 1
        types[qtype] += 1
    return len(data), categories, levels, types


def main() -> None:
    topics = validate_topics()
    quiz_total = validate_quizzes(topics)
    track_total, stage_total = validate_tracks(topics)
    interview_total, categories, levels, types = validate_interview()

    print(f"学习项: {len(topics)}")
    print(f"随堂题: {quiz_total}")
    print(f"路线: {track_total}，阶段: {stage_total}")
    print(f"面试题: {interview_total}")
    if interview_total:
        print("  分类:", dict(categories))
        print("  等级:", dict(levels))
        print("  题型:", dict(types))

    for message in warnings:
        print(f"[WARN] {message}")
    for message in errors:
        print(f"[ERROR] {message}")

    if errors:
        sys.exit(1)
    print("[OK] 内容校验通过")


if __name__ == "__main__":
    main()
