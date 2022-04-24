/**
  * Copyright 2022 json.cn 
  */
package com.newstar.scorpiodata.entity;

/**
 * Auto-generated: 2022-01-06 15:23:11
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class LivenessData {

    private String detectionResult;
    private float livenessScore;
    public void setDetectionResult(String detectionResult) {
         this.detectionResult = detectionResult;
     }
     public String getDetectionResult() {
         return detectionResult;
     }

    public void setLivenessScore(int livenessScore) {
         this.livenessScore = livenessScore;
     }
     public float getLivenessScore() {
         return livenessScore;
     }

}