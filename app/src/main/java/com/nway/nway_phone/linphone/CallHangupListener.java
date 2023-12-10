package com.nway.nway_phone.linphone;

import com.nway.nway_phone.ui.call.CallHistory;

public interface CallHangupListener {
    void notifyAddCallLog(CallHistory callHistory);
}
