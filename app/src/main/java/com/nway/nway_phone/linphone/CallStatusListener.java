package com.nway.nway_phone.linphone;

import androidx.annotation.NonNull;

import org.linphone.core.Call;

public interface CallStatusListener {
    void setCallStatus(@NonNull Call call, String status);

}
