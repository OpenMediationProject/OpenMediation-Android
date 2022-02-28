/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.utils.lifecycle;

import android.util.Base64;

import com.openmediation.sdk.utils.constant.CommonConstants;

import java.nio.charset.Charset;

public class TypeConstant {
    @SuppressWarnings("CharsetObjectCanBeUsed")
    final static String[] ADS_ACT = new String[]{
            new String(Base64.decode("Y29tLmFkdGJpZC5zZGs=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmdvb2dsZS5hbmRyb2lkLmdtcy5hZHM=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmZhY2Vib29rLmFkcw==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnVuaXR5M2Quc2VydmljZXMuYWRz", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnZ1bmdsZQ==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmFkY29sb255", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmFwcGxvdmlu", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLm1vcHVi", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnRhcGpveQ==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmNoYXJ0Ym9vc3Q=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmJ5dGVkYW5jZS5zZGs=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmlyb25zb3VyY2Uuc2RrLmNvbnRyb2xsZXI=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLm15LnRhcmdldA==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLm1icmlkZ2UubXNkay4=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmNoYXJ0Ym9vc3RfaGVsaXVtLnNkaw==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnFxLmUuYWRz", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnNpZ21vYi53aW5kYWQ=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmt3YWQuc2RrLmFwaS5wcm94eQ==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("aW8ucHJlc2FnZS5pbnRlcnN0aXRpYWwudWk=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("bmV0LnB1Ym5hdGl2ZS5saXRlLnNkaw==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmNyb3NzcHJvbW90aW9uLnNkay4=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("YWRtb3N0LnNkay4=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("YWRtb3N0LmFkc2VydmVyLg==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmZsYXRhZHMuc2RrLnVp", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmlubW9iaS5hZHMucmVuZGVyaW5n", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmh1YXdlaS5vcGVuYWxsaWFuY2UuYWQuYWN0aXZpdHk=", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
    };
}
