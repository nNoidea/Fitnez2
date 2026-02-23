# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep the localization classes for reflection-based auto-discovery
-keep class com.nnoidea.fitnez2.core.localization.EnStrings { *; }
-keep class * extends com.nnoidea.fitnez2.core.localization.EnStrings { *; }

# Keep kotlin-reflect metadata if needed (though keeping the classes above usually suffices)
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault,EnclosingMethod,InnerClasses,Signature
-keep class kotlin.reflect.jvm.internal.** { *; }

# Keep Backup data models for GSON serialization/deserialization
-keep class com.nnoidea.fitnez2.data.models.BackupData { *; }
-keep class com.nnoidea.fitnez2.data.models.ExportedExercise { *; }
-keep class com.nnoidea.fitnez2.data.models.ExportedRecord { *; }

# Keep GSON annotations and ensure they are visible at runtime
-keepattributes Signature, *Annotation*
-keep class com.google.gson.annotations.** { *; }