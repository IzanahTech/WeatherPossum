#!/bin/bash

# GitHub Release Helper Script for WeatherPossum
# This script helps create releases with proper APK and checksum files

set -e

# Configuration
REPO_OWNER="IzanahTech"
REPO_NAME="WeatherPossum"
APK_PATH="app/build/outputs/apk/release/app-release.apk"

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

# Get version from APK
echo -e "${YELLOW}📋 Extracting version information...${NC}"
VERSION_NAME=$(aapt dump badging "$APK_PATH" | grep "versionName" | sed "s/.*versionName='\([^']*\)'.*/\1/")
VERSION_CODE=$(aapt dump badging "$APK_PATH" | grep "versionCode" | sed "s/.*versionCode='\([^']*\)'.*/\1/")
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
