/**
  * Copyright 2021 bejson.com 
  */
package com.newstar.scorpiodata.entity;

import android.widget.Toast;

import com.newstar.scorpiodata.risk.RiskType;
import com.newstar.scorpiodata.utils.PluginInit;

/**
 * Auto-generated: 2021-02-03 16:0:31
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class Status {
    //成功
    public static final int SUCCEED = 200;

    //token验证失败
    public static final int AUTH_FAILURE_ERROR = 401;

    //用户名或密码错误
    public static final int CODE_0015 = 0015;

    public void showMessage(){
        Toast.makeText(PluginInit.ACTIVITY, message, Toast.LENGTH_SHORT).show();
    }
    private Integer code;
    private String message;
    public void setCode(Integer code) {
         this.code = code;
     }
     public Integer getCode() {
         return code;
     }

    public void setMessage(String message) {
         this.message = message;
     }
     public String getMessage() {
         return message;
     }

}