package com.nway.nway_phone.linphone;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nway.nway_phone.common.MyUtils;
import com.nway.nway_phone.R;
import com.nway.nway_phone.databinding.ActivityCallBinding;


import org.linphone.core.Call;

import java.util.Objects;

public class CallActivity extends AppCompatActivity implements CallStatusListener {
    private ActivityCallBinding binding;
    private Boolean isCalling = true;
    private Boolean isSpeaker = false;
    private Boolean isRecording = false;
    private Handler handlerBill ;

    @SuppressLint({"SetTextI18n", "HandlerLeak"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        hideActionBar();
        setTextColor(false);

        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.hangup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {

                SipPhone.getInstance().hangup();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CallActivity.this.finish();
                    }

                },1000);
            }
        });

        binding.speaker.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                isSpeaker = !isSpeaker;
                if(!isSpeaker){
                    binding.speaker.setTextColor(getResources().getColor(R.color.display_background_color));
                }else{
                    binding.speaker.setTextColor(R.color.dialer_action_pressed);
                }

                SipPhone.getInstance().speaker(isSpeaker);
            }
        });

        binding.recording.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                isRecording = !isRecording;
                if(!isRecording){
                    binding.recording.setTextColor(R.color.white);
                }else{
                    binding.recording.setTextColor(R.color.dialer_action_pressed);
                }
                SipPhone.getInstance().recording(isRecording);
            }
        });

        String callee = getIntent().getStringExtra("callee");
        String caller = getIntent().getStringExtra("caller");
        binding.callee.setText(callee);
        binding.callDescription.setText(caller+" -> "+callee);
        SipPhone sp = SipPhone.getInstance();
        sp.initCallStatusListener(this);

        //创建一个处理器对象
        handlerBill = new Handler(Looper.myLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                binding.status.setText((String) msg.obj);
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    //设置状态栏字体颜色
    public void setTextColor(boolean isDarkBackground){
        View decor = this.getWindow().getDecorView();
        if (isDarkBackground) {
            //黑暗背景字体浅色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(getColor(R.color.transparent));
        } else {
            //高亮背景字体深色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getColor(R.color.transparent));
        }
    }


    @Override
    public void setCallStatus(@NonNull Call call,String status) {
        binding.status.setText(status);

        if (Objects.equals(status,"通话中")){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    int i=0;
                    while (isCalling) {
                        i++;
                        Log.e("SipPhone","通话质量："+call.getCurrentQuality());
                        String s = status + " " + MyUtils.secToTime2(i);
                        //发送给处理器去处理,用处理器handler,创建一个空消息对象
                        Message message = new Message();
                        //把值赋给message
                        message.obj = s;
                        //把消息发送给处理器
                        handlerBill.sendMessage(message);
                        try {
                            //延时是一秒
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        if (Objects.equals(status,"挂断")){
            isCalling = false;
//            // 设置扬声器出声
//            audioManager.setSpeakerphoneOn(true);


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CallActivity.this.finish();

                }

            },3000);
        }
    }
}
