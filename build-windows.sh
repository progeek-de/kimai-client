#!/bin/bash

# Build Windows Version with ProGuard
# This creates a portable Windows app with ProGuard optimization
# Can be built on Linux! (MSI installer requires Windows)
# Supports CI mode: set CI=true to skip clean, VERSION env var for version

set -e  # Exit on error

echo "=========================================="
echo "  Kimai Desktop - Windows Build"
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

echo -e "${BLUE}Building Windows app with ProGuard optimization${NC}"
echo ""

# Auto-detect and configure Java 17 (required for ProGuard compatibility)
# ProGuard 7.2.2 doesn't support Java 21+
configure_java17() {
    # Skip if JAVA_HOME is already set to Java 17
    if [ -n "$JAVA_HOME" ]; then
        CURRENT_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$CURRENT_VERSION" = "17" ]; then
            echo -e "${GREEN}Using Java 17 from JAVA_HOME: $JAVA_HOME${NC}"
            return 0
        fi
    fi

    # Common Java 17 locations
    JAVA17_PATHS=(
        "/usr/lib/jvm/java-17-openjdk"           # Arch/Manjaro
        "/usr/lib/jvm/java-17-openjdk-amd64"     # Ubuntu/Debian
        "/usr/lib/jvm/temurin-17-jdk"            # Temurin on Linux
        "/usr/lib/jvm/temurin-17-jdk-amd64"      # Temurin on Ubuntu
        "/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/17"*"/x64"  # GitHub Actions Linux
        "/c/hostedtoolcache/windows/Java_Temurin-Hotspot_jdk/17"*"/x64"  # GitHub Actions Windows
        "/c/Program Files/Eclipse Adoptium/jdk-17"*  # Windows Temurin
        "/c/Program Files/Java/jdk-17"*          # Windows Oracle JDK
    )

    for path in "${JAVA17_PATHS[@]}"; do
        # Handle glob patterns
        for expanded_path in $path; do
            if [ -d "$expanded_path" ] && [ -x "$expanded_path/bin/java" ]; then
                export JAVA_HOME="$expanded_path"
                export PATH="$JAVA_HOME/bin:$PATH"
                echo -e "${GREEN}Auto-detected Java 17: $JAVA_HOME${NC}"
                return 0
            fi
        done
    done

    # Check current java version
    CURRENT_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$CURRENT_VERSION" = "17" ]; then
        echo -e "${GREEN}System Java is version 17${NC}"
        return 0
    fi

    echo -e "${YELLOW}Warning: Java 17 not found. ProGuard requires Java 17 (current: Java $CURRENT_VERSION)${NC}"
    echo "   Please install Java 17 or set JAVA_HOME to a Java 17 installation."
    return 1
}

configure_java17 || exit 1
echo ""

# Clean previous builds (skip in CI mode)
if [ "$CI" != "true" ]; then
    echo -e "${BLUE}Cleaning previous builds...${NC}"
    ./gradlew clean
    echo -e "${GREEN}Clean complete${NC}"
else
    echo -e "${BLUE}Skipping clean (CI mode)${NC}"
fi
echo ""

# Build version parameter
VERSION_PARAM=""
if [ -n "$VERSION" ]; then
    VERSION_PARAM="-PprojVersion=$VERSION"
    echo -e "${BLUE}Building version: $VERSION${NC}"
fi

# Build portable app with ProGuard
echo -e "${BLUE}Building Windows portable app with ProGuard...${NC}"
echo ""

START_TIME=$(date +%s)

./gradlew :kimai-desktop:createReleaseDistributable $VERSION_PARAM --no-daemon --stacktrace

# Try to build MSI installer (only works on Windows with WiX Toolset)
if [ "$CI" = "true" ] && [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" || "$OS" == "Windows_NT" ]]; then
    echo ""
    echo -e "${BLUE}Building Windows MSI installer...${NC}"
    ./gradlew :kimai-desktop:packageReleaseMsi $VERSION_PARAM --no-daemon --stacktrace || echo -e "${YELLOW}MSI build failed (WiX Toolset may not be installed)${NC}"
fi

END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}Build complete in ${BUILD_TIME}s${NC}"
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
    ARCHIVE_NAME="kimai-windows-portable.zip"
    if [ -f "$ARCHIVE_NAME" ]; then
        rm "$ARCHIVE_NAME"
    fi
    zip -r -q "$ARCHIVE_NAME" app/
else
    ARCHIVE_NAME="kimai-windows-portable.tar.gz"
    if [ -f "$ARCHIVE_NAME" ]; then
        rm "$ARCHIVE_NAME"
    fi
    tar -czf "$ARCHIVE_NAME" app/
fi

ARCHIVE_SIZE=$(du -sh "$ARCHIVE_NAME" | cut -f1)

cd - > /dev/null

echo -e "${GREEN}Archive created${NC}"
echo ""

# Display results
echo -e "${GREEN}=========================================="
echo "  Build Complete!"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}Build artifacts:${NC}"
echo ""
echo "  Portable App Directory:"
echo "     Location: $BUILD_DIR"
echo "     Size: $APP_SIZE"
echo ""
echo "  Archive (ProGuard optimized):"
echo "     Location: kimai-desktop/build/compose/binaries/main-release/$ARCHIVE_NAME"
echo "     Size: $ARCHIVE_SIZE"
echo ""
echo -e "${BLUE}How to use:${NC}"
echo ""
echo "  1. Copy the archive to a Windows machine"
echo "  2. Extract the archive"
echo "  3. Run app/kimai/kimai.exe"
echo "  4. No installation required!"
echo ""
echo -e "${GREEN}ProGuard Optimizations Applied:${NC}"
echo "  - Code shrinking (unused code removed)"
echo "  - Code optimization (performance improved)"
echo "  - Resource optimization (duplicates removed)"
echo ""
echo -e "${YELLOW}Note:${NC}"
echo "  - This is a PORTABLE version (no installer)"
echo "  - Includes embedded Java 17 runtime"
echo "  - Works on any Windows 10/11 system"
echo "  - No admin rights needed"
echo ""
echo -e "${YELLOW}For Windows installers (.exe/.msi):${NC}"
echo "  - Build on Windows with WiX Toolset installed"
echo "  - Or use CI/CD with Windows runners"
echo ""
