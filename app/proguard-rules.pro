# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class n to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file n.
#-renamesourcefileattribute SourceFile
-keep public class com.suib.**{*;}
-dontwarn com.suib.**

#for gaid
-keep class **.AdvertisingIdClient$** { *; }

#for js and webview interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}