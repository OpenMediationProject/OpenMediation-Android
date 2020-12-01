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

-keep class com.openmediation.sdk.adn.banner.**{*;}
-keep class com.openmediation.sdk.adn.bid.**{*;}
-keep class com.openmediation.sdk.adn.interstitial.**{*;}
-keep class com.openmediation.sdk.adn.nativead.**{*;}
-keep class com.openmediation.sdk.adn.video.**{*;}
-keep class com.openmediation.sdk.adn.promotion.**{*;}
-keep class com.openmediation.sdk.adn.utils.error.**{*;}
-keep class com.openmediation.sdk.adn.OmAdNetwork{*;}
-keep class com.openmediation.sdk.adn.utils.webview.JsBridge{*;}
-keep class * extends android.app.Activity