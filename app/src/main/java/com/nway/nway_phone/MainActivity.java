package com.nway.nway_phone;

import static com.nway.nway_phone.common.PhoneData.LOCAL_CALL;
import static com.nway.nway_phone.common.PhoneData.SIP_CALL;
import static com.xuexiang.xui.XUI.getContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import com.nway.nway_phone.MainActivityPermissionsDispatcher;
import com.nway.nway_phone.R;
import com.nway.nway_phone.common.CallLogDatabaseHelper;
import com.nway.nway_phone.common.MyUtils;
import com.nway.nway_phone.common.PhoneData;
import com.nway.nway_phone.databinding.ActivityMainBinding;
import com.nway.nway_phone.linphone.CallActivity;
import com.nway.nway_phone.linphone.PhoneSetting;
import com.nway.nway_phone.linphone.SipPhone;
import com.nway.nway_phone.service.PhoneStateReceiver;
import com.nway.nway_phone.ui.call.CallHistory;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;

import java.util.Objects;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements AhListener,PhoneStateReceiver.PhoneStateListener {

    private final static String TAG="MainActivity";
    private ActivityMainBinding binding;
    private String extension;
    private PhoneSetting phoneSetting;

    private PhoneStateReceiver phoneStateReceiver;
    private boolean savingCallLog;

//    @BindView(R.id.title_bar)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lunchPhoneStateReceiver();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setLeftVisible(true);
        titleBar.setLeftText(" - ");
        titleBar.setLeftImageResource(R.drawable.ic_dot_white_10dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            titleBar.setLeftTopRightBottom(0,100,0,0);
        }
//        titleBar.addAction(new TitleBar.ImageAction(R.drawable.ic_bell_outline_white_22dp) {
//            @Override
//            public void performAction(View view) {
////                showPhoneSetting();
//            }
//        });

        //获取电话设置
        phoneSetting = PhoneData.getPhoneSetting(this);
        SipPhone sp = SipPhone.getInstance();
        sp.initSip(this);
        if(Objects.equals(phoneSetting.phoneSelect,LOCAL_CALL)){
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sp.setLocalCall(phoneSetting.localPhoneNumber);
                }

            },1000);
        }else if(Objects.equals(phoneSetting.phoneSelect, SIP_CALL)){
            sp.registerAccount(phoneSetting.extension,phoneSetting.password,phoneSetting.domain,phoneSetting.port,phoneSetting.server);
        }

//        ButterKnife.bind(this);


//        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

//        getLastLocalCall();

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(phoneStateReceiver);
        super.onDestroy();
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void setTitleBarTitle(String title) {
        TitleBar titleBar = findViewById(R.id.title_bar);
        if (titleBar != null){
            titleBar.setVisibility(View.GONE);
            titleBar.setVisibility(View.VISIBLE);
            titleBar.setTitle(title);
        }
    }

    @Override
    public void setLeftTitle(String title,String status) {
        TitleBar titleBar = findViewById(R.id.title_bar);

        if (Objects.equals(title, "返回")){
            titleBar.setLeftText(" ");
            titleBar.setLeftTextBold(true);
            titleBar.setLeftImageResource(R.drawable.icon_back_white);
            titleBar.setLeftClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.super.onBackPressed();
                }
            });
            return;
        }
        if (!Objects.equals(title, "")){
            extension = title;
        }else{
            title = extension;
        }
        if (titleBar != null){
            int leftImage = R.drawable.ic_dot_white_10dp;
            String leftStatus = " ";
            titleBar.setLeftText(title + " " + status);
            switch (status){
                case "注册成功":
                    leftStatus = "已注册";
                    leftImage = R.drawable.ic_dot_green_10dp;
                    break;
                case "正在注册":
                    leftStatus = "正在注册";
                    leftImage = R.drawable.ic_dot_yellow_10dp;
                    break;
                case "注册失败":
                    leftStatus = "注册失败";
                    leftImage = R.drawable.ic_dot_gray_10dp;
                    break;
                case "退出注册":
                    leftStatus = "已注销";
                    leftImage = R.drawable.ic_dot_gray_10dp;
                    break;
                case "其他错误":
                    leftStatus = "注册错误";
                    leftImage = R.drawable.ic_dot_gray_10dp;
                    break;
                default:
                    leftStatus = status;
                    leftImage = R.drawable.ic_dot_green_10dp;

            }
            titleBar.setLeftText(title + " " + leftStatus);
            titleBar.setLeftTextBold(true);
            titleBar.setLeftImageResource(leftImage);
        }
    }

    @Override
    public void setPhoneSetting(PhoneSetting phoneSetting) {
        this.phoneSetting = phoneSetting;
    }

//
//    @Override
//    public void setCallStatus(String status) {
//        Log.e("Sip","接收到状态："+status);
//    }

    public void lunchPhoneStateReceiver(){
        phoneStateReceiver = new PhoneStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        Log.e(TAG,"开启电话监听");
        registerReceiver(phoneStateReceiver,intentFilter);
        phoneStateReceiver.setPhoneStateListener(this);
    }

    @Override
    public void onStateChanged(String state) {
        Log.e(TAG,"收到状态变化："+state);
        if(state.equals(TelephonyManager.EXTRA_STATE_IDLE) && !savingCallLog){
            savingCallLog = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getLastLocalCall();
                    savingCallLog = false;
                }

            },800);
        }
    }


    private void getLastLocalCall(){
        ContentResolver contentResolver = getContentResolver();
        //
        String ccc = CallLog.Calls.getLastOutgoingCall(this);
        Log.e(TAG,"lastOutGoingCall::"+ccc);
        String[] strings = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.LAST_MODIFIED,
        };
        Cursor cursor = null;
        Uri limitedCallLogUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            limitedCallLogUri = CallLog.Calls.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CallLog.Calls.LIMIT_PARAM_KEY, "1").build();
            cursor = contentResolver.query(limitedCallLogUri,strings,null,null,
                    CallLog.Calls.DEFAULT_SORT_ORDER);
        }else{
            cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,strings,null,null,
                    CallLog.Calls.DEFAULT_SORT_ORDER+" LIMIT 1");
        }
        if (cursor != null){
            try {
                while (cursor.moveToNext()){

                    CallLogDatabaseHelper callLogDatabaseHelper = CallLogDatabaseHelper.getInstance(this);
                    CallHistory vv = new CallHistory();
                    vv.setCallee(cursor.getInt(1)==2 ? cursor.getString(0) : extension);
                    vv.setCaller(cursor.getInt(1)==2 ? extension : cursor.getString(0));
                    vv.setCallDate(MyUtils.milliTimestampToDatetime(cursor.getLong(2)));
                    String callDirection = "";
                    if(cursor.getInt(1) == 2){
                        callDirection = "Outgoing";
                    }else{
                        callDirection = "Incoming";
                    }
                    vv.setCallDirection(callDirection);
                    long duration = (System.currentTimeMillis() - cursor.getLong(2)) / 1000;
                    vv.setCallDuration((int) duration);
                    vv.setCallBill(cursor.getInt(3));
                    String hangupCause = "";
                    switch (cursor.getInt(1)){
                        case 1:
                        case 2:
                            if(cursor.getInt(3) == 0){
                                hangupCause = "Aborted";
                            }
                            if (cursor.getInt(3) > 0){
                                hangupCause = "Success";
                            }
                            break;
                        case 3:
                            hangupCause = "Missed";
                            break;
                        case 4:
                            hangupCause = "VoiceMail";
                            break;
                        case 5:
                            hangupCause = "Rejected";
                            break;
                        case 6:
                            hangupCause = "Blocked";
                            break;
                        default:
                            hangupCause = String.valueOf(cursor.getInt(3));
                            break;
                    }

                    vv.setCallHangupCause(hangupCause);

                    if(Objects.equals(phoneSetting.phoneSelect, LOCAL_CALL) && phoneSetting.autoRecord && cursor.getInt(3) > 0){
                        vv.setRecordFile(MyUtils.getLocalRecord(cursor.getString(0),cursor.getLong(2),phoneSetting.localRecordingPath));
                    }

                    long row = callLogDatabaseHelper.addCallLog(vv);
                    Log.e("SipPhone","插入了话单："+row);
                    SipPhone.getInstance().callLogNotify(vv);
                }
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();  //关闭cursor，避免内存泄露
            }
        }
    }

    @Override
    public void readyToCall(String currentNumber){
        if(Objects.equals(phoneSetting.phoneSelect, LOCAL_CALL)){
            if (phoneSetting.autoRecord && !MyUtils.checkBrandRecord(this)){
                return;
            }
            String[] strings = new String []{Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if(MyUtils.hasPermissions(this,strings)){
                showLocalCall(currentNumber);
            }else{
                MainActivityPermissionsDispatcher.getRecordPermissionWithPermissionCheck(MainActivity.this);
            }
        }else if(Objects.equals(phoneSetting.phoneSelect, SIP_CALL)){
            MainActivityPermissionsDispatcher.showSipCallWithPermissionCheck(MainActivity.this,currentNumber);
        }
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.MODIFY_AUDIO_SETTINGS})
    public void showSipCall(String callee){
        SipPhone sp = SipPhone.getInstance();
        if (Objects.equals(sp.getExtension(), "")){
            XToastUtils.toast("sip账户不可用");
            return;
        }

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra("callee",callee);
        intent.putExtra("caller",sp.getExtension());
        startActivity(intent);
        sp.call(getContext(),callee,false);
    }

    @NeedsPermission({Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void getRecordPermission(){
        if(!MyUtils.hasPermissions(this, Manifest.permission.CALL_PHONE)){
            //有的手机用NeedsPermission无法唤起拨打电话的授权，所以单独请求一下权限
            ActivityCompat.requestPermissions(this,new String []{Manifest.permission.CALL_PHONE},1);
        }
        Log.e(TAG,"获取权限");
    }

    public void showLocalCall(String callee){
        String[] strings = new String []{Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};


        ActivityCompat.requestPermissions(this,strings,1);
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + callee);
        intent.setData(data);
        startActivity(intent);
//        calling = new CallHistory();
//        calling.setCallee(callee);
//        calling.setCaller(phoneSetting.localPhoneNumber);
//        calling.setCallDate(MyUtils.getDateTime());
//        calling.setCallDirection("Outgoing");
    }
}