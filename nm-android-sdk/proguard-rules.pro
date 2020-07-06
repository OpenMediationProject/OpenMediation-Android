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
#-keepattributes SourceFile,LineNumberTable

-dontskipnonpubliclibraryclasses
# SDK API
-keep class com.nbmediation.sdk.**{*;}
#-keep class com.nbmediation.sdk.mediation.**{*;}
#
## Mediation interface
#-keep class com.nbmediation.sdk.mobileads.**{*;}
#
## u3d interface
#-keep class com.nbmediation.sdk.api.**{*;}
#
#-keep class com.nbmediation.sdk.nativead.**{*;}

-dontwarn androidx.**

 #R
 -keepclassmembers class **.R$* {
     public static <fields>;
 }
 -keepattributes *Annotation*,InnerClasses
 -keepnames class * implements android.os.Parcelable {
     public static final ** CREATOR;
}
