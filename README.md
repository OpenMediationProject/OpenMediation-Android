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
  implementation 'com.openmediation:om-android-sdk:2.0.0'

  // AdTiming-Adapter
  implementation 'com.openmediation.adapters:adtiming:2.0.0'
  // AdMob-Adapter
  implementation 'com.openmediation.adapters:admob:2.0.0'
  // Facebook-Adapter
  implementation 'com.openmediation.adapters:facebook:2.0.0'
  // Unity-Adapter
  implementation 'com.openmediation.adapters:unity:2.0.0'
  // Vungle-Adapter
  implementation 'com.openmediation.adapters:vungle:2.0.0'
  // AdColony-Adapter
  implementation 'com.openmediation.adapters:adcolony:2.0.0'
  // AppLovin-Adapter
  implementation 'com.openmediation.adapters:applovin:2.0.0'
  // MoPub-Adapter
  implementation 'com.openmediation.adapters:mopub:2.0.0'
  // Tapjoy-Adapter
  implementation 'com.openmediation.adapters:tapjoy:2.0.0'
  // Chartboost-Adapter
  implementation 'com.openmediation.adapters:chartboost:2.0.0'
  // Mintegral-Adapter
  implementation 'com.openmediation.adapters:mintegral:2.0.0'
  //TikTok-Adapter
  implementation 'com.openmediation.adapters:tiktok:2.0.0'
  //IronSource-Adapter
  implementation 'com.openmediation.adapters:ironsource:2.0.0'
  //Fyber-Adapter
  implementation 'com.openmediation.adapters:fyber:2.0.0'
  //CrossPromotion-Adapter
  implementation 'com.openmediation.adapters:crosspromotion:1.0.0'
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


