package com.nway.nway_phone.ui.call;

public class CallHistory {
    private String callee;
    private String caller;
    private String callDate;
    private String callDirection;
    private int callDuration;
    private int callBill;
    private String callHangupCause;
    private String recordFile;

    public CallHistory(){

    }

    public CallHistory(String callee,String callDate,String callDirection,int callBill,String callHangupCause){
        this.callee = callee;
        this.callDate = callDate;
        this.callDirection = callDirection;
        this.callBill = callBill;
        this.callHangupCause = callHangupCause;
    }

    public CallHistory(String callee,String caller,String callDate,String callDirection,int callDuration,int callBill,String callHangupCause){
        this.callee = callee;
        this.caller = caller;
        this.callDate = callDate;
        this.callDirection = callDirection;
        this.callDuration = callDuration;
        this.callBill = callBill;
        this.callHangupCause = callHangupCause;
    }

    public CallHistory(String callee,String caller,String callDate,String callDirection,int callDuration,int callBill,String callHangupCause,String recordFile){
        this.callee = callee;
        this.caller = caller;
        this.callDate = callDate;
        this.callDirection = callDirection;
        this.callDuration = callDuration;
        this.callBill = callBill;
        this.callHangupCause = callHangupCause;
        this.recordFile = recordFile;
    }

    public String getCallee(){
        return callee;
    }
    public void setCallee(String callee){
        this.callee = callee;
    }

    public String getCaller(){
        return caller;
    }
    public void setCaller(String caller){
        this.caller = caller;
    }


    public String getCallDate(){
        return callDate;
    }
    public void setCallDate(String callDate){
        this.callDate = callDate;
    }
    public String getCallDirection(){
        return callDirection;
    }
    public void setCallDirection(String callDirection){
        this.callDirection = callDirection;
    }
    public int getCallDuration(){
        return callDuration;
    }
    public void setCallDuration(int callDuration){
        this.callDuration = callDuration;
    }
    public int getCallBill(){
        return callBill;
    }
    public void setCallBill(int callBill){
        this.callBill = callBill;
    }
    public String getCallHangupCause(){
        return callHangupCause;
    }
    public void setCallHangupCause(String callHangupCause){
        this.callHangupCause = callHangupCause;
    }

    public String getRecordFile(){
        return recordFile;
    }
    public void setRecordFile(String recordFile){
        this.recordFile = recordFile;
    }

}
