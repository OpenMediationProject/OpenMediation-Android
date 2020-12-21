// Copyright 2019 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.crosspromotion.sdk.report;

import android.content.Context;

import com.crosspromotion.sdk.bean.AdBean;
import com.crosspromotion.sdk.utils.PUtils;
import com.openmediation.sdk.utils.SceneUtil;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.request.network.AdRequest;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public final class AdReport {

    private static ConcurrentLinkedQueue<AdBean> sImpReport = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<AdBean> sClickReport = new ConcurrentLinkedQueue<>();

    /**
     * Click reporting
     */
    public static void CLKReport(Context context, String placementId, AdBean adBean) {
        if (adBean == null) {
            return;
        }

        if (sClickReport.contains(adBean)) {
            return;
        }

        List<String> clks = adBean.getClktrackers();
        if (clks == null || clks.isEmpty()) {
            return;
        }

        int sceneId = SceneUtil.getSceneId(placementId);

        for (String tracker : clks) {
            if (tracker.contains("{scene}")) {
                tracker = tracker.replace("{scene}", sceneId + "");
            }
            AdRequest.get().url(tracker).readTimeout(60000).connectTimeout(30000)
                    .instanceFollowRedirects(true).performRequest(context);
        }

        sClickReport.add(adBean);
    }

    /**
     * Impression reporting
     */
    public static void impReport(Context context, String placementId, AdBean adBean) {

        if (adBean == null) {
            return;
        }

        if (sImpReport.contains(adBean)) {
            return;
        }
        saveCLImprInfo(placementId, adBean);

        List<String> impTrackers = adBean.getImptrackers();
        if (impTrackers == null || impTrackers.isEmpty()) {
            return;
        }

        int sceneId = SceneUtil.getSceneId(placementId);

        for (String tracker : impTrackers) {
            if (tracker.contains("{scene}")) {
                tracker = tracker.replace("{scene}", sceneId + "");
            }
            AdRequest.get().url(tracker).readTimeout(60000).connectTimeout(30000)
                    .instanceFollowRedirects(true).performRequest(context);
        }

        sImpReport.add(adBean);
    }

    private static void saveCLImprInfo(final String placementId, final AdBean adBean) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adBean == null) {
                    return;
                }
                PUtils.saveClInfo(adBean, placementId);
            }
        };

        WorkExecutor.execute(runnable);
    }
}
