// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.nbmediation.sdk.utils.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * The type Gdpr util.
 */
public final class GdprUtil {
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";

    /**
     * Developer sets the value of context for SDK to receive
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isGdprSubjected(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String subjectToGdpr = preferences.getString(SUBJECT_TO_GDPR, "-1");
        return TextUtils.equals(subjectToGdpr, "1");
    }

}
