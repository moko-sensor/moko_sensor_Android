package com.moko.sensor.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.moko.support.log.LogModule;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "moko_sensor";
    // 数据库版本号
    private static final int DB_VERSION = 1;

    private Context mContext;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DEVICE);
        LogModule.i("创建数据库");
    }

    /**
     * 升级时数据库时调用
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogModule.i("数据库升级");
    }

    /**
     * 删除数据库
     *
     * @param context
     * @return
     */
    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(DB_NAME);
    }

    // 设备表
    private static final String CREATE_TABLE_DEVICE = "CREATE TABLE "
            + DBConstants.TABLE_NAME_DEVICE
            // id
            + " (" + DBConstants.DEVICE_FIELD_ID
            + " INTEGER primary key autoincrement, "
            // 功能
            + DBConstants.DEVICE_FIELD_FUNCTION + " TEXT,"
            // 名字
            + DBConstants.DEVICE_FIELD_NAME + " TEXT,"
            // 昵称
            + DBConstants.DEVICE_FIELD_NICK_NAME + " TEXT,"
            // 规格
            + DBConstants.DEVICE_FIELD_SPECIFICATIONS + " TEXT,"
            // 类型
            + DBConstants.DEVICE_FIELD_TYPE + " TEXT,"
            // MAC
            + DBConstants.DEVICE_FIELD_MAC + " TEXT);";

}
