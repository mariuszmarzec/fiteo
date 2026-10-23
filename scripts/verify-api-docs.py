#!/usr/bin/env python3
"""Validate generated Fiteo API documentation against critical runtime invariants.

The documentation generator is LLM-based. Prompt instructions are not sufficient:
this deterministic check validates every JSON example before the sync script can
report success.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs" / "api"
ERRORS: list[str] = []

ALLOWED_SCHEDULER_TYPES = {"OneShot", "Weekly", "Monthly"}
SCHEDULER_TYPE_FIELD = re.compile(r"""["']type["']\s*:\s*["']([^"']+)["']""")
# Any JSON string that looks like a date-time must use the exact LocalDateTime pattern.
DATETIME_VALUE = re.compile(r"""["'](\d{4}-\d{2}-\d{2}T[^"']*)["']""")
VALID_DATETIME = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}")
JSON_BLOCK = re.compile(r"\x60\x60\x60json[^\n]*\n(.*?)\x60\x60\x60", re.IGNORECASE | re.DOTALL)
README_LINK = re.compile(r"\]\(([^)#]+\.md)(?:#[^)]+)?\)")


def read_doc(name: str) -> str:
    path = DOCS / name
    if not path.is_file():
        ERRORS.append(f"Missing required documentation file: docs/api/{name}")
        return ""
    return path.read_text(encoding="utf-8")


def require(condition: bool, message: str) -> None:
    if not condition:
        ERRORS.append(message)


runtime = read_doc("runtime-behavior.md")
create = read_doc("tasks_create.md")
get_tasks = read_doc("tasks_get.md")
update = read_doc("tasks_update.md")
delete = read_doc("tasks_delete.md")
login = read_doc("login.md")
login_bearer = read_doc("login_bearer.md")
readme = read_doc("README.md")

# Keep the durable runtime contract present and linked from the API index.
for expected in (
    "OneShot",
    "Weekly",
    "Monthly",
    "yyyy-MM-dd'T'HH:mm:ss",
    "all endpoints",
    "Authorization",
    "60 seconds",
):
    require(expected.lower() in runtime.lower(), f"runtime-behavior.md must document {expected!r}.")
require(
    "[Runtime behavior not fully represented by OpenAPI](runtime-behavior.md)" in readme,
    "docs/api/README.md must link to runtime-behavior.md.",
)

# Every Markdown file linked by the API index must exist.
for link in README_LINK.findall(readme):
    require((DOCS / link).is_file(), f"docs/api/README.md links to missing file: {link}")

# The task creation page must show the exact OneShot API value.
create_json_blocks = JSON_BLOCK.findall(create)
require(
    any(
        any(m.group(1) == "OneShot" for m in SCHEDULER_TYPE_FIELD.finditer(block))
        for block in create_json_blocks
    ),
    "tasks_create.md must include a JSON example with the exact scheduler type OneShot.",
)
for scheduler_type in ALLOWED_SCHEDULER_TYPES:
    require(scheduler_type in create, f"tasks_create.md must document scheduler type {scheduler_type!r}.")

# Validate every JSON example in every API Markdown page. The backend shares
# Api.DATE_FORMAT across task, training, weight, FCM and other LocalDateTime fields.
date_values_checked = 0
for path in sorted(DOCS.glob("*.md")):
    content = path.read_text(encoding="utf-8")
    for block in JSON_BLOCK.findall(content):
        for match in DATETIME_VALUE.finditer(block):
            value = match.group(1)
            date_values_checked += 1
            if VALID_DATETIME.fullmatch(value) is None:
                ERRORS.append(
                    f"{path.relative_to(ROOT)} contains a date-time that does not match "
                    f"yyyy-MM-dd'T'HH:mm:ss in a JSON example: {value!r}"
                )

# Scheduler strings are specifically a TaskDto/SchedulerDto contract; don't
# misinterpret unrelated 'type' fields in other API models as scheduler types.
for filename, content in (
    ("tasks_create.md", create),
    ("tasks_get.md", get_tasks),
    ("tasks_update.md", update),
    ("tasks_delete.md", delete),
):
    for block in JSON_BLOCK.findall(content):
        for match in SCHEDULER_TYPE_FIELD.finditer(block):
            value = match.group(1)
            if value not in ALLOWED_SCHEDULER_TYPES:
                ERRORS.append(
                    f"docs/api/{filename} contains unsupported/case-mismatched scheduler type "
                    f"{value!r} in a JSON example."
                )

# Login flows must explain where the auth value is and how it is reused.
for filename, content in (("login.md", login), ("login_bearer.md", login_bearer)):
    require("Authorization" in content, f"{filename} must explain the Authorization response header.")
    require(
        re.search(r"unchanged|verbatim", content, re.IGNORECASE) is not None,
        f"{filename} must say the returned Authorization value is reused unchanged.",
    )
require("Bearer <token>" in login_bearer, "login_bearer.md must document the full 'Bearer <token>' header value.")
require("60 seconds" in login_bearer, "login_bearer.md must document the current 60-second token expiry.")

if ERRORS:
    print("API documentation verification FAILED:", file=sys.stderr)
    for error in ERRORS:
        print(f"  - {error}", file=sys.stderr)
    sys.exit(1)

print(
    "API documentation verification passed: "
    f"{date_values_checked} date-time strings checked across all API JSON examples; "
    "scheduler, authentication, and README link invariants are present."
)
