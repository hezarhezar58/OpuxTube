# ---- NewPipeExtractor ----
-keep class org.schabi.newpipe.extractor.** { *; }
-dontwarn org.schabi.newpipe.extractor.**
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.** { *; }
-dontwarn org.mozilla.javascript.**

# jsoup ships an optional re2j regex backend that we don't include — it falls back
# to java.util.regex at runtime, so silence the missing-class warnings.
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern

# ---- Media3 ----
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ---- Domain models (reflection via kotlinx.serialization) ----
-keep class dev.opux.tubeclient.core.domain.** { *; }
