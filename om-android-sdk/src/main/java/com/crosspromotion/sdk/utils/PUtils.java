// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;

import com.crosspromotion.sdk.ActionActivity;
import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.utils.webview.ActWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebView;
import com.crosspromotion.sdk.utils.webview.BaseWebViewClient;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.HandlerUtil;
import com.openmediation.sdk.utils.PlacementUtils;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.cache.DataCache;
import com.openmediation.sdk.utils.crash.CrashUtil;
import com.openmediation.sdk.utils.model.ImpRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public final class PUtils extends PlacementUtils {

    /**
     * Ads click
     */
    public static void doClick(final Context context, final String placementId, final AdBean adBean) {
        try {
            saveClickPackage(adBean.getPkgName());
            if (!adBean.isWebView()) {
                String landingUrl = "market://details?id=" + adBean.getPkgName();
                GpUtil.goGp(context, landingUrl);
                HandlerUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BaseWebView adView = ActWebView.getInstance().getActView();
                            if (adView == null) {
                                adView = new BaseWebView(context);
                            }
                            final Map<String, String> additionalHttpHeaders = new HashMap<>();
                            additionalHttpHeaders.put("Cache-Control", "no-cache");
                            adView.setWebViewClient(new BaseWebViewClient(context, adBean.getPkgName()) {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    super.shouldOverrideUrlLoading(view, url);
                                    Uri dst = Uri.parse(url);
                                    String scheme = dst.getScheme();
                                    if ("market".equals(scheme)) {
                                        String query = dst.getEncodedQuery();
                                        view.loadUrl("https://play.google.com/store/apps/details?" + query);
                                    } else {
                                        view.loadUrl(url, additionalHttpHeaders);
                                    }
                                    return true;
                                }
                            });
                            int sceneId = SceneUtil.getSceneId(placementId);
                            String adUrl = adBean.getAdUrl();
                            if (adUrl.contains("{scene}")) {
                                adUrl = adUrl.replace("{scene}", sceneId + "");
                            }
                            adView.loadUrl(adUrl, additionalHttpHeaders);
                        } catch (Throwable e) {
                            DeveloperLog.LogD("AdReport", e);
                            CrashUtil.getSingleton().saveException(e);
                        }
                    }
                });
            } else {
                Intent intent = new Intent(context, ActionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("adBean", AdBean.toJsonString(adBean));
                intent.putExtra("placementId", placementId);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            DeveloperLog.LogD("AdReport", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    public static void saveClInfo(AdBean adBean, String placementId) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = new Date();

            String today = dateFormat.format(date);

            ImpRecord impRecord = parseFromJson(DataCache.getInstance().get("ImpRecord", String.class));
            if (impRecord == null) {
                impRecord = new ImpRecord();
            }

            Map<String, Map<String, ImpRecord.Imp>> imps = impRecord.getImpMap();
            if (imps == null) {
                imps = new HashMap<>();
            }
            String tpmKey = placementId.trim().concat("_imp");
            Map<String, ImpRecord.Imp> imprs = imps.get(tpmKey);
            if (imprs == null || imprs.isEmpty()) {
                imprs = new HashMap<>();
                ImpRecord.Imp imp = new ImpRecord.Imp();
                imp.setPlacmentId(placementId);
                imp.setTime(today);
                imp.setImpCount(imp.getImpCount() + 1);
                imp.setPkgName(adBean.getPkgName());
                imp.setLashImpTime(System.currentTimeMillis());
                imprs.put(adBean.getPkgName(), imp);
            } else {
                if (imprs.toString().contains(adBean.getPkgName())) {
                    for (Map.Entry<String, ImpRecord.Imp> impr : imprs.entrySet()) {
                        if (impr.getValue() == null) {
                            continue;
                        }
                        if (TextUtils.equals(impr.getValue().getPkgName(), adBean.getPkgName())) {
                            impr.getValue().setPlacmentId(placementId);
                            impr.getValue().setTime(today);
                            impr.getValue().setImpCount(impr.getValue().getImpCount() + 1);
                            impr.getValue().setPkgName(adBean.getPkgName());
                            impr.getValue().setLashImpTime(System.currentTimeMillis());
                            imprs.put(adBean.getPkgName(), impr.getValue());
                            break;
                        }
                    }
                } else {
                    ImpRecord.Imp imp = new ImpRecord.Imp();
                    imp.setPlacmentId(placementId);
                    imp.setTime(today);
                    imp.setImpCount(imp.getImpCount() + 1);
                    imp.setPkgName(adBean.getPkgName());
                    imp.setLashImpTime(System.currentTimeMillis());
                    imprs.put(adBean.getPkgName(), imp);
                }
            }

            imps.put(tpmKey, imprs);
            impRecord.setImpMap(imps);

            DataCache.getInstance().set("ImpRecord", Uri.encode(transformToString(impRecord)));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

    private static void saveClickPackage(String pkg) {
        try {
            DataCache.getInstance().set(pkg, String.valueOf(System.currentTimeMillis()));
        } catch (Throwable e) {
            DeveloperLog.LogD("PlacementUtils", e);
            CrashUtil.getSingleton().saveException(e);
        }
    }

}
