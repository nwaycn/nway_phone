package com.nway.nway_phone.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.nway.nway_phone.linphone.PhoneSetting;
import com.nway.nway_phone.linphone.SipPhone;

import java.util.Objects;

public class PhoneData {
    private final static String TAG="PhoneData";
    private final static String PHONE_SETTING = "phone_setting";
    public final static String LOCAL_CALL = "localCall";
    public final static String SIP_CALL = "sipCall";

    public static PhoneSetting getPhoneSetting(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_SETTING, Context.MODE_PRIVATE);
        return new PhoneSetting(
                sharedPreferences.getString("phone_select",null),
                sharedPreferences.getString("local_phone_number",""),
                sharedPreferences.getBoolean("auto_record",false),
                sharedPreferences.getString("local_recording_path",MyUtils.localRecordPath()),
                sharedPreferences.getString("extension",""),
                sharedPreferences.getString("password",""),
                sharedPreferences.getString("domain",""),
                sharedPreferences.getString("port",""),
                sharedPreferences.getString("server",""));
    }

    public static void savePhoneSetting(Context context,PhoneSetting phoneSetting){
        SipPhone sp = SipPhone.getInstance();
        if(Objects.equals(phoneSetting.phoneSelect,LOCAL_CALL)){
            if(!Objects.equals(sp.getExtension(),"")){
                sp.unregister();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sp.setLocalCall(phoneSetting.localPhoneNumber);
                }

            },1000);
        }else if(Objects.equals(phoneSetting.phoneSelect,SIP_CALL)){
            sp.unregister();
            sp.registerAccount(phoneSetting.extension,phoneSetting.password,phoneSetting.domain,phoneSetting.port,phoneSetting.server);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PHONE_SETTING, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("phone_select",phoneSetting.phoneSelect);
        editor.putBoolean("auto_record",phoneSetting.autoRecord);
        editor.putString("local_phone_number",phoneSetting.localPhoneNumber);
        editor.putString("local_recording_path",phoneSetting.localRecordingPath);
        editor.putString("extension",phoneSetting.extension);
        editor.putString("password",phoneSetting.password);
        editor.putString("domain",phoneSetting.domain);
        editor.putString("port",phoneSetting.port);
        editor.putString("server",phoneSetting.server);
        editor.apply();
    }

}
