package com.nway.nway_phone.linphone;

import static com.nway.nway_phone.common.PhoneData.LOCAL_CALL;
import static com.nway.nway_phone.common.PhoneData.SIP_CALL;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.nway.nway_phone.AhListener;
import com.nway.nway_phone.common.MyUtils;
import com.nway.nway_phone.ui.call.CallFragment;
import com.nway.nway_phone.ui.call.CallHistory;
import com.nway.nway_phone.common.CallLogDatabaseHelper;
import com.nway.nway_phone.common.PhoneData;

import org.linphone.core.AccountParams;
import org.linphone.core.Address;
import org.linphone.core.AudioDevice;
import org.linphone.core.Factory;
import org.linphone.core.Account;
import org.linphone.core.AuthInfo;
import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListener;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.MediaEncryption;
import org.linphone.core.PayloadType;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;

import java.util.Objects;

public class SipPhone {

    private String extension = "";
    private String extensionStatus = "";
    private AhListener ahListener;
    private CallStatusListener callStatusListener;
    private CallHangupListener callHangupListener = null;
    private Core core;
    private Factory factory;
    private static SipPhone mInstance = null;
    private Context mContext;
    private Call currentCall;
    private  boolean isRecording;

    public static SipPhone getInstance(){
        if (mInstance == null){
            mInstance = new SipPhone();
        }
        return mInstance;
    }

    //初始化Factory, 在APP启动时调用.
//    public SipPhone(Context c){
//        mContext = c;
//        this.factory = Factory.instance();
//        mInstance = this;
//        if (mContext instanceof AhListener) {
//            Log.d("SipPhone:","ahListener");
//            ahListener = (AhListener) c;
//        }
//    }
    //初始化
    public void initSip(Context c){
        mContext = c;
        this.factory = Factory.instance();
        mInstance = this;
        if (mContext instanceof AhListener) {
            ahListener = (AhListener) c;
        }

        core = factory.createCore(null, null, mContext);
        core.addListener(coreListener);
        core.setEchoCancellationEnabled(true);

        //回声消除
//        core.getMediastreamerFactory().setDeviceInfo(android.os.Build.MANUFACTURER, android.os.Build.MODEL, android.os.Build.DEVICE, org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 0 );
        core.getMediastreamerFactory().setDeviceInfo(android.os.Build.MANUFACTURER, android.os.Build.MODEL, android.os.Build.DEVICE, org.linphone.mediastream.Factory.DEVICE_HAS_BUILTIN_AEC_CRAPPY, 0, 0 );
        core.setEchoCancellationEnabled(true);
        core.setEchoLimiterEnabled(false);

        PayloadType[] payloads =core.getAudioPayloadTypes();
        for (PayloadType payload : payloads) {
            Log.d("Sip",payload.getDescription()+payload.getEncoderDescription()+payload.getMimeType());
        }

//        AccountParams accountParams = core.createAccountParams();

    }

    public void initCallStatusListener(Context c){
        if (c instanceof CallStatusListener) {
            Log.d("SipPhone:","callStatusListener");
            callStatusListener = (CallStatusListener) c;
        }
    }

    public void initCallHangupListener(CallFragment c){
        if (c != null) {
            callHangupListener = (CallHangupListener) c;
        }
    }

    public void setExtension(String extension){
        this.extension = extension;
    }
    public String getExtension(){
        return this.extension;
    }
    //注册账户
    public void registerAccount(String username,String password,String domain,String port,String server){


        if(username.equals("") || password.equals("")){
            Log.e("SipPhone","login failed: username(" + username + "), password(" + password + "), domain(" + domain + "), port(" + port + ")");
            return;
        }

        if(port.equals("")){
            port = "5060";
        }
        //sip:100@17imp.com:5060
//        if(!domain.contains(":")){
//            domain += ":" + port;
//        }

        AuthInfo user = factory.createAuthInfo(username, null, password, null, null, domain, null);
        AccountParams accountParams = core.createAccountParams();
        // A SIP account is identified by an identity address that we can construct from the username and domain
        //设置sip账户
        String sipAddress = "sip:" + username + "@" + domain;
        Address identity = factory.createAddress(sipAddress);
        Log.e("SipPhone","login for address " + sipAddress);
        accountParams.setIdentityAddress(identity);

        // We also need to configure where the proxy server is located
        if (!server.equals("")){
            //设置代理服务器
            if(!server.contains(":")){
                server += ":" + port;
            }
            Address proxy = factory.createAddress("sip:" + server);
            Log.e("SipPhone","sip:" + server);
            // We use the Address object to easily set the transport protocol
            assert proxy != null;
            proxy.setTransport(TransportType.Udp);
            accountParams.setServerAddress(proxy);
            accountParams.setOutboundProxyEnabled(true);
        }

        // And we ensure the account will start the registration process
        accountParams.setRegisterEnabled(true);


        // Asks the CaptureTextureView to resize to match the captured video's size ratio
        //core.getConfig().setBool("video", "auto_resize_preview_to_keep_ratio", true);

        // Now that our AccountParams is configured, we can create the Account object
        Account account = core.createAccount(accountParams);
        //account.setCustomHeader("Header1", "Header2");

        // Now let's add our objects to the Core
        core.addAuthInfo(user);
        core.addAccount(account);

        // Also set the newly added account as default
        core.setDefaultAccount(account);
        core.setUserAgent("User", "Agent");

        // Finally we need the Core to be started for the registration to happen (it could have been started before)
        core.start();
    }
    //退出注册
    public void unregister(){
        if(core == null){
            return;
        }
        Account account = core.getDefaultAccount();
        if(account != null) {
            AccountParams accountParams = account.getParams().clone();
            accountParams.setRegisterEnabled(false);
            account.setParams(accountParams);
        }
    }
    //监听
    CoreListener coreListener = new CoreListenerStub(){
        @Override
        public void onCallStateChanged(@NonNull Core core, @NonNull Call call, Call.State state, @NonNull String message) {
            super.onCallStateChanged(core, call, state, message);
            currentCall = call;
            if (callStatusListener == null){
                return;
            }
            if (state == Call.State.OutgoingProgress){
                Log.e("SipPhone","正在呼出");
                callStatusListener.setCallStatus(call,"正在呼出");

            }else if (state == Call.State.IncomingReceived){
                Log.e("SipPhone","新的来电");
                callStatusListener.setCallStatus(call,"新的来电");
//                Intent intent = new Intent(mContext, CallActivity.class);
//                intent.putExtra("callee",getExtension());
//                intent.putExtra("caller",String.valueOf(call.getCallLog().getFromAddress().getUsername()));
//                mContext.startActivity(intent);
            }else if (state == Call.State.OutgoingEarlyMedia){
                Log.e("SipPhone","振铃中");
                callStatusListener.setCallStatus(call,"振铃中");
            }else if (state == Call.State.StreamsRunning){
                Log.e("SipPhone","通话中");
                callStatusListener.setCallStatus(call,"通话中");
            }else if (state == Call.State.Released){
                currentCall = null;
                isRecording = false;
                Log.e("SipPhone","挂断");
                callStatusListener.setCallStatus(call,"挂断");

                //写入话单
                CallLogDatabaseHelper callLogDatabaseHelper = CallLogDatabaseHelper.getInstance(mContext);
                CallHistory vv = new CallHistory(
                        String.valueOf(call.getToAddress().getUsername()),
                        String.valueOf(call.getCallLog().getFromAddress().getUsername()),
                        MyUtils.timestampToDatetime(call.getCallLog().getStartDate()),
                        String.valueOf(call.getDir()),
                        call.getCallLog().getDuration(),
                        call.getCallLog().getDuration(),
                        String.valueOf(call.getCallLog().getStatus()));
                if (isRecording){
                    vv.setRecordFile(call.getParams().getRecordFile());
                }
                long row = callLogDatabaseHelper.addCallLog(vv);
                Log.e("SipPhone","插入了话单："+row);
                //插入列表视图
                callLogNotify(vv);
            }else if (state == Call.State.Error){
                Log.e("SipPhone","呼叫错误");
                callStatusListener.setCallStatus(call,"呼叫错误");
            }

//            Log.e("SipPhone","呼叫状态变化："+state + " " + message);
//            Log.e("SipPhone",call.getCallLog().toStr());
//            //被叫 call.getToAddress().getUsername()
//            Log.e("SipPhone", String.valueOf(call.getToAddress().getUsername()));
//            //呼叫方向 call.getDir()
//            Log.e("SipPhone", String.valueOf(call.getDir()));
//
//            //接续时长？ call.getDuration()
//            Log.e("SipPhone", String.valueOf(call.getDuration()));
//            Log.e("SipPhone", String.valueOf(call.getCallLog().getDuration()));
//            //开始时间 call.getCallLog().getStartDate()
//            Log.e("SipPhone", String.valueOf(call.getCallLog().getStartDate()));
//            //状态 Aborted=呼叫失败
//            Log.e("SipPhone", String.valueOf(call.getCallLog().getStatus()));

        }

        @Override
        public void onAccountRegistrationStateChanged(@NonNull Core core, @NonNull Account account, RegistrationState state, @NonNull String message) {
            super.onAccountRegistrationStateChanged(core, account, state, message);
            Log.e("SipPhone",Objects.requireNonNull(account.findAuthInfo()).getUsername()+"注册状态变化："+state + " " + message);
            if (state == RegistrationState.Ok){
                Log.e("SipPhone","注册成功");
                extensionStatus = "注册成功";
                setExtension(Objects.requireNonNull(account.findAuthInfo()).getUsername());
//                unregister();
//                call("18102202722",false);
            }else if(state == RegistrationState.Progress){
                Log.e("SipPhone","正在注册");
                extensionStatus = "正在注册";
            }else if(state == RegistrationState.Failed){
                Log.e("SipPhone","注册失败");
                extensionStatus = "注册失败";
            }else if(state == RegistrationState.Cleared){
                Log.e("SipPhone","退出注册");
                extensionStatus = "退出注册";
                setExtension("");
            }
            else{
                Log.e("SipPhone","其他错误："+message);
                extensionStatus = "其他错误";
            }
            ahListener.setLeftTitle(" "+Objects.requireNonNull(account.findAuthInfo()).getUsername(),extensionStatus);
        }
    };

    public void setLocalCall(String localPhoneNumber){
        ahListener.setLeftTitle(localPhoneNumber,"");
    }

    public void setLeftAction(Context context){
        PhoneSetting phoneSetting = PhoneData.getPhoneSetting(context);
        if(Objects.equals(phoneSetting.phoneSelect,LOCAL_CALL)){
            setLocalCall(phoneSetting.localPhoneNumber);
        }else if(Objects.equals(phoneSetting.phoneSelect, SIP_CALL)){
            ahListener.setLeftTitle(phoneSetting.extension,extensionStatus);
        }
    }
    //拨打电话
    public void call(Context context,String number, boolean video){
        Account account = core.getDefaultAccount();
        assert account != null;
        AccountParams accountParams = account.getParams();
        String domain = accountParams.getDomain();
//        int port = Objects.requireNonNull(accountParams.getIdentityAddress()).getPort();
        String port = "11889";
        // As for everything we need to get the SIP URI of the remote and convert it to an Address
        String remoteSipUri = "sip:" + number + "@" + domain;
        Address remoteAddress = factory.createAddress(remoteSipUri);
        if(remoteAddress == null)return;
        // If address parsing fails, we can't continue with outgoing call process

        // We also need a CallParams object
        // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
        CallParams params = core.createCallParams(null);

        // We can now configure it
        // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
        assert params != null;
        String path = context.getExternalFilesDir("recording")+"/"+String.valueOf(System.currentTimeMillis())+".wav";
        Log.e("SipPhone","存储目录："+path);
        params.setRecordFile(path);
        params.setMediaEncryption(MediaEncryption.None);
        params.setVideoEnabled(video);

        //show preview before caling.
        //core.enableVideoPreview(video);

        // Finally we start the call
        core.inviteAddressWithParams(remoteAddress, params);
        //回声消除
        // Call process can be followed in onCallStateChanged callback from core listener
    }

    //接听电话

    public void answer(){
        if(currentCall != null){
            currentCall.accept();
        }
    }

    //挂断电话
    public void hangup(){
        if (core.getCallsNb() == 0) return;
        // If the call state isn't paused, we can get it using core.currentCall
        Call call = core.getCurrentCall() != null ? core.getCurrentCall() : core.getCalls()[0];
        if(call != null) {
            // Terminating a call is quite simple
            call.terminate();
        }
    }

    //通话记录视图
    public void callLogNotify(CallHistory vv){
        if(callHangupListener != null){
            callHangupListener.notifyAddCallLog(vv);
        }
    }

    //设置扬声器
    public void speaker(boolean speaker){
        if(currentCall != null){
            AudioDevice currentAudioDevice = currentCall.getOutputAudioDevice();
            assert currentAudioDevice != null;
             AudioDevice[] audioDevices = core.getAudioDevices();
            for (AudioDevice audioDevice : audioDevices) {
                Log.e("SipPhone","音频："+audioDevice.getType());
                if (speaker && audioDevice.getType() == AudioDevice.Type.Speaker) {
                    currentCall.setOutputAudioDevice(audioDevice);
                    currentCall.setInputAudioDevice(audioDevice);
                } else if (!speaker && audioDevice.getType() == AudioDevice.Type.Earpiece) {
                    currentCall.setOutputAudioDevice(audioDevice);
                    currentCall.setInputAudioDevice(audioDevice);
                }
            }
            currentAudioDevice = currentCall.getOutputAudioDevice();
        }
    }
    //设置录音
    public void recording(boolean recording){
        if(currentCall != null){
            isRecording = recording;
            if (recording){
                currentCall.startRecording();
            }else{
                currentCall.stopRecording();
            }
        }
    }
}
