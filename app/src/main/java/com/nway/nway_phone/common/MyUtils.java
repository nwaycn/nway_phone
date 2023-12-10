package com.nway.nway_phone.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.xuexiang.xui.utils.XToastUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

public class MyUtils {
    private final static String TAG="Utils";

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"没有权限："+permission);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkPermission(Activity activity,String[] strings) {
        boolean finalRes = true;
        for (String string : strings) {
            if (ContextCompat.checkSelfPermission(activity, string) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, strings, 1);
                finalRes = false;
            }else{
                Log.e("Permission","有权限"+string);
            }
        }
        return finalRes;
    }

    //时间戳转时间日期不带秒
    public static String getDateTimeWithOutSecond(long timestamp){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        return sdf.format(timestamp);
    }
    //当前日期时间
    public static String getDateTime(){
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    //毫秒转日期时间
    public static String milliTimestampToDatetime(long timestamp){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }

    //时间戳转时间
    public static String timestampToDatetime(long timestamp){
        // 时间戳转化为时间

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date time2 = new Date(timestamp * 1000L);
        return sdf.format(time2);
    }

    //秒数戳转时间
    public static String secToTime(Object second) {
        String res = "";
        Double d = Double.parseDouble(second.toString());
        Integer seconds = (int) Math.round(d);
        if(seconds <= 0)return "";
        int h = seconds / 3600;
        int m = seconds % 3600 / 60;
        int s = seconds % 60; //不足60的就是秒，够60就是分
        if (h != 0){
            res += h + "小时";
        }
        if (m != 0){
            res += m + "分钟";
        }
        if (s != 0){
            res += s + "秒";
        }
        return res;
    }
    //秒数戳转时间
    public static String secToTime2(Object second) {
        String res = "";
        Double d = Double.parseDouble(second.toString());
        Integer seconds = (int) Math.round(d);
        if(seconds <= 0)return "";
        int h = seconds / 3600;
        int m = seconds % 3600 / 60;
        int s = seconds % 60; //不足60的就是秒，够60就是分
        if (h < 10){
            res += "0"+h+":";
        }else{
            res += h+":";
        }
        if (m < 10){
            res += "0"+m + ":";
        }else{
            res += m + ":";
        }
        if (s < 10){
            res += "0"+s;
        }else{
            res += s;
        }
        return res;
    }

    public static String getLocalRecord(String phone,long callDate,String localRecordPath){
        ArrayList<String> result = new ArrayList<>();
        File filePath = new File(localRecordPath);
        if(!filePath.exists()){
            Log.e(TAG,"路径："+localRecordPath+" "+"不存在");
            return "";
        }
        File[] files = filePath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.contains(phone) && name.contains(getDateTimeWithOutSecond(callDate)));
//                return (name.contains(phone) && name.contains("20231126"));
            }
        });
        if(files == null){
            Log.e(TAG,"录音路径："+localRecordPath+" "+"listFiles没有文件");
            return "";
        }
        for (File file : files){
            Log.e(TAG,"文件名："+file.getName());
            if (file.getName().trim().toLowerCase().contains(phone)) {
                result.add(file.getName());
            }
        }
        Log.e(TAG,"最终路径"+localRecordPath+result.get(0));
        return result.isEmpty() ? "" : localRecordPath+result.get(0);
    }

    public static String localRecordPath(){
//        String recordPath = "file:///storage/emulated/0";
        String recordPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.e(TAG,"手机品牌："+Build.BRAND);
        switch (Build.BRAND){
            case "Redmi":
            case "Xiaomi":
                recordPath += "/MIUI/sound_recorder/call_rec/";
                break;
            case "Oppo":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    recordPath += "/Music/Recordings/Call Recordings/";
                }else{
                    recordPath += "/Recordings/";
                }
                break;
            case "Vivo":
                recordPath += "/Record/Call/";
                break;
            case "HONOR":
            case "HUAWEI":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    recordPath += "/Sounds/CallRecord/";
                }else{
                    recordPath += "/record/";
                }
                break;
            case "meizu":
                recordPath += "/Recorder/";
                break;
            case "samsung":
                recordPath += "/Call/";
                break;
            case "zte":
                recordPath += "/Recordings/";
                break;
            default:
                recordPath = "";
                break;
        }
        Log.e(TAG,"录音路径获取："+recordPath);
        return recordPath;
    }

    public static boolean checkBrandRecord(Context context){
        String brand = Build.BRAND;
        Log.e(TAG,"手机品牌："+brand);
        boolean checkRecord = true;
        switch (Build.BRAND){
            case "Redmi":
            case "Xiaomi":
                if(!checkXiaomiRecord(context)){
                    startXiaomiRecord(context);
                    checkRecord = false;
                }
                break;
            case "Oppo":
                if(!checkOppoRecord(context)){
                    startOppoRecord(context);
                    checkRecord = false;
                }
                break;
            case "Vivo":
                if(!checkVivoRecord(context)){
                    startVivoRecord(context);
                    checkRecord = false;
                }
                break;
            case "HONOR":
            case "HUAWEI":
                if(!checkHuaweiRecord(context)){
                    startHuaweiRecord(context);
                    checkRecord = false;
                }
                break;
        }
        return checkRecord;
    }

    public static boolean checkXiaomiRecord(Context context){
        try {
            int key = Settings.System.getInt(context.getContentResolver(), "button_auto_record_call");
            Log.d(TAG, "Xiaomi key:" + key);
            //0是未开启,1是开启
            return key != 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
    public static boolean checkOppoRecord(Context context){
        try {
            int key = Settings.Global.getInt(context.getContentResolver(), "oppo_all_call_audio_record");
            Log.d(TAG, "Oppo key:" + key);
            //0代表OPPO自动录音未开启,1代表OPPO自动录音已开启
            return key != 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
    public static boolean checkVivoRecord(Context context){
        try {
            int key = Settings.Global.getInt(context.getContentResolver(), "call_record_state_global");
            Log.d(TAG, "Vivo key:" + key);
            //0代表VIVO自动录音未开启,1代表VIVO所有通话自动录音已开启,2代表指定号码自动录音
            return key == 1;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
    public static boolean checkHuaweiRecord(Context context){
        try {
            int key = Settings.Secure.getInt(context.getContentResolver(), "enable_record_auto_key");
            Log.d(TAG, "Huawei key:" + key);
            //0代表华为自动录音未开启,1代表华为自动录音已开启
            return key != 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void findTheKey(Context context){
        //1.Secure
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Cursor cursor = context.getContentResolver().query(Settings.Secure.CONTENT_URI, null, null, null);
            String[] columnNames = cursor.getColumnNames();
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                for (String columnName : columnNames) {
                    @SuppressLint("Range") String string = cursor.getString(cursor.getColumnIndex(columnName));
                    builder.append(columnName).append(":").append(string).append("\n");
                }
            }
            Log.e(TAG, builder.toString());
        }

//2.Global
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Cursor cursor = context.getContentResolver().query(Settings.Global.CONTENT_URI, null, null, null);
            String[] columnNames = cursor.getColumnNames();
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                for (String columnName : columnNames) {
                    @SuppressLint("Range") String string = cursor.getString(cursor.getColumnIndex(columnName));
                    builder.append(columnName).append(":").append(string).append("\n");
                }
            }
            Log.e(TAG, builder.toString());
        }

//3.System
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Cursor cursor = context.getContentResolver().query(Settings.System.CONTENT_URI, null, null, null);
            String[] columnNames = cursor.getColumnNames();
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                for (String columnName : columnNames) {
                    @SuppressLint("Range") String string = cursor.getString(cursor.getColumnIndex(columnName));
                    builder.append(columnName).append(":").append(string).append("\n");
                }
            }
            Log.e(TAG, builder.toString());
        }
    }

    public static void startXiaomiRecord(Context context){
        ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.settings.CallRecordSetting");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        XToastUtils.toast("请打开通话自动录音功能");
    }
    public static void startVivoRecord(Context context){
        ComponentName componentName = new ComponentName("com.android.incallui", "com.android.incallui.record.CallRecordSetting");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        XToastUtils.toast("请打开通话自动录音功能");
    }
    public static void startHuaweiRecord(Context context){
        ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.MSimCallFeaturesSetting");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        XToastUtils.toast("请打开通话自动录音功能");
    }
    public static void startOppoRecord(Context context){
        ComponentName componentName = new ComponentName("com.android.phone", "com.android.phone.OppoCallFeaturesSetting");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        XToastUtils.toast("请打开通话自动录音功能");
    }

    public static String getAppVersion(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName,0);
            int versionCode = packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
