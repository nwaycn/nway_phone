package com.nway.nway_phone;

import com.nway.nway_phone.linphone.PhoneSetting;

public interface AhListener {
    void setTitleBarTitle(String title);
    void setLeftTitle(String title,String status);
    void setPhoneSetting(PhoneSetting phoneSetting);
    void readyToCall(String number);
}
