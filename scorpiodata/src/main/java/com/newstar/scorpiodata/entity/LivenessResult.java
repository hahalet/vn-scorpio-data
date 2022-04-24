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
public class LivenessResult {

    private String code;
    private String transactionId;
    private String pricingStrategy;
    private String message;
    private LivenessData data;
    private String extra;
    public void setCode(String code) {
         this.code = code;
     }
     public String getCode() {
         return code;
     }

    public void setTransactionId(String transactionId) {
         this.transactionId = transactionId;
     }
     public String getTransactionId() {
         return transactionId;
     }

    public void setPricingStrategy(String pricingStrategy) {
         this.pricingStrategy = pricingStrategy;
     }
     public String getPricingStrategy() {
         return pricingStrategy;
     }

    public void setMessage(String message) {
         this.message = message;
     }
     public String getMessage() {
         return message;
     }

    public void setData(LivenessData data) {
         this.data = data;
     }
     public LivenessData getData() {
         return data;
     }

    public void setExtra(String extra) {
         this.extra = extra;
     }
     public String getExtra() {
         return extra;
     }

}