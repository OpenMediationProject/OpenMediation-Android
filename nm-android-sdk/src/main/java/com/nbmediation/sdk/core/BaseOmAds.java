// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.core;

import com.nbmediation.sdk.utils.AdRateUtil;
import com.nbmediation.sdk.utils.PlacementUtils;
import com.nbmediation.sdk.utils.SceneUtil;
import com.nbmediation.sdk.utils.event.EventId;
import com.nbmediation.sdk.utils.event.EventUploadManager;
import com.nbmediation.sdk.utils.model.Placement;
import com.nbmediation.sdk.utils.model.Scene;

/**
 * The type Base om ads.
 */
public abstract class BaseOmAds {
    /**
     * Returns whether this scene is capped
     *
     * @param scene scene name
     * @return true or false
     */
    public static boolean isSceneCapped(String scene) {
        return false;
    }

    /**
     * Is scene capped boolean.
     *
     * @param adType    the ad type
     * @param sceneName the scene name
     * @return the boolean
     */
    protected static boolean isSceneCapped(int adType, String sceneName) {
        Placement placement = PlacementUtils.getPlacement(adType);
        Scene scene = SceneUtil.getScene(placement, sceneName);
        boolean isCapped = AdRateUtil.shouldBlockScene(placement != null ? placement.getId() : "", scene);
        if (isCapped) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_TRUE,
                    SceneUtil.sceneReport(placement != null ? placement.getId() : "", scene));
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_FALSE,
                    SceneUtil.sceneReport(placement != null ? placement.getId() : "", scene));
        }
        return isCapped;
    }

    /**
     * Is scene capped boolean.
     *
     * @param placementId the placement id
     * @param sceneName   the scene name
     * @return the boolean
     */
    static boolean isSceneCapped(String placementId, String sceneName) {
        Placement placement = PlacementUtils.getPlacement(placementId);
        Scene scene = SceneUtil.getScene(placement, sceneName);
        boolean isCapped = AdRateUtil.shouldBlockScene(placement != null ? placement.getId() : "", scene);
        if (isCapped) {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_TRUE,
                    SceneUtil.sceneReport(placement != null ? placement.getId() : "", scene));
        } else {
            EventUploadManager.getInstance().uploadEvent(EventId.CALLED_IS_CAPPED_FALSE,
                    SceneUtil.sceneReport(placement != null ? placement.getId() : "", scene));
        }
        return isCapped;
    }

    /**
     * Returns SceneInfo with given scene
     *
     * @param scene scene name
     * @return the scene info
     */
    public static Scene getSceneInfo(String scene) {
        return null;
    }

    /**
     * Gets scene info.
     *
     * @param adType the ad type
     * @param scene  the scene
     * @return the scene info
     */
    protected static Scene getSceneInfo(int adType, String scene) {
        Placement placement = PlacementUtils.getPlacement(adType);
        return SceneUtil.getScene(placement, scene);
    }
}
