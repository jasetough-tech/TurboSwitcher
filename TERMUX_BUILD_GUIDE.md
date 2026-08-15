# TurboSwitcher - Termux Build Guide (Oppo A5x)

## 📱 Prerequisites

### 1. Install Termux
- Download from: [F-Droid](https://f-droid.org/packages/com.termux/) (RECOMMENDED - most stable)
- **DO NOT** use Google Play version (outdated)

### 2. Initial Termux Setup

Open Termux and run these commands:

```bash
# Update packages
pkg update && pkg upgrade -y

# Install Git
pkg install git -y

# Install OpenJDK (Java)
pkg install openjdk-17 -y

# Install essential build tools
pkg install build-essential clang -y
```

## 🔧 Clone & Prepare Project

```bash
# Create work directory
mkdir -p ~/projects
cd ~/projects

# Clone your repository
git clone https://github.com/jasetough-tech/TurboSwitcher.git
cd TurboSwitcher

# Checkout the fixed branch
git checkout feature/turboswitcher-app
```

## 📦 Download Android SDK (IMPORTANT)

The first time, you need to download the Android SDK. This is large (~5GB) and may take time.

```bash
# Create SDK directory
mkdir -p ~/Android/sdk

# Set environment variables
export ANDROID_HOME=$HOME/Android/sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# Add to .bashrc so it persists
echo 'export ANDROID_HOME=$HOME/Android/sdk' >> ~/.bashrc
echo 'export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH' >> ~/.bashrc

# Reload bash
source ~/.bashrc
```

## 🚀 Build the APK

### Option A: Quick Build (Recommended for first time)

```bash
cd ~/projects/TurboSwitcher

# Make gradle executable
chmod +x gradlew

# Build debug APK (faster, no obfuscation)
./gradlew assembleDebug
```

**This will:**
- Download Gradle (~200MB)
- Download dependencies (~800MB)
- Compile Kotlin code
- Build APK
- **Total time: 15-30 minutes first time (depends on phone specs)**

### Output Location
```
app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Optimized Build (after first successful build)

```bash
# Clear old build artifacts
./gradlew clean

# Build release APK (smaller, optimized)
./gradlew assembleRelease
```

**Output:**
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

## 📲 Install APK on Your Phone

### Method 1: Via Termux (Easiest)

```bash
# Give Termux permission first:
# Go to Settings > Apps > Termux > Permissions > Files

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Manual Installation

```bash
# Find the APK
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Copy to Downloads folder (visible from file manager)
cp app/build/outputs/apk/debug/app-debug.apk $HOME/../usr/var/app/Downloads/

# Or use Termux file manager
cd app/build/outputs/apk/debug/
ls -la app-debug.apk
```

Then:
1. Open your phone's **Files** app
2. Navigate to **Downloads**
3. Find **app-debug.apk**
4. Tap to install
5. Allow installation from unknown sources if prompted

## 🔍 View Logs (Debugging)

```bash
# View real-time logs
adb logcat | grep turboswitcher

# Or filter all logs
adb logcat

# Save logs to file
adb logcat > logs.txt &
```

## 🧪 Test the App

1. **Launch TurboSwitcher** from app drawer
2. **Tap "Start Overlay"** → should show permission prompt
3. **Grant "Display over other apps"** permission
4. **Tap "Grant Usage Access"** → opens system settings
5. **Enable "TurboSwitcher"** in Usage Access
6. Return to app
7. **Tap "Start Overlay"** again
8. A **floating purple bubble** should appear on screen
9. **Tap the bubble** → should expand to show app launcher

## ✅ Verification Checklist

- [ ] APK built successfully
- [ ] APK installed on Oppo A5x
- [ ] App launches without crashing
- [ ] Floating bubble appears
- [ ] Bubble expands when tapped
- [ ] Can tap pinned apps to launch them
- [ ] Search bar is functional
- [ ] No crashes in logcat

## 🚨 Troubleshooting

### "gradlew: command not found"
```bash
chmod +x gradlew
```

### "No space left on device"
Android SDK needs 5-10GB free space. Check:
```bash
df -h
```

### "JAVA_HOME not set"
```bash
export JAVA_HOME=/data/data/com.termux/files/usr
export PATH=$JAVA_HOME/bin:$PATH
```

### APK crashes on install
```bash
# Uninstall old version
adb uninstall com.turboswitcher

# Reinstall
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Can't see floating bubble
1. Check: Settings > Apps > TurboSwitcher > Display over other apps (ENABLED)
2. Check logs: `adb logcat | grep turboswitcher`
3. Restart the app

## 📚 Useful Termux Commands

```bash
# Check available disk space
df -h

# Check RAM usage
free -h

# Monitor build progress
tail -f build.log

# Stop running gradle process
pkill -f gradle

# Clear gradle cache (if build fails)
rm -rf ~/.gradle/caches
```

## 🎯 Next Steps

1. **Follow steps above to build**
2. **Test on your Oppo A5x**
3. **Report any issues** with logcat output
4. **Once working, we can:**
   - Add more workflows
   - Customize theme colors
   - Optimize performance
   - Create release build

---

**Need help?** Share:
- Output from `./gradlew assembleDebug`
- Logcat output: `adb logcat | grep turboswitcher`
- Error messages from Termux
