package com.nway.nway_phone.ui.call;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.nway.nway_phone.AhListener;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.nway.nway_phone.R;

import com.nway.nway_phone.common.CallLogDatabaseHelper;
import com.nway.nway_phone.common.MyUtils;
import com.nway.nway_phone.common.PhoneData;
import com.nway.nway_phone.linphone.CallHangupListener;
import com.nway.nway_phone.linphone.PhoneSetting;
import com.nway.nway_phone.linphone.SipPhone;
import com.nway.nway_phone.databinding.FragmentCallBinding;
import com.xuexiang.xui.utils.XToastUtils;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CallFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener, CallHangupListener {

    private final static String TAG = "CallLog";
    private final String PHONE_SETTING = "phone_setting";
    private final String LOCAL_CALL = "localCall";
    private final String SIP_CALL = "sipCall";
    private PhoneSetting phoneSetting;
    private String phoneSelect;
    private FragmentCallBinding binding;
    private AhListener ahListener;
    private MaterialSearchBar materialSearchBar;
    private int page=1;
    private String searchNumber = null;
    private List<CallHistory> callHistoryList;
    private CallHistoryAdapter callHistoryAdapter;
    private Handler handlerDial ;
    private View dialpad;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CallViewModel callViewModel =
                new ViewModelProvider(this).get(CallViewModel.class);

        binding = FragmentCallBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        materialSearchBar = binding.searchBar;
        materialSearchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dialpadHidden();
                return false;
            }
        });

        //设置搜索框
        setSearchLayout();

        binding.recyclerCallHistory.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dialpadHidden();
                keyboardHidden(v);
                return false;
            }
        });

        //拨号盘的显示隐藏
        binding.numPadBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
            @Override
            public void onClick(View v) {
                int visibility = dialpad.getVisibility();
                if (visibility == View.VISIBLE) {
                    // View可见
                    dialpad.setVisibility(View.INVISIBLE);
                    dialpadDownAnimation(dialpad);
                    binding.numPadBtn.setBackground(getResources().getDrawable(R.drawable.circle_blue));
                    binding.numPadBtn.setImageResource(R.drawable.ic_dialpad_white_24dp);
                } else if (visibility == View.INVISIBLE) {
                    // View不可见，但仍占据空间
                    dialpad.setVisibility(View.VISIBLE);
                    dialpadUpAnimation(dialpad);
                    binding.numPadBtn.setBackground(getResources().getDrawable(R.drawable.circle_white));
                    binding.numPadBtn.setImageResource(R.drawable.ic_baseline_dialpad_24);
                } else if (visibility == View.GONE) {
                    // View不可见，且不占据空间
                }

            }
        });

        //处理拨号显示
        dialpad =  root.findViewById(R.id.in_dialpad);
        View display = dialpad.findViewById(R.id.in_display);
        handlerDial = new Handler(Looper.myLooper()){
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                TextView dial_display = display.findViewById(R.id.formula);
                String currentNumber = dial_display.getText().toString();
                if (msg.what == 0){
                    //add一个输入的号码
                    currentNumber += (String) msg.obj;
                    dial_display.setText(currentNumber);
                }else if(msg.what == 1){
                    if(phoneSetting.phoneSelect == null){
                        XToastUtils.toast("请先设置拨号线路");
                        showPhoneSetting();
                    }
                    //发起呼叫一个号码,并清空输入栏
                    if(!currentNumber.equals("")){
                        ahListener.readyToCall(currentNumber);
                    }
                    currentNumber = "";
                    dial_display.setText(currentNumber);
                }else if(msg.what == 3){
                    //栅格一个输入的号码
                    if(currentNumber.length() > 0){
                        dial_display.setText(currentNumber.substring(0, currentNumber.length() - 1));
                    }
                }
                else if(msg.what == 4){
                    showPhoneSetting();
                }
                if(dial_display.getText().toString().length() > 0 && display.getVisibility()==View.INVISIBLE){
                    display.setVisibility(View.VISIBLE);
                }else if(dial_display.getText().toString().length() == 0 && display.getVisibility()==View.VISIBLE){
                    display.setVisibility(View.INVISIBLE);
                }
            }
        };
        ImageButton imageButton = display.findViewById(R.id.del_number);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.what = 3;
                handlerDial.sendMessage(message);
            }
        });

        //拨号盘按键监听
        ViewGroup numberPad = binding.getRoot().findViewById(R.id.number_pad);
        for(int i=0;i<numberPad.getChildCount();i++){
            numberPad.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NonConstantResourceId")
                @Override
                public void onClick(View v) {
                    Message message = new Message();
                    message.what = 0;
                    String numberText = "";
                    switch (v.getId()){
                        case R.id.button1:
                            numberText = "1";
                            break;
                        case R.id.button2:
                            numberText = "2";
                            break;
                        case R.id.button3:
                            numberText = "3";
                            break;
                        case R.id.button4:
                            numberText = "4";
                            break;
                        case R.id.button5:
                            numberText = "5";
                            break;
                        case R.id.button6:
                            numberText = "6";
                            break;
                        case R.id.button7:
                            numberText = "7";
                            break;
                        case R.id.button8:
                            numberText = "8";
                            break;
                        case R.id.button9:
                            numberText = "9";
                            break;
                        case R.id.button10:
                            numberText = "*";
                            break;
                        case R.id.button11:
                            numberText = "0";
                            break;
                        case R.id.button12:
                            numberText = "#";
                            break;
                        case R.id.phone_setting:
                            message.what = 4;
                            numberText = "setting";
                            break;
                        case R.id.call_button:
                            message.what = 1;
                            numberText = "Outgoing";
                            break;
                    }
                    message.obj = numberText;
                    //把消息发送给处理器
                    handlerDial.sendMessage(message);
                }
            });
        }

        initCallHistory();
        refresh();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ahListener.setTitleBarTitle("通话");
        SipPhone sp = SipPhone.getInstance();
        sp.setLeftAction(requireContext());

    }

    //拨号盘显示隐藏处理
    @SuppressLint("UseCompatLoadingForDrawables")
    public void dialpadHidden(){
        int visibility = dialpad.getVisibility();
        if (visibility == View.VISIBLE) {
            // View可见
            dialpad.setVisibility(View.INVISIBLE);
            dialpadDownAnimation(dialpad);
            binding.numPadBtn.setBackground(getResources().getDrawable(R.drawable.circle_blue));
            binding.numPadBtn.setImageResource(R.drawable.ic_dialpad_white_24dp);
        }
    }
    //收起软键盘
    public void keyboardHidden(View v){
        boolean searchFocus = binding.searchBar.hasFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                if (searchFocus) {
//                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
//                } else {
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                }
    }

    public void dialpadUpAnimation(View dialpad){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                dialpad.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(400);
        animate.setFillAfter(true);
        dialpad.startAnimation(animate);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialpad.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    public void dialpadDownAnimation(View dialpad){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                dialpad.getHeight()); // toYDelta
        animate.setDuration(400);
        animate.setFillAfter(true);
        dialpad.startAnimation(animate);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dialpad.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void initCallHistory(){
        this.callHistoryList = getListData(null);
        RecyclerView recyclerView = binding.recyclerCallHistory;
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);

        this.callHistoryAdapter = new CallHistoryAdapter(requireContext(),callHistoryList);
        callHistoryAdapter.setItemOnClickListener(new CallHistoryAdapter.ItemOnClickListener() {
            @Override
            public void onDetailClick(View view, int i) {
                dialpadHidden();

                Bundle bundle = new Bundle();
                bundle.putString("callee",callHistoryList.get(i).getCallee());
               NavController navController = Navigation.findNavController(binding.getRoot());
               navController.navigate(R.id.action_navigation_call_to_navigation_call_detail,bundle);


            }

            @Override
            public void onClick(View view, int i) {
                dialpadHidden();
                ahListener.readyToCall(callHistoryList.get(i).getCallee());
            }

            @Override
            public void onLongClick(View view, int i) {
                dialpadHidden();
//                showContextMenuDialog(callHistoryList.get(i).getCallee());
            }
        });
        recyclerView.setAdapter(callHistoryAdapter);
    }

    public List<CallHistory> getListData(String number){
        List<CallHistory> callHistoryList = new ArrayList<>();
        int pageLimit = 10;
        callHistoryList = CallLogDatabaseHelper.getInstance(getContext()).queryByPage(number,page, pageLimit);
        return callHistoryList;
    }

    public void refresh(){
        //下拉刷新和上拉加载
        RefreshLayout refreshLayout = binding.refreshLayout;
        refreshLayout.setRefreshHeader(new ClassicsHeader(requireContext()));
        refreshLayout.setRefreshFooter(new ClassicsFooter(requireContext()));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                callHistoryAdapter.refresh(getListData(searchNumber));
                refreshLayout.finishRefresh();
//                refreshLayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page += 1;
                List<CallHistory> listData = getListData(searchNumber);
                callHistoryAdapter.add(listData);
                refreshLayout.setNoMoreData(listData.size() == 0);
                refreshLayout.finishLoadMore();
//                refreshLayout.finishLoadMore(2000/*,false*/);//传入false表示加载失败
            }
        });
    }


    public void setSearchLayout(){
//        materialSearchBar.setHint("Custom hint");
//        materialSearchBar.setSpeechMode(false);
        materialSearchBar.setOnSearchActionListener(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AhListener) {
            ahListener = (AhListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListener");
        }
        //获取电话设置
        phoneSetting = PhoneData.getPhoneSetting(requireContext());
        SipPhone sp = SipPhone.getInstance();
        sp.initCallHangupListener(this);
    }


    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        String s = enabled ? "enabled" : "disabled";
        Log.d("Search","search changed:"+s);
        if(!enabled){
            searchNumber = null;
            callHistoryAdapter.refresh(getListData(null));
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
//        startSearch(text.toString(), true, null, true);
        Log.d("Search",text.toString());
        searchNumber = text.toString();
        page = 1;
        callHistoryAdapter.refresh(getListData(text.toString()));
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode){
            case MaterialSearchBar.BUTTON_NAVIGATION:
                Log.d("Search","click button "+buttonCode);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                Log.d("Search","click button 2"+buttonCode);
                break;
        }
    }


    @Override
    public void notifyAddCallLog(CallHistory callHistory) {
        callHistoryAdapter.addOnHangup(callHistory);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerCallHistory.getLayoutManager();
        if (linearLayoutManager != null) {
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (firstVisibleItemPosition == 0){
                linearLayoutManager.scrollToPosition(0);
            }
        }
    }

    public void showPhoneSetting(){
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_sip_setting,
                null);
        new MaterialDialog.Builder(requireContext())
                .customView(layout, true)
                .title(R.string.phone_setting)
                .positiveText(R.string.confirm)
                .showListener(dialog -> {
                    if(phoneSetting.phoneSelect != null){
                        LinearLayout localSettingGroup = layout.findViewById(R.id.local_setting_group);
                        LinearLayout sipSettingGroup = layout.findViewById(R.id.sip_setting_group);
                        if(Objects.equals(phoneSetting.phoneSelect, LOCAL_CALL)){
                            RadioButton radioButton = layout.findViewById(R.id.local_call);
                            radioButton.setChecked(true);
                            localSettingGroup.setVisibility(View.VISIBLE);
                            sipSettingGroup.setVisibility(View.GONE);

                        }else if(Objects.equals(phoneSetting.phoneSelect, SIP_CALL)){
                            RadioButton radioButton = layout.findViewById(R.id.sip_call);
                            radioButton.setChecked(true);
                            localSettingGroup.setVisibility(View.GONE);
                            sipSettingGroup.setVisibility(View.VISIBLE);
                        }
                        TextView tv_localPhoneNumber = layout.findViewById(R.id.local_phone_number);
                        tv_localPhoneNumber.setText(phoneSetting.localPhoneNumber);
                        SwitchMaterial sm_autoRecord = layout.findViewById(R.id.auto_record);
                        sm_autoRecord.setChecked(phoneSetting.autoRecord);
                        TextView tv_localRecordingPath = layout.findViewById(R.id.local_recording_path);
                        if(Objects.equals(phoneSetting.localRecordingPath, "")){
                            phoneSetting.localRecordingPath = MyUtils.localRecordPath();
                        }
                        tv_localRecordingPath.setText(phoneSetting.localRecordingPath);
                        TextView tv_extension = layout.findViewById(R.id.extension);
                        tv_extension.setText(phoneSetting.extension);
                        TextView tv_password = layout.findViewById(R.id.password);
                        tv_password.setText(phoneSetting.password);
                        TextView tv_domain = layout.findViewById(R.id.domain);
                        tv_domain.setText(phoneSetting.domain);
                        TextView tv_port = layout.findViewById(R.id.port);
                        tv_port.setText(phoneSetting.port);
                        TextView tv_server = layout.findViewById(R.id.server);
                        tv_server.setText(phoneSetting.server);
                    }else{
                        //初次打开设置时的录音路径初始化
                        TextView tv_localRecordingPath = layout.findViewById(R.id.local_recording_path);
                        if(Objects.equals(phoneSetting.localRecordingPath, "")){
                            phoneSetting.localRecordingPath = MyUtils.localRecordPath();
                        }
                        tv_localRecordingPath.setText(phoneSetting.localRecordingPath);
                    }
                })
                .onPositive((dialog, which) -> {
                    assert dialog.getCustomView() != null;
                    if(phoneSelect == null){
                        XToastUtils.toast("请选择线路");
                        return;
                    }
                    if(Objects.equals(phoneSelect, LOCAL_CALL)){
                        TextView tv_localPhoneNumber = dialog.getCustomView().findViewById(R.id.local_phone_number);
                        String localPhone = tv_localPhoneNumber.getText().toString();
                        SwitchMaterial sm_autoRecord = dialog.getCustomView().findViewById(R.id.auto_record);
                        boolean autoRecord = sm_autoRecord.isChecked();
                        TextView tv_localRecordingPath = dialog.getCustomView().findViewById(R.id.local_recording_path);
                        String localRecordingPath = tv_localRecordingPath.getText().toString();

                        phoneSetting.updateLocalCall(phoneSelect,localPhone,autoRecord,localRecordingPath);
                    }else if(Objects.equals(phoneSelect,SIP_CALL)){
                        TextView tv_extension = dialog.getCustomView().findViewById(R.id.extension);
                        String extension = tv_extension.getText().toString();
                        TextView tv_password = dialog.getCustomView().findViewById(R.id.password);
                        String password = tv_password.getText().toString();
                        TextView tv_domain = dialog.getCustomView().findViewById(R.id.domain);
                        String domain = tv_domain.getText().toString();
                        TextView tv_port = dialog.getCustomView().findViewById(R.id.port);
                        String port = tv_port.getText().toString();
                        TextView tv_server = dialog.getCustomView().findViewById(R.id.server);
                        String server = tv_server.getText().toString();
                        phoneSetting.updateSipCall(phoneSelect,extension,password,domain,port,server);
                    }
                    PhoneData.savePhoneSetting(requireContext(),phoneSetting);
                    //通知activity里的变量更新
                    ahListener.setPhoneSetting(phoneSetting);
                })
                .negativeText(R.string.cancel)
                .show();

        RadioGroup radioGroup = layout.findViewById(R.id.phone_select);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                LinearLayout localSettingGroup = layout.findViewById(R.id.local_setting_group);
                LinearLayout sipSettingGroup = layout.findViewById(R.id.sip_setting_group);
                if (checkedId == R.id.local_call){
                    localSettingGroup.setVisibility(View.VISIBLE);
                    sipSettingGroup.setVisibility(View.GONE);
                    phoneSelect = LOCAL_CALL;
                }else if(checkedId == R.id.sip_call){
                    sipSettingGroup.setVisibility(View.VISIBLE);
                    localSettingGroup.setVisibility(View.GONE);
                    phoneSelect = SIP_CALL;

                }
            }
        });
    }
}