# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# --- Koin ---
-keep class org.koin.core.** { *; }
-keep class org.koin.core.scope.Scope { *; }
-keep class org.koin.java.* { *; }
-keep class org.koin.android.* { *; }
-keep class org.koin.androidx.* { *; }
-keep class * extends org.koin.core.module.Module { *; }
-keep class * implements org.koin.core.module.Module { *; }
-keepattributes Signature
-keepattributes InnerClasses

# --- Moshi (codegen adapters) ---
-keepattributes *Annotation*
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class **JsonAdapter { *; }
-keepnames class com.weatherpossum.app.data.model.** { *; }
-keepnames class com.weatherpossum.app.data.api.GhRelease { *; }
-keepnames class com.weatherpossum.app.data.api.GhAsset { *; }
-keepnames class com.weatherpossum.app.data.cache.** { *; }

# --- Retrofit ---
-keep,allowobfuscation,allowshrinking interface com.weatherpossum.app.data.api.** { *; }
-dontwarn okhttp3.internal.platform.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# --- Time4J Android ---
# Time4J uses ServiceLoader to locate AndroidResourceLoader. If this gets removed by shrinking,
# Time4J can fail fast at startup with "Wrong configuration of external resource loader".
-keep class net.time4j.android.spi.AndroidResourceLoader { *; }

# --- JSoup ---
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# --- Glance App Widget ---
-keep class androidx.glance.appwidget.** { *; }
-dontwarn androidx.glance.**

# --- WorkManager ---
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
