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