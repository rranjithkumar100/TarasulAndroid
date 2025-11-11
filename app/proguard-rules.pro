# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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

# ========== General Android Rules ==========
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ========== Kotlin ==========
-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# ========== Kotlinx Coroutines ==========
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ========== Compose ==========
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# ========== Hilt / Dagger ==========
-dontwarn com.google.errorprone.annotations.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$ViewWithFragmentComponentBuilderEntryPoint
-keep class * extends dagger.hilt.internal.GeneratedComponentManager
-keep class **_HiltModules { *; }
-keep class **_HiltModules$** { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keepclasseswithmembernames class * {
    @dagger.** <methods>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembernames class * {
    @javax.inject.* <methods>;
}
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ========== Retrofit ==========
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ========== OkHttp ==========
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ========== Moshi ==========
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
}
-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}

# ========== Data Classes / Models ==========
-keep class com.tcc.tarasulandroid.data.** { *; }
-keep class com.tcc.tarasulandroid.feature.**.model.** { *; }
-keepclassmembers class com.tcc.tarasulandroid.data.** {
    <init>(...);
    <fields>;
}
-keepclassmembers class com.tcc.tarasulandroid.feature.**.model.** {
    <init>(...);
    <fields>;
}

# ========== ViewModels ==========
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.tcc.tarasulandroid.viewmodels.** { *; }
-keep class com.tcc.tarasulandroid.feature.**.viewmodels.** { *; }

# ========== Navigation ==========
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# ========== DataStore ==========
-keep class androidx.datastore.*.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# ========== Encrypted SharedPreferences ==========
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# ========== Socket.IO ==========
-keep class io.socket.** { *; }
-keep class io.socket.engineio.** { *; }
-keep class io.socket.client.** { *; }
-keep class okio.** { *; }
-dontwarn io.socket.**
-dontwarn org.json.**

# ========== Application Classes ==========
-keep class com.tcc.tarasulandroid.TarasulApplication { *; }
-keep class com.tcc.tarasulandroid.MainActivity { *; }

# ========== R8 Full Mode ==========
-allowaccessmodification
-repackageclasses ''

# ========== Remove Logging in Release ==========
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# ========== Keep Native Methods ==========
-keepclasseswithmembernames class * {
    native <methods>;
}

# ========== Keep Custom Views ==========
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ========== Parcelable ==========
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ========== Serializable ==========
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ========== Enum ==========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
