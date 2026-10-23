#!/usr/bin/env python3
"""把选择题的正确选项均匀分布到 A/B/C/D，避免答案总在同一个位置。

只调整选项顺序，不改变题目与答案内容；含“以上/都/全部”等整体性选项的题跳过。
用法：
    python scripts/balance_options.py
    python scripts/balance_options.py --write
"""

from __future__ import annotations

import argparse
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PARTS = ROOT / "content" / "interview" / "parts"

SKIP_MARKERS = ("以上", "都对", "都不对", "全部正确", "全部错误", "均是", "均不是")


def balance(questions: list[dict]) -> tuple[list[dict], Counter, Counter]:
    before: Counter = Counter()
    after: Counter = Counter()
    slot = 0
    for item in questions:
        if item.get("type") != "choice":
            continue
        options = item.get("options") or []
        correct = item.get("correctIndex")
        if not isinstance(correct, int) or not 0 <= correct < len(options):
            continue
        before[correct] += 1
        if any(marker in option for option in options for marker in SKIP_MARKERS):
            after[correct] += 1
            continue

        target = slot % 4
        slot += 1
        if target != correct:
            moved = options.pop(correct)
            options.insert(target, moved)
            item["correctIndex"] = target
        after[item["correctIndex"]] += 1
    return questions, before, after


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="写回分片文件")
    parser.add_argument("files", nargs="*", help="只处理指定分片，如 mysql network")
    args = parser.parse_args()

    if args.files:
        paths = [PARTS / (name if name.endswith(".json") else f"{name}.json") for name in args.files]
    else:
        paths = sorted(PARTS.glob("*.json"))

    for path in paths:
        if path.name == "legacy.json":
            continue
        data = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(data, list):
            continue
        data, before, after = balance(data)
        print(f"{path.name}: {dict(sorted(before.items()))} -> {dict(sorted(after.items()))}")
        if args.write:
            path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if not args.write:
        print("未写入；加 --write 生效")


if __name__ == "__main__":
    main()
