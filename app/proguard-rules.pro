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

-keep class com.tcc.tarasulandroid.data.** { *; }
-keepclassmembers class com.tcc.tarasulandroid.data.** { *; }

# Keep DataStore related classes
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }
# Keep DataStore extension
-keep class com.tcc.tarasulandroid.di.DataStoreModuleKt { *; }
-keepclassmembers class com.tcc.tarasulandroid.di.DataStoreModuleKt { *; }

# Keep the extension property
-keepnames class com.tcc.tarasulandroid.di.DataStoreModuleKt
# Keep all Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class dagger.hilt.** { *; }
-keep class **Dagger** { *; }

# Keep all your app classes
-keep class com.tcc.tarasulandroid.** { *; }
-keep interface com.tcc.tarasulandroid.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }