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
-dontusemixedcaseclassnames
-printmapping proguard.map
-keepattributes Signature,InnerClasses,Exceptions,*Annotation*
-keep public class com.applovin.sdk.AppLovinSdk { *; }
-keep public class com.applovin.sdk.AppLovin* { public protected *; }
-keep public class com.applovin.nativeAds.AppLovin* { public protected *; }
-keep public class com.applovin.adview.* { public protected *; }
-keep public class com.applovin.mediation.* { public protected *; }
-keep public class com.applovin.mediation.ads.* { public protected *; }
-keep public class com.applovin.impl.sdk.utils.BundleUtils { public protected *; }
-keep public class com.applovin.impl.**.AppLovin* { public protected *; }
-keep public class com.applovin.impl.**.*Impl { public protected *; }
-keep public class com.applovin.impl.adview.AppLovinOrientationAwareInterstitialActivity { *; }
-keepclassmembers class com.applovin.sdk.AppLovinSdkSettings { private java.util.Map localSettings; }
-keep class com.applovin.mediation.adapters.** { *; }
-keep class com.applovin.mediation.adapter.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }