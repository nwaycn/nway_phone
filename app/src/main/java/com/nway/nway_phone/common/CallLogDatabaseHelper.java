package com.nway.nway_phone.common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nway.nway_phone.ui.call.CallHistory;

import java.util.ArrayList;
import java.util.List;

public class CallLogDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "call_log.db";
    private static final String TABLE_NAME = "call_log";
    private static final int DB_VERSION = 2;
    private static CallLogDatabaseHelper callLogDatabaseHelper = null;
    private static SQLiteDatabase mReadDatabase = null;
    private static SQLiteDatabase mWriteDatabase = null;

    public static CallLogDatabaseHelper getInstance(Context context){
        if (callLogDatabaseHelper == null){
            synchronized (CallLogDatabaseHelper.class){
                if (callLogDatabaseHelper == null){
                    callLogDatabaseHelper = new CallLogDatabaseHelper(context);
                }
            }
        }
        return callLogDatabaseHelper;
    }

    private CallLogDatabaseHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS call_log (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "callee VARCHAR(50) NOT NULL," +
                "caller VARCHAR(50)," +
                "call_date VARCHAR(50)," +
                "direction VARCHAR(50)," +
                "duration int(10)," +
                "bill INT(10)," +
                "hangup_cause VARCHAR(50)," +
                "record_file VARCHAR(200)," +
                "over INT(1));";
        Log.e("DB",sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //打开写链接
    public SQLiteDatabase openWriteLink() {
        if (mWriteDatabase == null || !mWriteDatabase.isOpen()) {
            mWriteDatabase = callLogDatabaseHelper.getWritableDatabase();
        }
        return mWriteDatabase;
    }

    //打开读链接
    public SQLiteDatabase openReadLink() {
        if (mReadDatabase == null || !mReadDatabase.isOpen()) {
            mReadDatabase = callLogDatabaseHelper.getReadableDatabase();
        }
        return mReadDatabase;
    }

    //关闭链接
    public void closeLink() {
        if (mReadDatabase != null && mReadDatabase.isOpen()) {
            mReadDatabase.close();
            mReadDatabase = null;
        }
        if (mWriteDatabase != null && mWriteDatabase.isOpen()) {
            mWriteDatabase.close();
            mWriteDatabase = null;
        }
    }

    //写入通话记录
    public long addCallLog(CallHistory callHistory){
        openWriteLink();
        ContentValues values = new ContentValues();
        //将要传入的参数以键值对的方式写入参数对象
        values.put("callee", callHistory.getCallee());
        values.put("caller", callHistory.getCaller());
        values.put("call_date", callHistory.getCallDate());
        values.put("direction", callHistory.getCallDirection());
        values.put("duration", callHistory.getCallDuration());
        values.put("bill", callHistory.getCallBill());
        values.put("hangup_cause", callHistory.getCallHangupCause());
        values.put("record_file", callHistory.getRecordFile());
        values.put("over", 0);
//        writableDatabase.insert(<1>,<2>,<3>);
//        参数分别为：表名，第三个参数为空时的默认字段名，ContentValues对象。
        long res = mWriteDatabase.insert(TABLE_NAME, null, values);
        closeLink();
        return res;
    }

    //读取通话记录
    public List<CallHistory> queryByPage(String number,int page,int limit){
        openReadLink();
        List<CallHistory> list = new ArrayList<>();
        String[] cl = new String[]{"callee","caller","call_date","direction","duration","bill","hangup_cause","record_file"};
        String selection = number==null ? null : String.format("callee like '%%%s%%'",number);
//        String groupBy = number==null ? null : "callee";
        String lm = String.format("%s,%s",(page-1)*limit,limit);
        // 执行记录查询动作，该语句返回结果集的游标
        Cursor cursor = mReadDatabase.query(TABLE_NAME, cl, selection, null, null, null, "id desc",lm);
        // 循环取出游标指向的每条记录
        while (cursor.moveToNext()) {
            CallHistory vv = new CallHistory(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getString(7));
            list.add(vv);
        }
        cursor.close();
        closeLink();
        return list;
    }
}
