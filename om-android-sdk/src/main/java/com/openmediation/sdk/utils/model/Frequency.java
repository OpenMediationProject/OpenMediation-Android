// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

public class Frequency {
    //0: not capped; n: n impressions in fu hours 
    private int fc;
    //interval in hours
    private int fu;
    //minimum interval between two impressions, in seconds
    private int fi;

    public int getFrequencyCap() {
        return fc;
    }

    public void setFrequencyCap(int frequency_cap) {
        this.fc = frequency_cap;
    }

    public int getFrequencyUnit() {
        return fu;
    }

    public void setFrequencyUnit(int frequency_unit) {
        this.fu = frequency_unit;
    }

    public int getFrequencyInterval() {
        return fi;
    }

    public void setFrequencyInterval(int frequency_interval) {
        this.fi = frequency_interval;
    }
}
