#!/bin/bash
# TurboSwitcher - Automated Build & Install Script for Termux
# Usage: chmod +x build-and-install.sh && ./build-and-install.sh

set -e

echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "  TurboSwitcher - Build & Install Script"
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

# Check if in correct directory
if [ ! -f "gradlew" ]; then
    echo "❌ Error: gradlew not found. Are you in the TurboSwitcher directory?"
    exit 1
fi

echo "✅ Found gradlew"
echo ""

# Make gradlew executable
chmod +x gradlew
echo "✅ Made gradlew executable"

# Set environment variables
export ANDROID_HOME=$HOME/Android/sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

echo ""
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo "  BUILDING DEBUG APK..."
echo "═══════════════════════════════════════════════════════════════════════════════════"
echo ""

# Build APK
if ./gradlew assembleDebug; then
    echo ""
    echo "═══════════════════════════════════════════════════════════════════════════════════"
    echo "  ✅ BUILD SUCCESSFUL!"
    echo "═══════════════════════════════════════════════════════════════════════════════════"
    echo ""
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
        echo "📦 APK Location: $APK_PATH"
        echo "📊 APK Size: $APK_SIZE"
        echo ""
        echo "═══════════════════════════════════════════════════════════════════════════════════"
        echo "  NEXT STEPS:"
        echo "═══════════════════════════════════════════════════════════════════════════════════"
        echo ""
        echo "Option 1 - Install via ADB (if available):"
        echo "  adb install $APK_PATH"
        echo ""
        echo "Option 2 - Manual Install:"
        echo "  1. Open Files app on your Oppo A5x"
        echo "  2. Navigate to: /sdcard/Download/"
        echo "  3. Tap app-debug.apk to install"
        echo ""
        echo "Option 3 - Copy to Downloads:"
        echo "  cp $APK_PATH \$HOME/../usr/var/app/Download/"
        echo ""
        echo "═══════════════════════════════════════════════════════════════════════════════════"
        echo ""
    fi
else
    echo ""
    echo "❌ BUILD FAILED!"
    echo ""
    echo "Troubleshooting:"
    echo "1. Ensure you have at least 5GB free space: df -h"
    echo "2. Check JAVA_HOME: echo \$JAVA_HOME"
    echo "3. Clear cache: rm -rf ~/.gradle/caches"
    echo "4. Try again: ./gradlew clean assembleDebug"
    echo ""
    exit 1
fi
