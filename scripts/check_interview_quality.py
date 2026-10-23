#!/usr/bin/env python3
"""面试题库质量检查：近似重复、答案位置分布、长度异常。

用法：
    python scripts/check_interview_quality.py
"""

from __future__ import annotations

import difflib
import json
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
BANK = ROOT / "content" / "interview" / "questions.json"

SIMILARITY = 0.85


def main() -> None:
    questions = json.loads(BANK.read_text(encoding="utf-8"))
    print(f"题目总数: {len(questions)}")

    by_category: dict[str, list[dict]] = defaultdict(list)
    for item in questions:
        by_category[item["category"]].append(item)

    positions = Counter()
    short_explanations = 0
    for item in questions:
        if item["type"] == "choice":
            positions[item["correctIndex"]] += 1
        if len(item.get("explanation", "")) < 20:
            short_explanations += 1

    print("选择题答案位置:", dict(sorted(positions.items())))
    print(f"解析少于 20 字: {short_explanations}")

    duplicates = 0
    for category, items in by_category.items():
        for i in range(len(items)):
            for j in range(i + 1, len(items)):
                left = items[i]["stem"]
                right = items[j]["stem"]
                if abs(len(left) - len(right)) > 30:
                    continue
                ratio = difflib.SequenceMatcher(None, left, right).ratio()
                if ratio >= SIMILARITY:
                    duplicates += 1
                    print(f"[疑似重复 {ratio:.2f}] {category}")
                    print(f"  - {left}")
                    print(f"  - {right}")

    print(f"疑似重复对: {duplicates}")


if __name__ == "__main__":
    main()
