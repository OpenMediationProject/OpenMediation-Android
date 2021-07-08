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

# ADMOST
-keepattributes Exceptions, InnerClasses
-dontwarn admost.sdk.**
-keep class admost.sdk.** {*;}
-dontwarn com.amr.unity.**
-keep class com.amr.unity.** {*;}
-dontwarn admost.adserver.**
-keep class com.adjust.sdk.** {*;}
-dontwarn com.adjust.sdk.**
-keep class com.appsflyer.** {*;}
-dontwarn com.appsflyer.**
-keep class admost.adserver.** { *; }
-dontwarn com.google.android.exoplayer2.**
-keep class com.google.android.exoplayer2.**{ *;}
-keep class android.support.v4.app.DialogFragment { *; }
-keep class android.support.v4.util.LruCache { *; }


# VOLLEY
-keep class com.android.volley.** { *; }
-keep interface com.android.volley.** { *; }
-keep class org.apache.commons.logging.**

# # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

# ADCOLONY
-dontwarn com.adcolony.**
-keep class com.adcolony.** { *; }
-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
-keepclassmembers class com.adcolony.sdk.ADCNative** { *; }

# ADGEM
-keep class com.adgem.** { *; }
-dontwarn com.adgem.**

# ADMOB / ADX / GOOGLE
-keep class com.android.vending.billing.**
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable { public static final *** CREATOR; }
-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * { @android.support.annotation.Keep <fields>; }
-keepclasseswithmembers class * { @android.support.annotation.Keep <methods>; }
-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }
-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * { public <fields>; public <methods>; }
-keep public @com.google.android.gms.common.util.DynamiteApi class * { *; }
-keep class com.google.android.apps.common.proguard.UsedBy*
-keep @com.google.android.apps.common.proguard.UsedBy* class *
-keepclassmembers class * { @com.google.android.apps.common.proguard.UsedBy* *; }
-dontwarn android.security.NetworkSecurityPolicy
-dontwarn android.app.Notification

#ADTIMING
-dontwarn com.aiming.mdt.**.*
-dontoptimize
-dontskipnonpubliclibraryclasses
-keepattributes *Annotation*
#adt
-keep class com.aiming.mdt.**{ *; }
-keepattributes *Annotation*,InnerClasses
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}
#R
-keepclassmembers class **.R$* {
public static <fields>;
}

# AMAZON
-dontwarn com.amazon.**
-keep class com.amazon.** { *; }
-dontwarn org.apache.http.**
-keepattributes *Annotation*

# APPLOVIN
-dontwarn com.applovin.**
-keep class com.applovin.** { *; }

# APPNEXT
-keep class com.appnext.** { *; }
-dontwarn com.appnext.**

# APPSAMURAI
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# AVOCARROT - GLISPA
-keep class com.avocarrot.** { *; }
-dontwarn com.avocarrot.**
-keep class com.google.android.exoplayer2.SimpleExoPlayer
-dontwarn com.google.android.exoplayer2.SimpleExoPlayer

# CHARTBOOST
-dontwarn org.apache.http.**
-dontwarn com.chartboost.sdk.impl.**
-keep class com.chartboost.sdk.** { *; }
-keepattributes *Annotation*

# CRITEO
-dontwarn com.criteo.**
-keep class com.criteo.** { *; }

# DISPLAYIO
-keep class io.display.sdk.** { *; }
-dontwarn io.display.sdk.**
-keep class com.brandio.ads.** { *;}
-dontwarn com.brandio.ads.**

# FACEBOOK
-dontwarn com.facebook.ads.**
-dontnote com.facebook.ads.**
-keep class com.facebook.** { *; }
-keepattributes Signature
-keep class com.google.android.exoplayer2.** {*;}
-dontwarn com.google.android.exoplayer2.**

# FLURRY
-keep class com.flurry.** { *; }
-dontwarn com.flurry.**
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepclasseswithmembers class * { public <init>(android.content.Context, android.util.AttributeSet, int); }
-keep class * extends java.util.ListResourceBundle { protected Object[][] getContents(); }
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }
-keepnames class * implements android.os.Parcelable { public static final ** CREATOR; }

# FREECORP
-keep class com.fly.** { *; }
-dontwarn com.fly.**

# FYBER
-keep class com.fyber.** { *; }
-dontwarn com.fyber.**
-keep class com.fyber.mediation.MediationConfigProvider { public static *; }
-keep class com.fyber.mediation.MediationAdapterStarter { public static *; }
-keepclassmembers class com.fyber.ads.videos.mediation.** { void setValue(java.lang.String); }

# HUAWEI-ADS
-keep class com.huawei.openalliance.ad.** { *; }
-keep class com.huawei.hms.ads.** { *; }

# INMOBI
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** {*;}
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{public *;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{public *;}
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
-keep class com.moat.** {*;}
-dontwarn com.moat.**
-keep class com.integralads.avid.library.* {*;}


# INNERACTIVE
-dontwarn com.fyber.**
-keep class com.fyber.** {*;}
-dontwarn com.inneractive.api.ads.**
-keep class com.inneractive.api.ads.** {*;}
-keepclassmembers class com.inneractive.api.ads.** {*;}
-keepclassmembers class com.inneractive.api.ads.sdk.nativead.** {*;}
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.Gson { *; }
-keep class com.google.gson.GsonBuilder { *; }
-keep class com.google.gson.FieldNamingStrategy { *; }

# IRONSOURCE
-dontwarn com.ironsource.**
-keep class com.ironsource.** { *; }
-keepclassmembers class * implements android.os.Parcelable { public static final android.os.Parcelable$Creator *; }
-keep public class com.google.android.gms.ads.** { public *; }
-keep class com.ironsource.adapters.** { *; }
-dontwarn com.moat.**
-keep class com.moat.** { public protected private *; }

# LOOPME
-dontwarn com.loopme.**
-keep class com.loopme.** { *; }

# MEDIABRIX
-keep class com.mediabrix.** { *; }
-keep class com.moat.** { *; }
-keep class mdos.** { *; }
-dontwarn com.mediabrix.**
-dontwarn com.moat.**
-dontwarn mdos.**

# MILLENNIAL & NEXAGE
-keep class com.millennialmedia.** { *; }
-dontwarn com.millennialmedia.**

# MINTEGRAL
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mbridge.** {*; }
-keep interface com.mbridge.** {*; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.** { *; }
-dontwarn com.mbridge.**
-keep class **.R$* { public static final int mbridge*; }

# MOBFOX
-dontwarn com.mobfox.**
-keep class com.mobfox.** { *; }
-keep class com.mobfox.adapter.** { *; }
-keep class com.mobfox.sdk.** { *; }

# MOPUB
-keepclassmembers class com.mopub.** { public *; }
-dontnote com.mopub.**
-dontwarn com.mopub.**
-keep public class com.mopub.**
-keep class com.mopub.mobileads.** { *; }
-keep class * extends com.mopub.mobileads.CustomEventBanner {}
-keep class * extends com.mopub.mobileads.CustomEventInterstitial {}
-keep class * extends com.mopub.nativeads.CustomEventNative {}
-keep class * extends com.mopub.nativeads.CustomEventRewardedAd {}
-keepclassmembers class ** { @com.mopub.common.util.ReflectionTarget *; }
-keepclassmembers class com.integralads.avid.library.mopub.** { public *; }
-keep public class com.integralads.avid.library.mopub.**
-keepclassmembers class com.moat.analytics.mobile.mpub.** { public *; }
-keep public class com.moat.analytics.mobile.mpub.**
-keep class com.google.android.gms.common.GooglePlayServicesUtil { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient { *; }
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info { *; }
-keep class * extends java.util.ListResourceBundle { protected Object[][] getContents(); }
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable { public static final *** NULL; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * { @com.google.android.gms.common.annotation.KeepName *; }
-keepnames class * implements android.os.Parcelable { public static final ** CREATOR; }
-keep public class android.webkit.JavascriptInterface {}

# MYTARGET
-keep class com.my.target.** {*;}

# NEND
-keep class net.nend.android.** { *; }
-dontwarn net.nend.android.**

# NATIVEX
-dontwarn com.nativex.**
-keep class com.nativex.** { *; }

# OGURY
-dontwarn com.ogury.**
-keep class com.ogury.** { *; }

# OUTBID
-dontwarn outbid.com.outbidsdk.**
-keep class outbid.com.outbidsdk.** { *; }

# QUMPARA
-dontwarn com.qumpara.analytics.**
-keep class com.qumpara.analytics.** { *; }
-dontwarn com.qumpara.offerwall.sdk.**
-keep class com.qumpara.offerwall.sdk.** { *; }

#POLLFISH
-dontwarn com.pollfish.**
-keep class com.pollfish.** { *; }

# PUBNATIVE
-keepattributes Signature
-keep class net.pubnative.** { *; }
-dontwarn net.pubnative.**
-keep class com.squareup.picasso.** { *; }
-dontwarn com.squareup.picasso.**

# REVMOB
-dontwarn rm.com.android.sdk.**
-keep class rm.com.android.sdk.** { public *; }

# RETROFIT
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# SMAATO
-dontwarn com.smaato.**
-keep class com.smaato.** { public *; }
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-keep public class com.smaato.soma.internal.connector.OrmmaBridge { public *; }
-keepattributes *Annotation*
-dontwarn com.smaato.soma.SomaUnityPlugin*
-dontwarn com.millennialmedia**
-dontwarn com.facebook.**

# SMARTAD
-keep class com.smartadserver.** { *; }
-dontwarn com.smartadserver.**


# STARTAPP
-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile, LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn android.webkit.JavascriptInterface
-keep class com.startapp.** { *; }
-dontwarn com.startapp.**

# TAPJOY
-keep class com.tapjoy.** {*;}
-keep class com.moat.** {*;}
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keep class * extends java.util.ListResourceBundle {protected Object[][] getContents();}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {public static final *** NULL;}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {@com.google.android.gms.common.annotation.KeepName *;}
-keepnames class * implements android.os.Parcelable {public static final ** CREATOR;}
-keep class com.google.android.gms.ads.identifier.** {*;}
-dontwarn com.tapjoy.**

# TAPPX
-keepattributes *Annotation*
-keepclassmembers class com.google.**.R$* { public static <fields>; }
-keep public class com.google.ads.** {*;}
-keep public class com.google.android.gms.** {*;}
-keep public class com.tappx.** { *; }
-dontwarn com.tappx.**

#TIKTOK
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.** {*;}
-keep class com.bytedance.embed_dr.** {*;}
-keep class com.bytedance.embedapplog.** {*;}

# UNITY
-dontwarn com.unity3d.**
-keep class com.unity3d.ads.** { *; }

# Vungle
-dontwarn com.vungle.**
-dontnote com.vungle.**
-keep class com.vungle.** { *; }
-dontwarn com.vungle.warren.error.VungleError$ErrorCode
-keep class com.moat.** { *; }
-dontwarn com.moat.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.ads.identifier.**

-keepclassmembers enum com.vungle.warren.** { *; }
-dontwarn kotlin.Unit
-dontwarn retrofit2.-KotlinExtensions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepclassmembers class * extends com.vungle.warren.persistence.Memorable {
   public <init>(byte[]);
}

#YANDEX
-dontwarn com.yandex.**
-keep class com.yandex.** { *; }

# YOUAPPI
-keep class com.google.gson.**{ *;}
-keep class com.google.android.gms.**{*;}
-keep class com.youappi.sdk.**{*;}
-keep interface com.youappi.sdk.**{*;}
-keep enum com.youappi.sdk.**{*;}
-keepclassmembers class * {
   @android.webkit.JavascriptInterface <methods>;
}

# VERIZON
-keepclassmembers class com.verizon.ads** {
public *;
}
-keep class com.verizon.ads**