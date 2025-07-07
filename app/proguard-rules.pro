# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/username/Library/Android/sdk/tools/proguard/proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection or JNI commands, consider using proguard.Keep annotation
# http://proguard.sourceforge.net/manual/annotations.html
-keep class com.google.dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.hilt.lifecycle.ViewModelInjectAdapter
-keep class * implements androidx.hilt.lifecycle.ViewModelInjectAdapter
-keep @dagger.hilt.android.internal.EarlyEntryPoint class * { *; }
-keep @dagger.hilt.android.internal.AggregatedEntryPoint class * { *; }
-keep @dagger.hilt.android.EntryPoint class * { *; }
-keep @dagger.hilt.android.components.ViewModelComponent class * { *; }
-keep @dagger.hilt.android.scopes.ViewModelScoped class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.Provides class * { *; }
-keep @javax.inject.Inject class * { *; }
-keep @javax.inject.Singleton class * { *; }

# Retain Room generated classes
-keep class **_Impl {
    *;
}
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static java.lang.String getMIGRATION_PARAM_BUNDLE_KEY();
    public static java.lang.String getMIGRATION_PARAM_START_VERSION_KEY();
    public static java.lang.String getMIGRATION_PARAM_END_VERSION_KEY();
}

# Keep default constructors of Views and other UI components if they are created via reflection.
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep public class * extends android.app.Fragment {
    public <init>();
}
-keep public class * extends androidx.fragment.app.Fragment {
    public <init>();
}

# Keep application classes that are referenced in the manifest
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# For Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile boolean _handled;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _result;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _state;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _update;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _value;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _next;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _prev;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _cur;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _token;
}
-keepclassmembers class ** {
    @kotlin.jvm.JvmField volatile java.lang.Object _consensus;
}

# For Jetpack Compose
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}
-keep class androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.runtime.internal.ComposableLambda { *; }
-keepclassmembernames class **.*$composable { *; }
-keepclassmembernames class **.ComposableSingletons* { *; }
-keepclassmembernames class **$WhenMappings { *; }

# Keep any classes that are used by GSON for serialization/deserialization
# -keep class com.google.gson.examples.android.model.** { *; }

# If you are using reflection
# -keepattributes Signature
# -keepattributes InnerClasses

# If you are using JNI
# -keepattributes NativeMethods

# If you are using R8 full mode, you might need to add more rules.
# See https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md
# and https://www.guardsquare.com/manual/configuration/usage
