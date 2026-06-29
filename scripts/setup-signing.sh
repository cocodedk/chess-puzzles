#!/usr/bin/env bash
# Upload the four release-signing secrets to GitHub so the "Release APK" workflow can build
# signed APKs. Run locally with gh authenticated as the repo owner:
#     bash scripts/setup-signing.sh
#
# The keystore and passwords are read from ~/.chess-release (override with KEYSTORE_DIR).
# Nothing secret is committed to the repo — this only creates encrypted GitHub Actions
# secrets (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD). Keep the keystore
# itself backed up somewhere durable; it is the only key that can sign updates.
set -euo pipefail

repo="${REPO:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"
dir="${KEYSTORE_DIR:-$HOME/.chess-release}"
keystore="$dir/release.keystore"
creds="$dir/credentials.txt"

[ -f "$keystore" ] || { echo "No keystore at $keystore — generate or restore it first." >&2; exit 1; }
# shellcheck disable=SC1090
[ -f "$creds" ] && . "$creds"
: "${KEY_ALIAS:?KEY_ALIAS not set (add it to $creds or export it)}"
: "${KEYSTORE_PASSWORD:?KEYSTORE_PASSWORD not set}"
: "${KEY_PASSWORD:?KEY_PASSWORD not set}"

echo "Uploading signing secrets to $repo ..."
base64 < "$keystore" | tr -d '\n' | gh secret set KEYSTORE_BASE64 --repo "$repo"
printf '%s' "$KEYSTORE_PASSWORD" | gh secret set KEYSTORE_PASSWORD --repo "$repo"
printf '%s' "$KEY_ALIAS"         | gh secret set KEY_ALIAS         --repo "$repo"
printf '%s' "$KEY_PASSWORD"      | gh secret set KEY_PASSWORD      --repo "$repo"
echo "Done — KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD set on $repo."
gh secret list --repo "$repo"
