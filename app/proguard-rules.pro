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

# --- Koin Proguard rules ---
# Koin
-keep class org.koin.core.scope.Scope { *; }
-keep class org.koin.java.* { *; }
-keep class org.koin.android.* { *; }
-keep class org.koin.androidx.* { *; }
-keep class * extends org.koin.core.module.Module { *; }
-keep class * implements org.koin.core.module.Module { *; }
-keep class org.koin.androidx.workmanager.dsl.* { *; }
-keep class org.koin.androidx.compose.* { *; }
-keep class org.koin.androidx.compose.navigation.* { *; }

# Allow reflection for Koin internal features
-keepattributes Signature
-keepattributes InnerClasses

# Keep @KoinViewModel classes
-keep class * extends androidx.lifecycle.ViewModel {
    @org.koin.android.annotation.KoinViewModel <init>(...);
}
# Keep @KoinWorker classes
-keep class * extends androidx.work.ListenableWorker {
    @org.koin.android.annotation.KoinWorker <init>(...);
}

# --- Other Libraries ---
# Note: If you encounter issues in release builds with libraries such as
# JSoup, Retrofit, OkHttp, or Gson, you may need to add specific Proguard
# rules for them. Consult their respective documentation for details.
# For example, Gson often requires rules for @SerializedName and TypeAdapters.
# OkHttp and Retrofit might need rules if using advanced features or with specific serialization libraries.