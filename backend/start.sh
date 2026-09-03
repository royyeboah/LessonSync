#!/usr/bin/env bash
set -euo pipefail

# Railway (and similar hosts) only accept secrets as environment variables. Vertex AI
# still expects a file path in GOOGLE_APPLICATION_CREDENTIALS, so materialise one.
if [[ -n "${GOOGLE_APPLICATION_CREDENTIALS_JSON:-}" ]]; then
  creds_file="${GOOGLE_APPLICATION_CREDENTIALS:-/tmp/gcp-application-credentials.json}"
  mkdir -p "$(dirname "$creds_file")"
  printf '%s' "$GOOGLE_APPLICATION_CREDENTIALS_JSON" > "$creds_file"
  export GOOGLE_APPLICATION_CREDENTIALS="$creds_file"
fi

jar="$(ls -1 target/LessonSync-*.jar 2>/dev/null | grep -v 'sources\|original' | head -n 1 || true)"
if [[ -z "$jar" ]]; then
  echo "No packaged Spring Boot jar found in target/. Did the Maven build run?" >&2
  exit 1
fi

exec java -jar "$jar"
