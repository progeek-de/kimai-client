#!/bin/bash

# Build script for Linux native app with ProGuard optimization
set -e

echo "🚀 Building Linux native app with ProGuard optimization..."

# Check if required tools are available
command -v java >/dev/null 2>&1 || { echo "❌ Java is required but not installed. Aborting." >&2; exit 1; }

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Set build configuration for ProGuard
export GRADLE_OPTS="-Xmx6g -XX:+UseG1GC -XX:+UseStringDeduplication"
export JAVA_OPTS="-Xmx6g"

echo "📦 Building with ProGuard optimization..."
echo "   ✓ Shrinking: Removing unused code"
echo "   ✓ Optimizing: Code optimization passes"
echo "   ✓ Debug removal: Removing debug calls"
echo ""

# Build with ProGuard release configuration
./gradlew :kimai-desktop:packageReleaseAppImage \
    -Pbuildkonfig.flavor=release \
    --parallel \
    --build-cache \
    --info

echo ""
echo "✅ ProGuard build completed!"
echo ""

# Show build results
echo "📁 Build artifacts location:"
echo "   Native App: kimai-desktop/build/compose/binaries/main-release/app/kimai/"
echo ""

# Compare sizes if previous build exists
if [ -d "kimai-desktop/build/compose/binaries/main/app/kimai/" ] && [ -d "kimai-desktop/build/compose/binaries/main-release/app/kimai/" ]; then
    echo "📊 Size comparison:"
    
    # Original size
    original_size=$(du -sh kimai-desktop/build/compose/binaries/main/app/kimai/ 2>/dev/null | cut -f1 || echo "N/A")
    echo "   Original build: $original_size"
    
    # ProGuard optimized size
    optimized_size=$(du -sh kimai-desktop/build/compose/binaries/main-release/app/kimai/ 2>/dev/null | cut -f1 || echo "N/A")
    echo "   ProGuard build: $optimized_size"
    
    # Calculate savings if both exist
    if [ "$original_size" != "N/A" ] && [ "$optimized_size" != "N/A" ]; then
        original_bytes=$(du -sb kimai-desktop/build/compose/binaries/main/app/kimai/ 2>/dev/null | cut -f1 || echo "0")
        optimized_bytes=$(du -sb kimai-desktop/build/compose/binaries/main-release/app/kimai/ 2>/dev/null | cut -f1 || echo "0")
        
        if [ "$original_bytes" -gt 0 ] && [ "$optimized_bytes" -gt 0 ]; then
            savings=$(echo "scale=1; (1 - $optimized_bytes / $original_bytes) * 100" | bc -l 2>/dev/null || echo "N/A")
            if [ "$savings" != "N/A" ]; then
                echo "   Size reduction: ${savings}%"
            fi
        fi
    fi
else
    # Show current build size
    if [ -d "kimai-desktop/build/compose/binaries/main-release/app/kimai/" ]; then
        size=$(du -sh kimai-desktop/build/compose/binaries/main-release/app/kimai/ | cut -f1)
        echo "📊 ProGuard optimized size: $size"
    fi
fi

echo ""
echo "🎉 ProGuard optimized Linux native app ready!"
echo ""
echo "💡 To run the optimized app:"
echo "   cd kimai-desktop/build/compose/binaries/main-release/app/kimai/bin/"
echo "   ./kimai"
echo ""
echo "📦 To create optimized portable package:"
echo "   cd kimai-desktop/build/compose/binaries/main-release/app/"
echo "   tar -czf kimai-linux.tar.gz kimai/"
echo ""
echo "🚀 ProGuard optimizations applied:"
echo "   ✓ Unused code removed"
echo "   ✓ Code optimization passes"
echo "   ✓ Debug calls eliminated"
echo "   ✓ Kotlin intrinsics optimized"
echo "   ✓ Logging calls removed"