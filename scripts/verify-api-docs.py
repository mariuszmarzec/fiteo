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


def read_source(path: Path) -> str:
    if not path.is_file():
        ERRORS.append(f"Missing required runtime source: {path.relative_to(ROOT)}")
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
feature_toggles = read_doc("feature_toggles_get.md")
openapi_doc = read_doc("openapi.md")
fiteo_routes = read_source(ROOT / "src/jvmMain/kotlin/com/marzec/fiteo/FiteoApi.kt")
task_model = read_source(ROOT / "src/commonMain/kotlin/com/marzec/todo/model/model.kt")

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
require(
    "[Compile-time OpenAPI and Swagger UI](openapi.md)" in readme,
    "docs/api/README.md must retain its link to the OpenAPI generation guide.",
)
require(
    "[OpenAPI and Swagger UI endpoints](openapi.md#openapi-and-swagger-ui-endpoints)" in readme,
    "docs/api/README.md must retain its link to the OpenAPI and Swagger UI endpoints section.",
)
require(
    re.search(r"(?m)^## OpenAPI and Swagger UI endpoints\s*$", openapi_doc) is not None,
    "openapi.md must contain the 'OpenAPI and Swagger UI endpoints' section linked by README.",
)

# Every Markdown file linked by the API index must exist.
for link in README_LINK.findall(readme):
    require((DOCS / link).is_file(), f"docs/api/README.md links to missing file: {link}")

# Check the feature-toggle authentication claim against its actual routing scope.
auth_scope = re.search(
    r"authenticate\(di\.authToken\)\s*\{(?P<body>.*?)^[ \t]{4}\}",
    fiteo_routes,
    re.MULTILINE | re.DOTALL,
)
require(
    auth_scope is not None,
    "FiteoApi.kt must contain a parseable authenticate(di.authToken) route block.",
)
feature_auth_section = re.search(
    r"(?im)^## Authentication\s*\n(.*?)(?=^## |\Z)",
    feature_toggles,
    re.DOTALL,
)
require(
    feature_auth_section is not None,
    "feature_toggles_get.md must contain an Authentication section.",
)
if auth_scope is not None and feature_auth_section is not None:
    feature_route_is_authenticated = re.search(
        r"(?m)^[ \t]*featureToggles\(api\)\s*$",
        auth_scope.group("body"),
    ) is not None
    auth_text = feature_auth_section.group(1)
    if feature_route_is_authenticated:
        require(
            re.search(r"\brequired\b", auth_text, re.IGNORECASE) is not None
            and re.search(r"\bnot required\b", auth_text, re.IGNORECASE) is None,
            "feature_toggles_get.md must require authentication when featureToggles(api) is inside authenticate(di.authToken).",
        )
    else:
        require(
            re.search(r"\bnot required\b", auth_text, re.IGNORECASE) is not None,
            "feature_toggles_get.md must say 'Not required.' because featureToggles(api) is outside authenticate(di.authToken).",
        )
        require(
            re.search(r"authenticated session|authentication is required", feature_toggles, re.IGNORECASE) is None,
            "feature_toggles_get.md must not claim that this public route requires an authenticated session.",
        )

# Match the scheduler field's type and requiredness to the real CreateTaskDto declaration.
create_task_dto = re.search(
    r"data class CreateTaskDto\s*\((?P<body>.*?)^[ \t]*\)",
    task_model,
    re.MULTILINE | re.DOTALL,
)
require(create_task_dto is not None, "model.kt must contain the CreateTaskDto declaration.")
scheduler_property = None
if create_task_dto is not None:
    scheduler_property = re.search(
        r"^[ \t]*val scheduler:[ \t]*(SchedulerDto\??)(?:[ \t]*=[ \t]*([^,\n]+))?,?[ \t]*$",
        create_task_dto.group("body"),
        re.MULTILINE,
    )
require(
    scheduler_property is not None,
    "CreateTaskDto must declare a scheduler property whose type can be checked.",
)
scheduler_row = re.search(
    r"(?m)^\|\s*scheduler\s*\|\s*([^|]+?)\s*\|\s*(Yes|No)\s*\|",
    create,
)
require(
    scheduler_row is not None,
    "tasks_create.md must contain a scheduler row in the CreateTaskDto field table.",
)
if scheduler_property is not None and scheduler_row is not None:
    expected_scheduler_type = scheduler_property.group(1).strip()
    actual_scheduler_type = scheduler_row.group(1).strip()
    require(
        actual_scheduler_type == expected_scheduler_type,
        f"tasks_create.md documents scheduler as {actual_scheduler_type!r}, but CreateTaskDto declares {expected_scheduler_type!r}.",
    )
    has_default = scheduler_property.group(2) is not None
    expected_required = "No" if has_default else "Yes"
    actual_required = scheduler_row.group(2)
    require(
        actual_required == expected_required,
        f"tasks_create.md documents scheduler Required={actual_required}, but CreateTaskDto has_default={has_default} implies Required={expected_required}.",
    )

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
