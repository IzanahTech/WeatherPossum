# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Lottie Proguard rules ---
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# --- Jetpack Compose Proguard rules ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.activity.compose.** { *; }
-dontwarn androidx.activity.compose.**
-keep class androidx.lifecycle.compose.** { *; }
-dontwarn androidx.lifecycle.compose.**
-keep class androidx.savedstate.** { *; }
-dontwarn androidx.savedstate.**
-keep class androidx.customview.** { *; }
-dontwarn androidx.customview.**
-keep class androidx.compose.runtime.saveable.** { *; }
-dontwarn androidx.compose.runtime.saveable.**