package com.openmediation.sdk.utils.lifecycle;

import android.util.Base64;

import com.openmediation.sdk.utils.constant.CommonConstants;

import java.nio.charset.Charset;

public class TypeConstant {

    @SuppressWarnings("CharsetObjectCanBeUsed")
    final static String[] ADS_ACT = new String[]{
            new String(Base64.decode("Y29tLmFkdGltaW5nLm1lZGlhdGlvbnNkaw==", Base64.NO_WRAP),
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
            new String(Base64.decode("Y29tLm1pbnRlZ3JhbC5tc2Rr", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLmNoYXJ0Ym9vc3RfaGVsaXVtLnNkaw==", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8)),
            new String(Base64.decode("Y29tLnFxLmUuYWRz", Base64.NO_WRAP),
                    Charset.forName(CommonConstants.CHARTSET_UTF8))
    };
}
