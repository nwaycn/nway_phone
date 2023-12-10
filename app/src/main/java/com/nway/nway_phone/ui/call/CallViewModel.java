package com.nway.nway_phone.ui.call;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CallViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public CallViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is call fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}