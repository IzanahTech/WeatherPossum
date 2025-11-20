#!/bin/bash

# GitHub Release Helper Script for WeatherPossum
# This script helps create releases with proper APK and checksum files

set -e

# Configuration
REPO_OWNER="IzanahTech"
REPO_NAME="WeatherPossum"
APK_PATH="app/release/app-release.apk"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 WeatherPossum Release Helper${NC}"
echo "=================================="

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo -e "${RED}❌ APK not found at: $APK_PATH${NC}"
    echo "Please build the release APK first:"
    echo "  ./gradlew assembleRelease"
    exit 1
fi

# Get version information
echo -e "${YELLOW}📱 APK Information:${NC}"
APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
echo "  Path: $APK_PATH"
echo "  Size: $APK_SIZE"

# Generate checksum
echo -e "${YELLOW}🔐 Generating SHA256 checksum...${NC}"
APK_FILENAME=$(basename "$APK_PATH")
CHECKSUM_FILE="${APK_FILENAME}.sha256"
sha256sum "$APK_PATH" > "$CHECKSUM_FILE"
CHECKSUM=$(cat "$CHECKSUM_FILE")
echo "  Checksum: $CHECKSUM"

# Get version from APK or build.gradle.kts
echo -e "${YELLOW}📋 Extracting version information...${NC}"

# Try to find aapt in common Android SDK locations
AAPT_CMD=""
if command -v aapt &> /dev/null; then
    AAPT_CMD="aapt"
elif [ -n "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/build-tools"/*/aapt ]; then
    AAPT_CMD=$(find "$ANDROID_HOME/build-tools" -name aapt 2>/dev/null | head -1)
elif [ -n "$ANDROID_SDK_ROOT" ] && [ -f "$ANDROID_SDK_ROOT/build-tools"/*/aapt ]; then
    AAPT_CMD=$(find "$ANDROID_SDK_ROOT/build-tools" -name aapt 2>/dev/null | head -1)
fi

if [ -n "$AAPT_CMD" ] && [ -f "$APK_PATH" ]; then
    VERSION_NAME=$("$AAPT_CMD" dump badging "$APK_PATH" 2>/dev/null | grep "versionName" | sed "s/.*versionName='\([^']*\)'.*/\1/" || echo "")
    VERSION_CODE=$("$AAPT_CMD" dump badging "$APK_PATH" 2>/dev/null | grep "versionCode" | sed "s/.*versionCode='\([^']*\)'.*/\1/" || echo "")
fi

# Fallback to reading from build.gradle.kts if aapt failed or not found
if [ -z "$VERSION_NAME" ] || [ -z "$VERSION_CODE" ]; then
    echo "  ⚠️  aapt not found, reading from build.gradle.kts..."
    if [ -f "app/build.gradle.kts" ]; then
        VERSION_NAME=$(grep "versionName" app/build.gradle.kts | sed -E 's/^[^"]*"([^"]+)".*/\1/')
        VERSION_CODE=$(grep "versionCode" app/build.gradle.kts | awk -F'=' '{print $2}' | tr -d ' ')
    fi
fi

# Final fallback: prompt user
if [ -z "$VERSION_NAME" ] || [ -z "$VERSION_CODE" ]; then
    echo -e "${YELLOW}  Please enter version information:${NC}"
    read -p "  Version Name (e.g., 1.5.0): " VERSION_NAME
    read -p "  Version Code (e.g., 3): " VERSION_CODE
fi

echo "  Version Name: $VERSION_NAME"
echo "  Version Code: $VERSION_CODE"

# Create release tag
TAG="v$VERSION_NAME"
echo -e "${YELLOW}🏷️  Release Tag: $TAG${NC}"

# Prompt for release notes
echo -e "${YELLOW}📝 Release Notes:${NC}"
echo "Enter release notes (press Enter twice to finish):"
RELEASE_NOTES=""
while IFS= read -r line; do
    if [ -z "$line" ]; then
        break
    fi
    RELEASE_NOTES="${RELEASE_NOTES}${line}\n"
done

# Create GitHub release (requires GitHub CLI)
if command -v gh &> /dev/null; then
    echo -e "${YELLOW}🌐 Creating GitHub release...${NC}"
    
    # Create release with APK and checksum
    gh release create "$TAG" \
        --title "WeatherPossum $VERSION_NAME" \
        --notes "$RELEASE_NOTES" \
        "$APK_PATH" \
        "$CHECKSUM_FILE"
    
    echo -e "${GREEN}✅ Release created successfully!${NC}"
    echo "  Tag: $TAG"
    echo "  APK: $APK_FILENAME"
    echo "  Checksum: $CHECKSUM_FILE"
    
else
    echo -e "${YELLOW}⚠️  GitHub CLI not found. Manual release creation required.${NC}"
    echo ""
    echo "To create the release manually:"
    echo "1. Go to: https://github.com/$REPO_OWNER/$REPO_NAME/releases/new"
    echo "2. Create new release with tag: $TAG"
    echo "3. Title: WeatherPossum $VERSION_NAME"
    echo "4. Upload files:"
    echo "   - $APK_PATH"
    echo "   - $CHECKSUM_FILE"
    echo "5. Add release notes:"
    echo "$RELEASE_NOTES"
fi

# Cleanup
echo -e "${YELLOW}🧹 Cleaning up...${NC}"
rm -f "$CHECKSUM_FILE"

echo -e "${GREEN}🎉 Release preparation complete!${NC}"
echo ""
echo "The in-app updater will now be able to:"
echo "  ✅ Detect the new version"
echo "  ✅ Download the APK securely"
echo "  ✅ Verify the checksum"
echo "  ✅ Install the update"
