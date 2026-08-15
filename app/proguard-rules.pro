# Keep data model classes for Room
-keep class com.turboswitcher.data.model.** { *; }
-keep class com.turboswitcher.data.local.*Entity { *; }

# Keep annotations
-keepattributes *Annotation*

# Keep WorkManager
-dontwarn androidx.work.**
-keep class androidx.work.** { *; }

# Keep Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Optimize
-optimizationpasses 5
-dontusemixedcaseclassnames
