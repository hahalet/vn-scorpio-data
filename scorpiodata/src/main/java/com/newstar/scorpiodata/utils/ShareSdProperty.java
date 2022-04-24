package com.newstar.scorpiodata.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ShareSdProperty {

    /**
     * 保存值
     * @param key
     * @param value
     */
    public static void setValue(String key, String value) {
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            Map oldValuews = getValues();
            if(oldValuews!=null){
                map.putAll(oldValuews);
            }

            map.put(key, value);
            File dir = Environment.getExternalStorageDirectory();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(getSDPath() + "/share_property");
            if (!file.exists()) {
                //在指定的文件夹中创建文件
                file.createNewFile();
            }
            FileOutputStream out;
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(map);
            objOut.flush();
            objOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取值
     * @param key
     * @return
     */
    public static String getValue(String key){
        Map<String, String> map = getValues();
        if(map!=null){
            return map.get(key);
        }
        return null;
    }

    private static Map<String, String> getValues() {
        Map<String, String> temp = null;
        File file = new File(getSDPath() + "/share_property");
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            temp = (Map<String, String>) objIn.readObject();
            objIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private static String getSDPath() {
        File sdDir=null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在*/
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }
}