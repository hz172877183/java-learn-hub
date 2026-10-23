#!/usr/bin/env python3
"""合并 content/interview/parts/*.json 为 content/interview/questions.json。

用法：
    python scripts/build_interview_bank.py
    python scripts/build_interview_bank.py --check
"""

from __future__ import annotations

import argparse
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PARTS = ROOT / "content" / "interview" / "parts"
TARGET = ROOT / "content" / "interview" / "questions.json"

CATEGORY_ORDER = [
    "java-basics",
    "collections",
    "concurrency",
    "jvm",
    "spring",
    "spring-boot",
    "spring-cloud",
    "mysql",
    "redis",
    "mq",
    "distributed",
    "architecture",
    "engineering",
    "docker-k8s",
    "network",
    "os-linux",
    "design-pattern",
    "algorithm",
    "frontend",
    "security",
    "testing",
    "ai",
    "project",
]
LEVEL_ORDER = ["junior", "mid", "senior", "architect"]
LEVELS = set(LEVEL_ORDER)
TYPES = {"choice", "text"}


def fail(message: str) -> None:
    raise SystemExit(f"[FAIL] {message}")


def normalize_stem(stem: str) -> str:
    return "".join(stem.split()).lower()


def load_part(path: Path) -> list[dict]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        fail(f"{path.name} JSON 解析失败: {exc}")
    if not isinstance(data, list):
        fail(f"{path.name} 顶层必须是数组")
    return data


def validate_question(item: dict, source: str, index: int) -> None:
    where = f"{source}[{index}]"
    if item.get("category") not in CATEGORY_ORDER:
        fail(f"{where} 未知 category: {item.get('category')!r}")
    if item.get("level") not in LEVELS:
        fail(f"{where} 非法 level: {item.get('level')!r}")
    if item.get("type") not in TYPES:
        fail(f"{where} 非法 type: {item.get('type')!r}")

    stem = (item.get("stem") or "").strip()
    explanation = (item.get("explanation") or "").strip()
    if not stem:
        fail(f"{where} 题干为空")
    if not explanation:
        fail(f"{where} 缺少 explanation")

    if item["type"] == "choice":
        options = item.get("options") or []
        if not 2 <= len(options) <= 6:
            fail(f"{where} 选择题需要 2-6 个选项，实际 {len(options)}")
        if any(not str(option).strip() for option in options):
            fail(f"{where} 存在空选项")
        correct = item.get("correctIndex")
        if not isinstance(correct, int) or not 0 <= correct < len(options):
            fail(f"{where} correctIndex 非法: {correct!r}")
        if item.get("referenceAnswer") not in (None, ""):
            fail(f"{where} 选择题不应有 referenceAnswer")
    else:
        if item.get("options"):
            fail(f"{where} 自答题 options 应为空")
        if item.get("correctIndex") is not None:
            fail(f"{where} 自答题 correctIndex 应为 null")
        if not (item.get("referenceAnswer") or "").strip():
            fail(f"{where} 自答题缺少参考答案")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="只校验，不写文件")
    args = parser.parse_args()

    # legacy 是旧题库备份，放在最后处理：与新题重复时以新题为准。
    part_files = sorted(
        (path for path in PARTS.glob("*.json")),
        key=lambda path: (path.name == "legacy.json", path.name),
    )
    if not part_files:
        fail(f"{PARTS} 下没有分片文件")

    merged: list[dict] = []
    seen: dict[str, str] = {}
    per_file: dict[str, int] = {}
    skipped: list[str] = []
    for path in part_files:
        questions = load_part(path)
        per_file[path.name] = len(questions)
        for index, item in enumerate(questions):
            validate_question(item, path.name, index)
            key = normalize_stem(item["stem"])
            if key in seen:
                if path.name == "legacy.json":
                    skipped.append(f"{path.name}[{index}] 与 {seen[key]} 重复，已跳过")
                    continue
                fail(f"题干重复: {path.name}[{index}] 与 {seen[key]} -> {item['stem'][:60]}")
            seen[key] = f"{path.name}[{index}]"
            merged.append({
                "category": item["category"],
                "level": item["level"],
                "type": item["type"],
                "stem": item["stem"].strip(),
                "options": [str(option).strip() for option in (item.get("options") or [])],
                "correctIndex": item.get("correctIndex"),
                "referenceAnswer": (item.get("referenceAnswer") or None),
                "explanation": item["explanation"].strip(),
            })

    merged.sort(key=lambda item: (
        CATEGORY_ORDER.index(item["category"]),
        LEVEL_ORDER.index(item["level"]),
    ))

    categories = Counter(item["category"] for item in merged)
    levels = Counter(item["level"] for item in merged)
    types = Counter(item["type"] for item in merged)

    print(f"分片: {len(part_files)} 个，题目: {len(merged)} 道")
    for name, count in per_file.items():
        print(f"  {name}: {count}")
    print("分类分布:")
    for category in CATEGORY_ORDER:
        if categories.get(category):
            print(f"  {category}: {categories[category]}")
    print("等级分布:", dict(levels))
    print("题型分布:", dict(types))
    for message in skipped:
        print(f"[SKIP] {message}")

    if args.check:
        print("仅校验，未写入 questions.json")
        return

    TARGET.write_text(
        json.dumps(merged, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print(f"已写入 {TARGET}")


if __name__ == "__main__":
    main()
