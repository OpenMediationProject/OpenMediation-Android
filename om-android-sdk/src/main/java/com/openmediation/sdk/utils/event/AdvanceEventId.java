/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.utils.event;

public interface AdvanceEventId {

    int CODE_LOAD_WHILE_INIT_PENDING = 10000;
    String MSG_LOAD_WHILE_INIT_PENDING = "This load occurs while init pending";

    int CODE_LOAD_WHILE_NOT_INIT = 10001;
    String MSG_LOAD_WHILE_NOT_INIT = "This load occurs while not init or init failed";

    int CODE_BID_RESPONSE_EXPIRED = 10002;
    String MSG_BID_RESPONSE_EXPIRED = "Bid response is expired";

    int CODE_WF_RESPONSE_DATA = 10003;
    String MSG_WF_RESPONSE_DATA = "Waterfall response data";

    int CODE_WF_RESPONSE_EXCEPTION = 10004;
    String MSG_WF_RESPONSE_EXCEPTION = "Waterfall response exception";

    int CODE_WF_RESPONSE_FAILED = 10005;
    String MSG_WF_RESPONSE_FAILED = "Waterfall response failed";

    int CODE_NOTIFY_WIN_RES_NULL = 10006;
    String MSG_NOTIFY_WIN_RES_NULL = "Notify win failed: instance response is null";

    int CODE_NOTIFY_LOSE_RES_NULL = 10007;
    String MSG_NOTIFY_LOSE_RES_NULL = "Notify lose failed: instance response is null";

    int CODE_INS_DESTROY = 10008;
    String MSG_INS_DESTROY = "Instance destroy";

    int CODE_S2S_BID_COMPLETED = 10009;
    String MSG_S2S_BID_COMPLETED = "S2S bid is completed";

    int CODE_TOTAL_INS_NULL = 10010;
    String MSG_TOTAL_INS_NULL = "Total instance list is null";

    int CODE_INVENTORY_IS_FULL = 10011;
    String MSG_INVENTORY_IS_FULL = "After waterfall, Inventory is full";

    int CODE_WF_CODE_ERROR = 10012;
    String MSG_WF_CODE_ERROR = "Waterfall success but response code not 0";

    int CODE_WF_RESPONSE_ERROR = 10013;
    String MSG_WF_RESPONSE_ERROR = "Waterfall response error: ";

    int CODE_HAS_READY_INSTANCE = 10014;
    String MSG_HAS_READY_INSTANCE = "HybridAds has ready instance";

    int CODE_LOAD_INS_INDEX_OOB = 10015;
    String MSG_LOAD_INS_INDEX_OOB = "HybridAds instance index out of bounds: ";

    int CODE_INS_LOAD_EXP = 10016;
    String MSG_INS_LOAD_EXP = "HybridAds instance load exception: ";

    int CODE_NEXT_INS_LOAD_EXP = 10017;
    String MSG_NEXT_INS_LOAD_EXP = "HybridAds startNextInstance error: ";

    int CODE_INS_GROUP_LOAD_TIMEOUT = 10018;
    String MSG_INS_GROUP_LOAD_TIMEOUT = "HybridAds group ins load timeout: ";

    int CODE_INS_GROUP_LOAD_FAILED = 10019;
    String MSG_INS_GROUP_LOAD_FAILED = "HybridAds group ins load failed";

    int CODE_RE_INIT_ERROR = 10020;
    String MSG_RE_INIT_ERROR = "SDK re init error: ";

    int CODE_START_LOAD_ERROR = 10021;
    String MSG_START_LOAD_ERROR = "Start load ad error: ";

    int CODE_CAN_NOT_LOAD = 10022;
    String MSG_CAN_NOT_LOAD = "Not can load ins";

    int CODE_INS_LOAD_LIMIT = 10023;
    String MSG_INS_LOAD_LIMIT = "Ins load limit: ";

    int CODE_S2S_NO_ADAPTER = 10024;
    String MSG_S2S_NO_ADAPTER = "S2S get token error: adapter = null";

    int CODE_S2S_GET_TOKEN_ERROR = 10025;
    String MSG_S2S_GET_TOKEN_ERROR = "S2S get token error: ";

    int CODE_S2S_BID_ERROR = 10026;
    String MSG_S2S_BID_ERROR = "S2S bid error: ";

    int CODE_C2S_NO_ADAPTER = 10027;
    String MSG_C2S_NO_ADAPTER = "C2S bid error: adapter = null";

    int CODE_C2S_BID_ERROR = 10028;
    String MSG_C2S_BID_ERROR = "C2S bid error: ";

    int CODE_C2S_BID_TIMEOUT = 10029;
    String MSG_C2S_BID_TIMEOUT = "C2S bid timeout";

    int CODE_AD_EXPIRED = 10030;
    String MSG_AD_EXPIRED = "Ad is expired";

    int CODE_PLACEMENT_LOAD_DUR = 10031;
    String MSG_PLACEMENT_LOAD_DUR = "Placement load duration: ";

}
