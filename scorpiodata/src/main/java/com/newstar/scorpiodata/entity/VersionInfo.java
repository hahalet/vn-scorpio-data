package com.newstar.scorpiodata.entity;

/**
 * Auto-generated: 2021-02-26 15:51:18
 *
 * @author www.jsons.cn 
 * @website http://www.jsons.cn/json2java/ 
 */
public class VersionInfo {

    private int id;
    private String versionNumber;
    private String versionContent;
    private int createTime;
    private int updateTime;
    private String enableFlag;
    private String needUpdate;
    private String appDownloadUrl;
    public void setId(int id) {
         this.id = id;
     }
     public int getId() {
         return id;
     }

    public void setVersionNumber(String versionNumber) {
         this.versionNumber = versionNumber;
     }
     public String getVersionNumber() {
         return versionNumber;
     }

    public void setVersionContent(String versionContent) {
         this.versionContent = versionContent;
     }
     public String getVersionContent() {
         return versionContent;
     }

    public void setCreateTime(int createTime) {
         this.createTime = createTime;
     }
     public int getCreateTime() {
         return createTime;
     }

    public void setUpdateTime(int updateTime) {
         this.updateTime = updateTime;
     }
     public int getUpdateTime() {
         return updateTime;
     }

    public void setEnableFlag(String enableFlag) {
         this.enableFlag = enableFlag;
     }
     public String getEnableFlag() {
         return enableFlag;
     }

    public void setNeedUpdate(String needUpdate) {
         this.needUpdate = needUpdate;
     }
     public String getNeedUpdate() {
         return needUpdate;
     }

    public void setAppDownloadUrl(String appDownloadUrl) {
         this.appDownloadUrl = appDownloadUrl;
     }
     public String getAppDownloadUrl() {
         return appDownloadUrl;
     }
}