// Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk;

import com.openmediation.sdk.utils.error.Error;

/**
 * ImpressionListener is an interface to notify the application about ad impressions for all
 * ad formats.
 */
public interface ImpressionDataListener {
  /**
   * SDK will call method onImpression once the ad becomes visible for the first time.
   * @param error - If impression level revenue data is enabled for this account,
   *                this value will be null, otherwise not null.
   *
   * @param impressionData - extended information about the ad including revenue per impression.
   *                       This value can be null if impression level revenue data is not enabled
   *                       for this account.
   */
  void onImpression(Error error, ImpressionData impressionData);
}
