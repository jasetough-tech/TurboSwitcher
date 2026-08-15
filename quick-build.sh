#!/bin/bash
# Ultra-quick rebuild (after dependencies are cached)
cd "$(dirname "$0")"
echo "Building TurboSwitcher..."
./gradlew assembleDebug --offline 2>/dev/null || ./gradlew assembleDebug
echo ""
echo "✅ Build complete: app/build/outputs/apk/debug/app-debug.apk"
