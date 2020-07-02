package com.cloudtech.shell.entity;

import com.cloudtech.shell.Constants;

/**
 * Created by jiantao.tu on 2018/5/23.
 */
public class ModulePo {

    private Integer id = null;

    private String moduleName;

    private String version;

    private boolean isDynamic;

    private String downloadUrl;

    private String checksum;

    private String className;

    private String methodName;

    private boolean isDown;

    private boolean isDel;

    private boolean isActive;

    private int currentSwitch;

    private int small;

    private int medium;

    private int sized;

    private PluginType pluginType;


    public ModulePo(String moduleName, String version, boolean isDynamic, String downloadUrl,
                    String checksum, String className, String methodName, boolean isDown, boolean
                            isDel, boolean isActive, int currentSwitch, int small, int medium, int sized,PluginType pluginType) {
        this.moduleName = moduleName;
        this.version = version;
        this.isDynamic = isDynamic;
        this.downloadUrl = downloadUrl;
        this.checksum = checksum;
        this.className = className;
        this.methodName = methodName;
        this.isDown = isDown;
        this.isDel = isDel;
        this.isActive = isActive;
        this.currentSwitch = currentSwitch;
        this.small = small;
        this.medium = medium;
        this.sized = sized;
        this.pluginType = pluginType;
    }


    public ModulePo() {
    }

    public int getSwitch() {
        int switchVal = 0;
        if (isDown()) switchVal = Constants.DOWNLOAD;
        if (isActive()) switchVal |= Constants.ACTIVE;
        if (isDel()) switchVal |= Constants.DELETE;
        return switchVal;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public boolean getDynamic(int isDynamic) {
        if (isDynamic == 1) return true;
        return false;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isDown() {
        return isDown;
    }

    public boolean getDown(int isDown) {
        if (isDown == 1) return true;
        return false;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public boolean isDel() {
        return isDel;
    }

    public boolean isTurnOff() {
        return !(isDel || isActive || isDown);
    }

    public boolean getDel(int isDel) {
        if (isDel == 1) return true;
        return false;
    }

    public void setDel(boolean del) {
        isDel = del;
    }

    public boolean getActive(int isActive) {
        if (isActive == 1) return true;
        return false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCurrentSwitch() {
        return currentSwitch;
    }

    public void setCurrentSwitch(int currentSwitch) {
        this.currentSwitch = currentSwitch;
    }

    public int getSmall() {
        return small;
    }

    public void setSmall(int small) {
        this.small = small;
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(int medium) {
        this.medium = medium;
    }

    public int getSized() {
        return sized;
    }

    public void setSized(int sized) {
        this.sized = sized;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
       this.pluginType = pluginType;
    }
}
