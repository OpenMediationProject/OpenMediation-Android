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
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.qq.e.** {
       *;
    }
    -keep class android.support.v4.**{
        public *;
    }
    -keep class android.support.v7.**{
        public *;
    }
-keep class MTT.ThirdAppInfoNew {
         *;
     }
-keep class com.tencent.** {
         *;
      }
-keep class com.baidu.mobads.*.** { *; }
-keep class com.baidu.mobad.** { *; }

-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.androidquery.callback.** {*;}
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.ss.sys.ces.a {*;}
-keep class com.ss.android.socialbase.** {*;}

-keep class pl.droidsonroids.gif.sample.GifSelectorDrawable { *; }

-keep class com.baidu.mobads.*.** { *; }

-keep class sun.misc.Unsafe { *; }
-dontwarn com.sigmob.**
-keep class com.sigmob.**.**{*;}

-keep class btmsdkobf.** { *; }
-keep class com.tmsdk.** { *; }
-keep class tmsdk.** { *; }
#in---------start----------
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-keep public class com.google.android.gms.**
-dontwarn com.google.android.gms.**
-dontwarn com.squareup.picasso.**
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient{public *;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info{public *;}
#skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
#skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
#skip AVID classes
-keep class com.integralads.avid.library.** {*;}
#in---------end----------
-keep class com.bun.** {*;}
-keep class com.android.ag.**{*;}
-keep class com.tencent.**{*;}
-keep class com.kwad.sdk.** { *;}
-keep class com.ksad.download.** { *;}
-keep class com.kwai.filedownloader.** { *;}

-keep class com.hytt.** { *; }
-keep interface com.hytt.** { *; }

-keep class cn.vlion.ad.**{*;}
-keep class show.vion.cn.vlion_ad_inter.**{*;}
#忽略警告
-ignorewarning