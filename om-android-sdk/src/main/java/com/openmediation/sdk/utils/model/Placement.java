// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.model;

import android.util.SparseArray;

import java.util.Map;

/**
 *
 */
public class Placement extends Frequency {
    //id
    private String id;
    // Placement Name
    private String name;
    //type
    private int t;

    private int main;

    //Array of Scenes
    private Map<String, Scene> scenes;
    //max seconds for loading one AdNetwork     30
    private int pt;
    //batchSize for Instance, for Banner&Native   2
    private int bs;
    //Fan Out flag, immediate Ready mode, for Banner&Native     1
    private int fo;
    //cache size: # of ready in stock     3
    private int cs;
    //refresh in how many seconds for RewardVideo     30
    private int rf;
    private Map<Integer, Integer> rfs;
    //Mediation placement data
    private SparseArray<BaseInstance> insMap;

    private int rlw;

    private String oriData;

    private boolean hasHb;
    private int hbAbt;
    private int wfAbt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHbAbt(int hbAbt) {
        this.hbAbt = hbAbt;
    }

    public int getHbAbt() {
        return hbAbt;
    }

    public void setWfAbt(int wfAbt) {
        this.wfAbt = wfAbt;
    }

    public int getWfAbt() {
        return wfAbt;
    }

    public void setOriData(String oriData) {
        this.oriData = oriData;
    }

    public String getOriData() {
        return oriData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public SparseArray<BaseInstance> getInsMap() {
        return insMap;
    }

    public void setInsMap(SparseArray<BaseInstance> insMap) {
        this.insMap = insMap;
    }

    public Map<String, Scene> getScenes() {
        return scenes;
    }

    public void setScenes(Map<String, Scene> scenes) {
        this.scenes = scenes;
    }

    public int getCs() {
        return cs;
    }

    public void setCs(int cs) {
        this.cs = cs;
    }

    public int getRf() {
        return rf;
    }

    public void setRf(int rf) {
        this.rf = rf;
    }

    public void setRfs(Map<Integer, Integer> rfs) {
        this.rfs = rfs;
    }

    public Map<Integer, Integer> getRfs() {
        return rfs;
    }

    public int getPt() {
        return pt;
    }

    public void setPt(int pt) {
        this.pt = pt;
    }

    public int getBs() {
        return bs;
    }

    public void setBs(int bs) {
        this.bs = bs;
    }

    public int getFo() {
        return fo;
    }

    public void setFo(int fo) {
        this.fo = fo;
    }

    public void setMain(int main) {
        this.main = main;
    }

    public int getMain() {
        return main;
    }

    public void setRlw(int rlw) {
        this.rlw = rlw;
    }

    public int getRlw() {
        return rlw;
    }

    public void setHasHb(boolean hasHb) {
        this.hasHb = hasHb;
    }

    public boolean hasHb() {
        return hasHb;
    }

    @Override
    public String toString() {
        return "Placement{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", t=" + t +
                '}';
    }
}
