#!/usr/bin/env python3
"""Validate critical Fiteo runtime-contract facts in generated API documentation.

This guard is intentionally deterministic: the documentation generator is LLM-based,
so prompt instructions alone are not sufficient to protect implementation-specific
behavior from being removed or rewritten incorrectly.
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
# Find strings that look like ISO local date-times, then require the exact format
# accepted by the backend. This rejects Z, offsets, fractional seconds and omissions.
DATETIME_VALUE = re.compile(r"""["'](\d{4}-\d{2}-\d{2}T[^"']*)["']""")
VALID_DATETIME = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}")
JSON_BLOCK = re.compile(r"\x60\x60\x60json[^\n]*\n(.*?)\x60\x60\x60", re.IGNORECASE | re.DOTALL)


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

# Keep the durable behavioral contract present and linked from the API index.
for expected in (
    "OneShot",
    "Weekly",
    "Monthly",
    "yyyy-MM-dd'T'HH:mm:ss",
    "Authorization",
    "60 seconds",
):
    require(expected in runtime, f"runtime-behavior.md must document {expected!r}.")
require(
    "[Runtime behavior not fully represented by OpenAPI](runtime-behavior.md)" in readme,
    "docs/api/README.md must link to runtime-behavior.md.",
)

# The endpoint docs must preserve the exact scheduler values and provide a OneShot JSON example.
create_json_blocks = JSON_BLOCK.findall(create)
require(
    any(SCHEDULER_TYPE_FIELD.search(block) and
        SCHEDULER_TYPE_FIELD.search(block).group(1) == "OneShot"
        for block in create_json_blocks),
    "tasks_create.md must include a JSON example with the exact scheduler type OneShot.",
)
for scheduler_type in ALLOWED_SCHEDULER_TYPES:
    require(scheduler_type in create, f"tasks_create.md must document scheduler type {scheduler_type!r}.")

for filename, content in (
    ("tasks_create.md", create),
    ("tasks_get.md", get_tasks),
    ("tasks_update.md", update),
    ("tasks_delete.md", delete),
):
    require(
        "yyyy-MM-dd'T'HH:mm:ss" in content,
        f"{filename} must document the accepted local date-time format.",
    )
    # Validate JSON examples only. Prose may explain rejected suffixes like Z.
    for block in JSON_BLOCK.findall(content):
        for value in DATETIME_VALUE.findall(block):
            if VALID_DATETIME.fullmatch(value) is None:
                ERRORS.append(
                    f"{filename} contains a date-time that does not match yyyy-MM-dd'T'HH:mm:ss: {value!r}"
                )
        for match in SCHEDULER_TYPE_FIELD.finditer(block):
            value = match.group(1)
            if value not in ALLOWED_SCHEDULER_TYPES:
                ERRORS.append(
                    f"{filename} contains unsupported/case-mismatched scheduler type {value!r} in a JSON example."
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

print("API documentation verification passed: runtime scheduler, date-time, and auth invariants are present.")
