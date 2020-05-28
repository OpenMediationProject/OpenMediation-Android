package com.openmediation.sdk.bid;

public enum BidLoseReason {
    /**
     *
     */
    INTERNAL(1),
    TIMEOUT(2),
    INVALID_RESPONSE(3),
    LOST_TO_HIGHER_BIDDER(102),
    INVENTORY_DID_NOT_MATERIALISE(4902);

    private final int reason;

    private BidLoseReason(int reason) {
        this.reason = reason;
    }

    public int getValue() {
        return this.reason;
    }
}
