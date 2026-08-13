#!/bin/bash
#
# WeatherPossum release script
#
# Bumps the version, collects changelog notes, builds a signed release APK,
# verifies it, then tags and publishes a GitHub release with APK + SHA-256.
#
# Signing credentials live in keystore.properties (gitignored):
#   storeFile=/absolute/path/to/WeatherPossum.jks
#   storePassword=...
#   keyAlias=...
#   keyPassword=...
#
# Usage: ./create_release.sh [--patch|--minor|--major|--version X.Y.Z]
#                            [--notes-file FILE] [--yes] [--dry-run] [--help]

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$REPO_ROOT"

REPO_OWNER="IzanahTech"
REPO_NAME="WeatherPossum"
GRADLE_APK="app/build/outputs/apk/release/app-release.apk"
CHANGELOG_FILE="CHANGELOG.md"
GRADLE_FILE="app/build.gradle.kts"
KEYSTORE_PROPERTIES="keystore.properties"
DEFAULT_KEYSTORE="${HOME}/Documents/Weather/WeatherPossum.jks"
SEMVER_REGEX='^[0-9]+\.[0-9]+\.[0-9]+$'
# SHA-256 of the WeatherPossum release signing certificate (apksigner --print-certs).
EXPECTED_CERT_SHA256="27edbf67021d2a3026213b2b98a8f99fd9cbc500c1661e3dda63a463dfa4b378"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

BUMP_KIND=""
CUSTOM_VERSION=""
NOTES_FILE=""
ASSUME_YES=0
DRY_RUN=0

die() {
    echo -e "${RED}❌ $*${NC}" >&2
    exit 1
}

info() {
    echo -e "${GREEN}$*${NC}"
}

warn() {
    echo -e "${YELLOW}$*${NC}"
}

usage() {
    cat <<'EOF'
Usage: ./create_release.sh [options]

  --patch            Bump patch version (default: 1.8.6 -> 1.8.7)
  --minor            Bump minor version (1.8.6 -> 1.9.0)
  --major            Bump major version (1.8.6 -> 2.0.0)
  --version X.Y.Z    Set a strict SemVer version greater than the current one
  --notes-file FILE  Read release notes from FILE instead of prompting
  --yes              Skip the final confirmation prompt
  --dry-run          Show what would happen without changing anything
  -h, --help         Show this help

The script will:
  1. Auto-bump versionName + versionCode in app/build.gradle.kts
  2. Ask for release notes / changelog
  3. Update CHANGELOG.md
  4. Build a signed release APK with Gradle
  5. Verify APK version and signature
  6. Generate a SHA-256 checksum
  7. Commit, create git tag vX.Y.Z, push, and publish a GitHub release
EOF
}

while [ $# -gt 0 ]; do
    case "$1" in
        --patch|--minor|--major)
            BUMP_KIND="${1#--}"
            shift
            ;;
        --version)
            [ $# -ge 2 ] || die "--version requires a value like 1.8.7"
            CUSTOM_VERSION="$2"
            BUMP_KIND="custom"
            shift 2
            ;;
        --notes-file)
            [ $# -ge 2 ] || die "--notes-file requires a path"
            NOTES_FILE="$2"
            shift 2
            ;;
        --yes|-y)
            ASSUME_YES=1
            shift
            ;;
        --dry-run)
            DRY_RUN=1
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            die "Unknown option: $1 (see --help)"
            ;;
    esac
done

require_semver() {
    local value="$1"
    local label="${2:-Version}"
    echo "$value" | grep -Eq "$SEMVER_REGEX" || die "${label} '$value' is not strict SemVer (X.Y.Z)"
}

# Returns 0 if $1 > $2 using numeric major.minor.patch comparison.
semver_is_newer() {
    local left="$1"
    local right="$2"
    local l1 l2 l3 r1 r2 r3
    IFS='.' read -r l1 l2 l3 <<EOF
$left
EOF
    IFS='.' read -r r1 r2 r3 <<EOF
$right
EOF
    [ "$l1" -gt "$r1" ] && return 0
    [ "$l1" -lt "$r1" ] && return 1
    [ "$l2" -gt "$r2" ] && return 0
    [ "$l2" -lt "$r2" ] && return 1
    [ "$l3" -gt "$r3" ]
}

normalize_cert_sha256() {
    echo "$1" | tr '[:upper:]' '[:lower:]' | tr -d '[:space:]:'
}

read_gradle_version() {
    VERSION_NAME="$(sed -nE 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"([^"]+)".*/\1/p' "$GRADLE_FILE" | head -1)"
    VERSION_CODE="$(sed -nE 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*([0-9]+).*/\1/p' "$GRADLE_FILE" | head -1)"
    [ -n "$VERSION_NAME" ] && [ -n "$VERSION_CODE" ] || die "Could not read versionName/versionCode from $GRADLE_FILE"
    require_semver "$VERSION_NAME" "Current versionName"
}

bump_semver() {
    local current="$1"
    local kind="$2"
    local major minor patch
    major="$(echo "$current" | cut -d. -f1)"
    minor="$(echo "$current" | cut -d. -f2)"
    patch="$(echo "$current" | cut -d. -f3)"
    major="${major:-0}"
    minor="${minor:-0}"
    patch="${patch:-0}"
    case "$kind" in
        major) echo "$((major + 1)).0.0" ;;
        minor) echo "${major}.$((minor + 1)).0" ;;
        patch|*) echo "${major}.${minor}.$((patch + 1))" ;;
    esac
}

prompt_bump_kind() {
    if [ -n "$BUMP_KIND" ]; then
        return
    fi
    if [ ! -t 0 ] || [ "$ASSUME_YES" -eq 1 ]; then
        BUMP_KIND="patch"
        return
    fi
    echo
    warn "📈 Current version: ${VERSION_NAME} (versionCode ${VERSION_CODE})"
    echo "  1) patch  -> $(bump_semver "$VERSION_NAME" patch) ($((VERSION_CODE + 1)))  [default]"
    echo "  2) minor  -> $(bump_semver "$VERSION_NAME" minor) ($((VERSION_CODE + 1)))"
    echo "  3) major  -> $(bump_semver "$VERSION_NAME" major) ($((VERSION_CODE + 1)))"
    echo "  4) custom"
    printf "Select bump type [patch]: "
    local choice
    read -r choice || true
    case "$choice" in
        ""|1|patch) BUMP_KIND="patch" ;;
        2|minor) BUMP_KIND="minor" ;;
        3|major) BUMP_KIND="major" ;;
        4|custom)
            BUMP_KIND="custom"
            printf "Enter version name (e.g. 1.9.0): "
            read -r CUSTOM_VERSION
            [ -n "$CUSTOM_VERSION" ] || die "Version name cannot be empty"
            require_semver "$CUSTOM_VERSION" "Custom version"
            ;;
        *) die "Invalid bump type: $choice" ;;
    esac
}

compute_new_version() {
    if [ "$BUMP_KIND" = "custom" ]; then
        require_semver "$CUSTOM_VERSION" "Custom version"
        NEW_VERSION_NAME="$CUSTOM_VERSION"
    else
        NEW_VERSION_NAME="$(bump_semver "$VERSION_NAME" "$BUMP_KIND")"
    fi
    require_semver "$NEW_VERSION_NAME" "New version"
    if ! semver_is_newer "$NEW_VERSION_NAME" "$VERSION_NAME"; then
        die "New version $NEW_VERSION_NAME must be greater than current version $VERSION_NAME"
    fi
    NEW_VERSION_CODE="$((VERSION_CODE + 1))"
    TAG="v${NEW_VERSION_NAME}"
}

load_android_sdk() {
    if [ -z "${ANDROID_HOME:-}" ] && [ -z "${ANDROID_SDK_ROOT:-}" ] && [ -f local.properties ]; then
        ANDROID_HOME="$(sed -n 's/^sdk.dir=//p' local.properties | tail -1 | tr -d '\r')"
        ANDROID_HOME="${ANDROID_HOME//\\:/:}"
    fi
    if [ -z "${ANDROID_HOME:-}" ] && [ -n "${ANDROID_SDK_ROOT:-}" ]; then
        ANDROID_HOME="$ANDROID_SDK_ROOT"
    fi
    export ANDROID_HOME="${ANDROID_HOME:-}"
}

find_build_tool() {
    local name="$1"
    if command -v "$name" >/dev/null 2>&1; then
        command -v "$name"
        return
    fi
    [ -n "${ANDROID_HOME:-}" ] || return 0
    local dir=""
    dir="$(ls -1d "${ANDROID_HOME}/build-tools"/*/ 2>/dev/null | sort -V | tail -1 || true)"
    if [ -n "$dir" ] && [ -x "${dir}${name}" ]; then
        echo "${dir}${name}"
    fi
}

ensure_keystore_properties() {
    if [ -f "$KEYSTORE_PROPERTIES" ]; then
        grep -q '^storeFile=' "$KEYSTORE_PROPERTIES" || die "$KEYSTORE_PROPERTIES is missing storeFile"
        grep -q '^storePassword=' "$KEYSTORE_PROPERTIES" || die "$KEYSTORE_PROPERTIES is missing storePassword"
        grep -q '^keyAlias=' "$KEYSTORE_PROPERTIES" || die "$KEYSTORE_PROPERTIES is missing keyAlias"
        grep -q '^keyPassword=' "$KEYSTORE_PROPERTIES" || die "$KEYSTORE_PROPERTIES is missing keyPassword"
        return
    fi

    [ -t 0 ] || die "Missing $KEYSTORE_PROPERTIES. Create it or run this script interactively."

    warn "🔐 No $KEYSTORE_PROPERTIES found. Let's set up release signing."
    local store_file store_password key_alias key_password
    store_file="$DEFAULT_KEYSTORE"
    if [ ! -f "$store_file" ]; then
        printf "Keystore path: "
        read -r store_file
    else
        printf "Keystore path [%s]: " "$store_file"
        local entered
        read -r entered
        [ -n "$entered" ] && store_file="$entered"
    fi
    [ -f "$store_file" ] || die "Keystore not found: $store_file"

    printf "Keystore password: "
    read -rs store_password
    echo
    [ -n "$store_password" ] || die "Keystore password cannot be empty"

    key_alias="$(keytool -list -v -keystore "$store_file" -storepass "$store_password" 2>/dev/null \
        | awk -F': ' '/^[[:space:]]*Alias name:/{print $2; exit}')"
    if [ -z "$key_alias" ]; then
        printf "Key alias: "
        read -r key_alias
    else
        printf "Key alias [%s]: " "$key_alias"
        local entered_alias
        read -r entered_alias
        [ -n "$entered_alias" ] && key_alias="$entered_alias"
    fi
    [ -n "$key_alias" ] || die "Key alias cannot be empty"

    printf "Key password (Enter if same as keystore): "
    read -rs key_password
    echo
    [ -z "$key_password" ] && key_password="$store_password"

    umask 077
    cat > "$KEYSTORE_PROPERTIES" <<EOF
storeFile=$store_file
storePassword=$store_password
keyAlias=$key_alias
keyPassword=$key_password
EOF
    info "✅ Wrote $KEYSTORE_PROPERTIES (gitignored)"
}

collect_release_notes() {
    if [ -n "$NOTES_FILE" ]; then
        [ -f "$NOTES_FILE" ] || die "Notes file not found: $NOTES_FILE"
        RELEASE_NOTES="$(tr -d '\r' < "$NOTES_FILE")"
        while [ "${RELEASE_NOTES# }" != "$RELEASE_NOTES" ]; do RELEASE_NOTES="${RELEASE_NOTES# }"; done
        [ -n "$(printf '%s' "$RELEASE_NOTES" | tr -d '[:space:]')" ] || die "Release notes file is empty"
        return
    fi

    [ -t 0 ] || die "No TTY. Pass --notes-file FILE to provide changelog notes."

    echo
    warn "📝 Release notes for ${NEW_VERSION_NAME}"
    echo "Enter changelog text. Blank lines are allowed."
    echo "Finish with a single '.' on its own line, or Ctrl-D."
    echo "----------------------------------------"
    RELEASE_NOTES=""
    local line
    while IFS= read -r line || [ -n "$line" ]; do
        if [ "$line" = "." ]; then
            break
        fi
        if [ -z "$RELEASE_NOTES" ]; then
            RELEASE_NOTES="$line"
        else
            RELEASE_NOTES="${RELEASE_NOTES}
${line}"
        fi
    done
    echo "----------------------------------------"
    [ -n "$(printf '%s' "$RELEASE_NOTES" | tr -d '[:space:]')" ] || die "Release notes cannot be empty"
}

confirm_plan() {
    echo
    info "🚀 WeatherPossum release plan"
    echo "=================================="
    echo "  Version:  ${VERSION_NAME} (${VERSION_CODE}) -> ${NEW_VERSION_NAME} (${NEW_VERSION_CODE})"
    echo "  Tag:      ${TAG}"
    echo "  APK:      ${GRADLE_APK}"
    echo "  Notes:"
    printf '%s\n' "$RELEASE_NOTES" | sed 's/^/    /'
    echo
    if [ "$DRY_RUN" -eq 1 ]; then
        warn "Dry run — no files, builds, tags, or releases will be created."
        exit 0
    fi
    if [ "$ASSUME_YES" -eq 1 ]; then
        return
    fi
    [ -t 0 ] || die "Refusing to publish without --yes when stdin is not a TTY"
    printf "Proceed with release? [y/N] "
    local answer
    read -r answer
    case "$answer" in
        y|Y|yes|YES) ;;
        *) die "Aborted" ;;
    esac
}

assert_clean_worktree() {
    local status
    status="$(git status --porcelain)"
    if [ -n "$status" ]; then
        echo "$status" >&2
        die "Working tree is dirty. Commit or stash all changes before running the release script."
    fi
}

load_java_home() {
    if [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
        export JAVA_HOME
        export PATH="${JAVA_HOME}/bin:${PATH}"
        return
    fi
    if command -v /usr/libexec/java_home >/dev/null 2>&1; then
        JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    fi
    if [ -z "${JAVA_HOME:-}" ] && [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]; then
        JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    fi
    [ -n "${JAVA_HOME:-}" ] && [ -x "${JAVA_HOME}/bin/java" ] || die "Java is required. Set JAVA_HOME to a JDK."
    export JAVA_HOME
    export PATH="${JAVA_HOME}/bin:${PATH}"
}

preflight() {
    [ -f "$GRADLE_FILE" ] || die "Run this script from the WeatherPossum repo root"
    [ -x "./gradlew" ] || die "./gradlew is missing"
    command -v git >/dev/null 2>&1 || die "git is required"
    git rev-parse --is-inside-work-tree >/dev/null 2>&1 || die "Not a git repository"
    assert_clean_worktree

    command -v gh >/dev/null 2>&1 || die "GitHub CLI (gh) is required"
    load_java_home
    command -v keytool >/dev/null 2>&1 || die "keytool is required (Java JDK)"

    gh auth status >/dev/null 2>&1 || die "GitHub CLI is not authenticated. Run: gh auth login"

    load_android_sdk
    AAPT_CMD="$(find_build_tool aapt || true)"
    APKSIGNER_CMD="$(find_build_tool apksigner || true)"
    [ -n "$AAPT_CMD" ] || die "aapt not found. Set ANDROID_HOME or install Android SDK build-tools."
    [ -n "$APKSIGNER_CMD" ] || die "apksigner not found. Set ANDROID_HOME or install Android SDK build-tools."
}

assert_tag_available() {
    if git rev-parse "$TAG" >/dev/null 2>&1; then
        die "Git tag $TAG already exists locally"
    fi
    if git ls-remote --exit-code --tags origin "refs/tags/${TAG}" >/dev/null 2>&1; then
        die "Git tag $TAG already exists on origin"
    fi
    if gh release view "$TAG" --repo "${REPO_OWNER}/${REPO_NAME}" >/dev/null 2>&1; then
        die "GitHub release $TAG already exists"
    fi
}

update_gradle_version() {
    local tmp
    tmp="$(mktemp)"
    awk -v name="$NEW_VERSION_NAME" -v code="$NEW_VERSION_CODE" '
        BEGIN { replaced_code=0; replaced_name=0 }
        !replaced_code && $0 ~ /^[[:space:]]*versionCode[[:space:]]*=/ {
            sub(/versionCode[[:space:]]*=[[:space:]]*[0-9]+/, "versionCode = " code)
            replaced_code=1
        }
        !replaced_name && $0 ~ /^[[:space:]]*versionName[[:space:]]*=/ {
            sub(/versionName[[:space:]]*=[[:space:]]*"[^"]+"/, "versionName = \"" name "\"")
            replaced_name=1
        }
        { print }
        END {
            if (!replaced_code || !replaced_name) exit 1
        }
    ' "$GRADLE_FILE" > "$tmp"
    mv "$tmp" "$GRADLE_FILE"
}

update_changelog() {
    local tmp body
    tmp="$(mktemp)"
    if [ -f "$CHANGELOG_FILE" ]; then
        body="$(awk '
            BEGIN { skip=1 }
            /^# Changelog/ { next }
            skip && /^[[:space:]]*$/ { next }
            { skip=0; print }
        ' "$CHANGELOG_FILE")"
    else
        body=""
    fi
    {
        printf '# Changelog\n\n'
        printf '## Version %s\n\n' "$NEW_VERSION_NAME"
        printf '%s\n\n' "$RELEASE_NOTES"
        printf '---\n'
        if [ -n "$body" ]; then
            printf '\n%s\n' "$body"
        fi
    } > "$tmp"
    mv "$tmp" "$CHANGELOG_FILE"
}

build_signed_apk() {
    warn "🔨 Building signed release APK..."
    ./gradlew assembleRelease --quiet
    [ -f "$GRADLE_APK" ] || die "Gradle did not produce $GRADLE_APK"
}

verify_apk() {
    warn "🔎 Verifying APK version and signature..."
    local badging apk_name apk_code
    badging="$("$AAPT_CMD" dump badging "$GRADLE_APK")"
    apk_name="$(printf '%s\n' "$badging" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p" | head -1)"
    apk_code="$(printf '%s\n' "$badging" | sed -n "s/.*versionCode='\([^']*\)'.*/\1/p" | head -1)"

    [ "$apk_name" = "$NEW_VERSION_NAME" ] || die "APK versionName is '$apk_name', expected '$NEW_VERSION_NAME'"
    [ "$apk_code" = "$NEW_VERSION_CODE" ] || die "APK versionCode is '$apk_code', expected '$NEW_VERSION_CODE'"

    local verify_out apk_cert expected_cert
    verify_out="$("$APKSIGNER_CMD" verify --verbose --print-certs "$GRADLE_APK" 2>&1)" || die "APK signature verification failed"
    printf '%s\n' "$verify_out" | grep -Eq 'Verified using v[23](\.[0-9]+)? scheme.*: true' \
        || die "APK is not signed with APK Signature Scheme v2 or v3"

    apk_cert="$(printf '%s\n' "$verify_out" | awk -F': ' '/certificate SHA-256 digest:/{print $2; exit}')"
    [ -n "$apk_cert" ] || die "Could not read the APK certificate SHA-256 digest"
    apk_cert="$(normalize_cert_sha256 "$apk_cert")"
    expected_cert="$(normalize_cert_sha256 "$EXPECTED_CERT_SHA256")"
    if [ "$apk_cert" != "$expected_cert" ]; then
        die "APK is not signed with the WeatherPossum release certificate (got $apk_cert, expected $expected_cert)"
    fi
    printf '%s\n' "$verify_out" | sed -n 's/^/  /p'

    info "✅ APK version ${apk_name} (${apk_code}) verified with WeatherPossum signature"
}

generate_checksum() {
    warn "🔐 Generating SHA-256 checksum..."
    local apk_dir apk_file
    apk_dir="$(dirname "$GRADLE_APK")"
    apk_file="$(basename "$GRADLE_APK")"
    CHECKSUM_FILE="${apk_dir}/${apk_file}.sha256"
    (
        cd "$apk_dir"
        if command -v sha256sum >/dev/null 2>&1; then
            sha256sum "$apk_file"
        else
            shasum -a 256 "$apk_file"
        fi
    ) > "$CHECKSUM_FILE"
    CHECKSUM="$(awk '{print $1}' "$CHECKSUM_FILE")"
    echo "  ${CHECKSUM}  ${apk_file}"
}

commit_tag_and_release() {
    warn "📦 Committing version bump and changelog..."
    git add -- "$GRADLE_FILE" "$CHANGELOG_FILE"
    if git diff --cached --quiet; then
        die "Nothing to commit — version files were not changed"
    fi
    git commit -m "$(cat <<EOF
Release ${NEW_VERSION_NAME}

EOF
)"

    warn "🏷️  Creating git tag ${TAG}..."
    git tag -a "$TAG" -m "WeatherPossum ${NEW_VERSION_NAME}"

    warn "⬆️  Pushing commit and tag..."
    git push origin HEAD
    git push origin "refs/tags/${TAG}"

    local notes_tmp
    notes_tmp="$(mktemp)"
    printf '%s\n' "$RELEASE_NOTES" > "$notes_tmp"

    warn "🌐 Creating GitHub release and uploading artifacts..."
    gh release create "$TAG" \
        --repo "${REPO_OWNER}/${REPO_NAME}" \
        --title "WeatherPossum ${NEW_VERSION_NAME}" \
        --notes-file "$notes_tmp" \
        "$GRADLE_APK" \
        "$CHECKSUM_FILE"
    rm -f "$notes_tmp"
}

# --- main ---

info "🚀 WeatherPossum Release"
echo "=================================="

preflight
read_gradle_version
prompt_bump_kind
compute_new_version
if [ "$DRY_RUN" -eq 0 ]; then
    assert_tag_available
    ensure_keystore_properties
fi
if [ "$DRY_RUN" -eq 1 ] && [ -z "$NOTES_FILE" ]; then
    RELEASE_NOTES="(dry run — notes not collected)"
else
    collect_release_notes
fi
confirm_plan
update_gradle_version
update_changelog
build_signed_apk
verify_apk
generate_checksum
commit_tag_and_release

echo
info "🎉 Release ${NEW_VERSION_NAME} published"
echo "  Tag:      ${TAG}"
echo "  APK:      ${GRADLE_APK}"
echo "  SHA-256:  ${CHECKSUM}"
echo "  Changelog updated in ${CHANGELOG_FILE}"
echo
echo "The in-app updater can now detect this release, download the APK,"
echo "verify the checksum, and install the update."
