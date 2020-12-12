# OpenMediation SDK for Android
Thanks for taking a look at OpenMediation! We offers diversified and competitive monetization solution and supports a variety of Ad formats including Native Ad, Interstitial Ad, Banner Ad, and Rewarded Video Ad. The OpenMediation platform works with multiple ad networks include AdMob, Facebook, UnityAds, Vungle, AdColony, AppLovin, MoPub, Tapjoy, Chartboost and Mintegral etc.

## Communication

- If you **found a bug**, _and can provide steps to reliably reproduce it_, open an issue.
- If you **have a feature request**, open an issue.

## Installation

```
android {
  ...
  defaultConfig {
        minSdkVersion 16
    }
}

dependencies {
  implementation 'com.adtiming.om:om-android-sdk:1.3.4'

  // AdTiming-Adapter
  implementation 'com.adtiming.adapters.om:adtiming:1.3.2'  
  // AdMob-Adapter
  implementation 'com.adtiming.adapters.om:admob:1.3.2'
  // Facebook-Adapter
  implementation 'com.adtiming.adapters.om:facebook:1.3.2'
  // Unity-Adapter
  implementation 'com.adtiming.adapters.om:unity:1.3.1'
  // Vungle-Adapter
  implementation 'com.adtiming.adapters.om:vungle:1.3.2'
  // AdColony-Adapter
  implementation 'com.adtiming.adapters.om:adcolony:1.3.1'
  // AppLovin-Adapter
  implementation 'com.adtiming.adapters.om:applovin:1.3.2'
  // MoPub-Adapter
  implementation 'com.adtiming.adapters.om:mopub:1.3.1'
  // Tapjoy-Adapter
  implementation 'com.adtiming.adapters.om:tapjoy:1.3.1'
  // Chartboost-Adapter
  implementation 'com.adtiming.adapters.om:chartboost:1.3.2'
  // Mintegral-Adapter
  implementation 'com.adtiming.adapters.om:mintegral:1.4.2'
  //TikTok-Adapter
  implementation 'com.adtiming.adapters.om:tiktok:1.2.1'
  //IronSource-Adapter
  implementation 'com.adtiming.adapters.om:ironsource:1.3.0'
  //Fyber-Adapter
  implementation 'com.adtiming.adapters.om:fyber:1.2.0'
}
```

## ProGuard
```
-keep class com.openmediation.sdk.mediation.** { *; }
-keep class com.openmediation.sdk.mobileads.** { *; }
```

## Requirements
We support Android Operating Systems Version 4.1 (API Level 16) and up. Be sure to:

- Use Android Studio 2.0 and up
- Target Android API level 28
- MinSdkVersion level 16 and up

## LICENSE
See the [LICENSE](LICENSE) file.


