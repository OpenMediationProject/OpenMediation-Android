/*
 * // Copyright 2021 ADTIMING TECHNOLOGY COMPANY LIMITED
 * // Licensed under the GNU Lesser General Public License Version 3
 */

package com.openmediation.sdk.inspector;

public interface LogConstants {

    int LOG_OTHER = -1;
    int LOG_TAG_INIT = 1;
    int LOG_TAG_INVENTORY = 2;
    int LOG_TAG_SETTINGS = 3;
    int LOG_TAG_WATERFALL = 4;

    int INIT_SUCCESS = 1001;
    int INIT_FAILED = 1002;

    int INVENTORY_IN = 2001;
    int INVENTORY_OUT = 2002;

    int CUSTOM_TAG_CHANGE = 3001;
    int CUSTOM_TAG_REMOVE = 3003;
    int CUSTOM_TAG_CLEAR = 3004;
    int USER_ID_CHANGE = 3002;

    int WF_SUCCESS = 4001;
    int WF_FAILED = 4002;

    int INS_FILL = 5001;
    int INS_LOAD_FAILED = 5002;
    int INS_PAYLOAD_SUCCESS = 5003;
    int INS_PAYLOAD_FAILED = 5004;
}
