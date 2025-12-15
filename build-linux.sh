#!/bin/bash

# Build script for Linux native app with ProGuard optimization
# Supports CI mode: set CI=true to skip clean, VERSION env var for version
set -e

echo "=========================================="
echo "  Kimai Desktop - Linux Build"
echo "  WITH ProGuard Optimization"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if required tools are available
command -v java >/dev/null 2>&1 || { echo -e "${RED}Java is required but not installed. Aborting.${NC}" >&2; exit 1; }

# Auto-detect and configure Java 21 (required for build)
configure_java21() {
    # Skip if JAVA_HOME is already set to Java 21
    if [ -n "$JAVA_HOME" ]; then
        CURRENT_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$CURRENT_VERSION" = "21" ]; then
            echo -e "${GREEN}Using Java 21 from JAVA_HOME: $JAVA_HOME${NC}"
            return 0
        fi
    fi

    # Common Java 21 locations
    JAVA21_PATHS=(
        "/usr/lib/jvm/java-21-openjdk"           # Arch/Manjaro
        "/usr/lib/jvm/java-21-openjdk-amd64"     # Ubuntu/Debian
        "/usr/lib/jvm/temurin-21-jdk"            # Temurin on Linux
        "/usr/lib/jvm/temurin-21-jdk-amd64"      # Temurin on Ubuntu
        "/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/21"*"/x64"  # GitHub Actions
    )

    for path in "${JAVA21_PATHS[@]}"; do
        # Handle glob patterns
        for expanded_path in $path; do
            if [ -d "$expanded_path" ] && [ -x "$expanded_path/bin/java" ]; then
                export JAVA_HOME="$expanded_path"
                export PATH="$JAVA_HOME/bin:$PATH"
                echo -e "${GREEN}Auto-detected Java 21: $JAVA_HOME${NC}"
                return 0
            fi
        done
    done

    # Check current java version
    CURRENT_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$CURRENT_VERSION" = "21" ]; then
        echo -e "${GREEN}System Java is version 21${NC}"
        return 0
    fi

    echo -e "${YELLOW}Warning: Java 21 not found. Build requires Java 21 (current: Java $CURRENT_VERSION)${NC}"
    echo "   Please install Java 21 or set JAVA_HOME to a Java 21 installation."
    echo "   On Arch/Manjaro: sudo pacman -S jdk21-openjdk"
    echo "   On Ubuntu/Debian: sudo apt install openjdk-21-jdk"
    return 1
}

configure_java21 || exit 1
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

# Set build configuration for ProGuard
export GRADLE_OPTS="-Xmx6g -XX:+UseG1GC -XX:+UseStringDeduplication"
export JAVA_OPTS="-Xmx6g"

echo -e "${BLUE}Building with ProGuard optimization...${NC}"
echo "   - Shrinking: Removing unused code"
echo "   - Optimizing: Code optimization passes"
echo "   - Debug removal: Removing debug calls"
echo ""

# Build version parameter
VERSION_PARAM=""
if [ -n "$VERSION" ]; then
    VERSION_PARAM="-PprojVersion=$VERSION"
    echo -e "${BLUE}Building version: $VERSION${NC}"
fi

START_TIME=$(date +%s)

# Build AppImage installer
echo -e "${BLUE}Building Linux AppImage...${NC}"
./gradlew :kimai-desktop:packageReleaseAppImage \
    $VERSION_PARAM \
    -Pbuildkonfig.flavor=release \
    --parallel \
    --build-cache \
    --no-daemon \
    --stacktrace

# Also build portable distribution
echo ""
echo -e "${BLUE}Building portable distribution...${NC}"
./gradlew :kimai-desktop:createReleaseDistributable \
    $VERSION_PARAM \
    --no-daemon \
    --stacktrace

END_TIME=$(date +%s)
BUILD_TIME=$((END_TIME - START_TIME))

echo ""
echo -e "${GREEN}Build complete in ${BUILD_TIME}s${NC}"
echo ""

# Show build results
echo -e "${BLUE}Build artifacts:${NC}"
echo "   AppImage: kimai-desktop/build/compose/binaries/main-release/app/"
echo "   Portable: kimai-desktop/build/compose/binaries/main-release/app/kimai/"
echo ""

# Show sizes
if [ -d "kimai-desktop/build/compose/binaries/main-release/app/kimai/" ]; then
    size=$(du -sh kimai-desktop/build/compose/binaries/main-release/app/kimai/ | cut -f1)
    echo -e "${GREEN}Portable app size: $size${NC}"
fi

APPIMAGE=$(find kimai-desktop/build/compose/binaries/main-release -name "*.AppImage" -type f 2>/dev/null | head -1)
if [ -n "$APPIMAGE" ]; then
    appimage_size=$(du -sh "$APPIMAGE" | cut -f1)
    echo -e "${GREEN}AppImage size: $appimage_size${NC}"
fi

echo ""
echo -e "${GREEN}=========================================="
echo "  Build Complete!"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}To run the app:${NC}"
echo "   cd kimai-desktop/build/compose/binaries/main-release/app/kimai/bin/"
echo "   ./kimai"
echo ""
echo -e "${BLUE}ProGuard optimizations applied:${NC}"
echo "   - Unused code removed"
echo "   - Code optimization passes"
echo "   - Debug calls eliminated"
echo "   - Kotlin intrinsics optimized"
echo "   - Built with Java 21 runtime"
echo ""
