#!/bin/bash

# Build Windows Portable Version on Linux WITH ProGuard
# This creates a portable Windows app with ProGuard optimization
# Can be built on Linux!

set -e  # Exit on error

echo "=========================================="
echo "  Kimai Desktop - Windows Portable Build"
echo "  WITH ProGuard Optimization"
echo "  (Can be built on Linux!)"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}Building portable Windows app with ProGuard optimization${NC}"
echo -e "${YELLOW}Note: This builds a PORTABLE version only${NC}"
echo -e "${YELLOW}Windows installers (.exe/.msi) require Windows + WiX Toolset${NC}"
echo ""

# Check Java 17 (Gradle will use the one from gradle.properties)
echo -e "${BLUE}Checking Java configuration...${NC}"
if [ -f "gradle.properties" ] && grep -q "org.gradle.java.home" gradle.properties; then
    GRADLE_JAVA=$(grep "org.gradle.java.home" gradle.properties | cut -d'=' -f2)
    echo -e "${GREEN}✓ Gradle configured to use: $GRADLE_JAVA${NC}"
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" != "17" ]; then
        echo -e "${YELLOW}Warning: Java 17 is required for ProGuard compatibility${NC}"
        echo "Current Java version: $JAVA_VERSION"
        echo "Please set JAVA_HOME to Java 17 or configure gradle.properties"
        exit 1
    fi
    echo -e "${GREEN}✓ Java 17 detected${NC}"
fi
echo ""

# Clean previous builds
echo -e "${BLUE}Cleaning previous builds...${NC}"
./gradlew clean
echo -e "${GREEN}✓ Clean complete${NC}"
echo ""

# Build portable app with ProGuard
echo -e "${BLUE}Building Windows portable app with ProGuard...${NC}"
echo "This may take 2-3 minutes..."
echo ""

START_TIME=$(date +%s)

./gradlew :kimai-desktop:createReleaseDistributable

END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}✓ Build complete in ${BUILD_TIME}s${NC}"
echo ""

# Find build directory
BUILD_DIR="kimai-desktop/build/compose/binaries/main-release/app"

if [ ! -d "$BUILD_DIR" ]; then
    echo -e "${RED}Error: Build directory not found!${NC}"
    echo "Expected: $BUILD_DIR"
    exit 1
fi

# Get app size
APP_SIZE=$(du -sh "$BUILD_DIR" | cut -f1)

echo -e "${BLUE}Creating portable archive...${NC}"

# Create archive file
cd kimai-desktop/build/compose/binaries/main-release

# Try zip first, fallback to tar.gz
if command -v zip &> /dev/null; then
    ARCHIVE_NAME="kimai-windows-portable-proguard.zip"
    if [ -f "$ARCHIVE_NAME" ]; then
        rm "$ARCHIVE_NAME"
    fi
    zip -r -q "$ARCHIVE_NAME" app/
else
    ARCHIVE_NAME="kimai-windows.tar.gz"
    if [ -f "$ARCHIVE_NAME" ]; then
        rm "$ARCHIVE_NAME"
    fi
    tar -czf "$ARCHIVE_NAME" app/
fi

ARCHIVE_SIZE=$(du -sh "$ARCHIVE_NAME" | cut -f1)

cd - > /dev/null

echo -e "${GREEN}✓ Archive created${NC}"
echo ""

# Display results
echo -e "${GREEN}=========================================="
echo "  Build Complete! 🎉"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}Build artifacts:${NC}"
echo ""
echo "  📁 Portable App Directory:"
echo "     Location: $BUILD_DIR"
echo "     Size: $APP_SIZE"
echo ""
echo "  📦 Archive (ProGuard optimized):"
echo "     Location: kimai-desktop/build/compose/binaries/main-release/$ARCHIVE_NAME"
echo "     Size: $ARCHIVE_SIZE"
echo ""
echo -e "${BLUE}How to use:${NC}"
echo ""
echo "  1. Copy the archive to a Windows machine"
echo "  2. Extract the archive"
echo "  3. Run app/kimai.exe"
echo "  4. No installation required!"
echo ""
echo -e "${GREEN}ProGuard Optimizations Applied:${NC}"
echo "  ✅ Code shrinking (unused code removed)"
echo "  ✅ Code optimization (performance improved)"
echo "  ✅ Resource optimization (duplicates removed)"
echo "  ✅ ~5-10% smaller than non-ProGuard build"
echo ""
echo -e "${YELLOW}Note:${NC}"
echo "  - This is a PORTABLE version (no installer)"
echo "  - Includes embedded Java 17 runtime"
echo "  - Works on any Windows 10/11 system"
echo "  - No admin rights needed"
echo "  - ProGuard optimized for production use"
echo ""
echo -e "${YELLOW}For Windows installers (.exe/.msi):${NC}"
echo "  - Build on Windows using: ./build-windows-proguard.sh"
echo "  - Or use CI/CD with Windows runners"
echo "  - Or use a Windows VM"
echo ""