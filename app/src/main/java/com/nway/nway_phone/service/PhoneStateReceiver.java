package com.nway.nway_phone.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PhoneStateReceiver extends BroadcastReceiver {
    private final String TAG = "PhoneState";
    private PhoneStateListener phoneStateListener = null;
    private boolean incoming_flag;
    private Context mContext;


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        String event = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(phoneStateListener != null){
            phoneStateListener.onStateChanged(event);
        }
        if (event.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            Log.d(TAG, "-->RINGING--正在响铃");
            incoming_flag = true;
        } else if (event.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            Log.d(TAG, "-->EXTRA_STATE_OFFHOOK--正在通话");
//            startService(context, event);
        } else if (event.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Log.d(TAG, "-->EXTRA_STATE_IDLE--电话挂断--空闲");
        }

    }


    public interface PhoneStateListener{
        void onStateChanged(String state);
    }
    public void setPhoneStateListener(PhoneStateListener phoneStateListener){
        this.phoneStateListener = phoneStateListener;
    }
}
