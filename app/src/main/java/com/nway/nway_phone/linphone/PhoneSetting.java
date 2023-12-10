package com.nway.nway_phone.linphone;

public class PhoneSetting {
    public String phoneSelect;
    public String localPhoneNumber;
    public boolean autoRecord;
    public String localRecordingPath;
    public String extension;
    public String password;
    public String domain;
    public String port;
    public String server;
    public String status;

    public PhoneSetting(String phoneSelect,String localPhoneNumber,boolean autoRecord,String localRecordingPath,String extension,String password,String domain,String port,String server){
        this.phoneSelect = phoneSelect;
        this.localPhoneNumber = localPhoneNumber;
        this.autoRecord = autoRecord;
        this.localRecordingPath = localRecordingPath;
        this.extension = extension;
        this.password = password;
        this.domain = domain;
        this.port = port;
        this.server = server;
    }

    public void updateLocalCall(String phoneSelect,String localPhoneNumber,boolean autoRecord,String localRecordingPath){
        this.phoneSelect = phoneSelect;
        this.localPhoneNumber = localPhoneNumber;
        this.autoRecord = autoRecord;
        this.localRecordingPath = localRecordingPath;
    }

    public void updateSipCall(String phoneSelect,String extension,String password,String domain,String port,String server){
        this.phoneSelect = phoneSelect;
        this.extension = extension;
        this.password = password;
        this.domain = domain;
        this.port = port;
        this.server = server;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
