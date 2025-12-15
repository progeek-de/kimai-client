#!/bin/bash

# Build macOS Installers WITH ProGuard
# This creates .dmg and .pkg installers
# MUST be run on macOS!

set -e  # Exit on error

echo "=========================================="
echo "  Kimai Desktop - macOS Installer Build"
echo "  WITH ProGuard Optimization"
echo "  (Requires macOS!)"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if running on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo -e "${RED}Error: This script must be run on macOS!${NC}"
    echo ""
    echo "macOS installers (.dmg/.pkg) require macOS-specific tools."
    echo ""
    echo "Alternatives:"
    echo "  1. Use ./build-macos-portable-proguard.sh on Linux (creates portable version)"
    echo "  2. Run this script on a macOS machine"
    echo "  3. Use CI/CD with macOS runners"
    echo "  4. Use a macOS VM"
    echo ""
    exit 1
fi

echo -e "${BLUE}Building macOS installers with ProGuard optimization${NC}"
echo ""

# Check Java 21
echo -e "${BLUE}Checking Java configuration...${NC}"
if [ -f "gradle.properties" ] && grep -q "org.gradle.java.home" gradle.properties; then
    GRADLE_JAVA=$(grep "org.gradle.java.home" gradle.properties | cut -d'=' -f2)
    echo -e "${GREEN}✓ Gradle configured to use: $GRADLE_JAVA${NC}"
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" != "21" ]; then
        echo -e "${YELLOW}Warning: Java 21 is required for build${NC}"
        echo "Current Java version: $JAVA_VERSION"
        echo "Please set JAVA_HOME to Java 21 or configure gradle.properties"
        echo "On macOS: brew install openjdk@21"
        exit 1
    fi
    echo -e "${GREEN}✓ Java 21 detected${NC}"
fi
echo ""

# Clean previous builds
echo -e "${BLUE}Cleaning previous builds...${NC}"
./gradlew clean
echo -e "${GREEN}✓ Clean complete${NC}"
echo ""

# Build installers with ProGuard
echo -e "${BLUE}Building macOS installers with ProGuard...${NC}"
echo "This may take 3-5 minutes..."
echo ""

START_TIME=$(date +%s)

./gradlew :kimai-desktop:packageReleaseDistributionForCurrentOS

END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}✓ Build complete in ${BUILD_TIME}s${NC}"
echo ""

# Find build artifacts
DMG_FILE=$(find kimai-desktop/build/compose/binaries/main-release -name "*.dmg" 2>/dev/null | head -1)
PKG_FILE=$(find kimai-desktop/build/compose/binaries/main-release -name "*.pkg" 2>/dev/null | head -1)

# Display results
echo -e "${GREEN}=========================================="
echo "  Build Complete! 🎉"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}Build artifacts:${NC}"
echo ""

if [ -n "$DMG_FILE" ]; then
    DMG_SIZE=$(du -sh "$DMG_FILE" | cut -f1)
    echo "  📦 DMG Installer (ProGuard optimized):"
    echo "     Location: $DMG_FILE"
    echo "     Size: $DMG_SIZE"
    echo ""
fi

if [ -n "$PKG_FILE" ]; then
    PKG_SIZE=$(du -sh "$PKG_FILE" | cut -f1)
    echo "  📦 PKG Installer (ProGuard optimized):"
    echo "     Location: $PKG_FILE"
    echo "     Size: $PKG_SIZE"
    echo ""
fi

if [ -z "$DMG_FILE" ] && [ -z "$PKG_FILE" ]; then
    echo -e "${YELLOW}  Warning: No installer files found!${NC}"
    echo "  Check: kimai-desktop/build/compose/binaries/main-release/"
    echo ""
fi

echo -e "${BLUE}How to use:${NC}"
echo ""
echo "  DMG Installer:"
echo "    1. Double-click the .dmg file"
echo "    2. Drag Kimai to Applications folder"
echo "    3. Launch from Applications"
echo ""
echo "  PKG Installer:"
echo "    1. Double-click the .pkg file"
echo "    2. Follow installation wizard"
echo "    3. Launch from Applications"
echo ""
echo -e "${GREEN}ProGuard Optimizations Applied:${NC}"
echo "  ✅ Code shrinking (unused code removed)"
echo "  ✅ Code optimization (performance improved)"
echo "  ✅ Resource optimization (duplicates removed)"
echo "  ✅ ~5-10% smaller than non-ProGuard build"
echo ""
echo -e "${YELLOW}Note:${NC}"
echo "  - Includes embedded Java 21 runtime"
echo "  - Works on macOS 10.15 (Catalina) and later"
echo "  - ProGuard optimized for production use"
echo ""
echo -e "${YELLOW}Distribution:${NC}"
echo "  - For public distribution, sign with Apple Developer certificate"
echo "  - Use 'codesign' and 'notarytool' for App Store distribution"
echo "  - First launch may show 'unidentified developer' warning"
echo ""

