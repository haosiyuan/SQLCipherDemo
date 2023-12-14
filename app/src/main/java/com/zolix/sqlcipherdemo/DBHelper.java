package com.zolix.sqlcipherdemo;

import android.content.Context;
import android.util.Log;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper implements DBConst{

    private static DBHelper dbHelper;
    public static SQLiteDatabase DATA_BASE_W;
    public static SQLiteDatabase DATA_BASE_R;
    public static DBHelper getInstance(Context context, String password){
        if(dbHelper == null){
            dbHelper = new DBHelper(context, password);
        }
        return dbHelper;
    }
    private DBHelper(Context context, String password) {
        super(context, DatabasePath, password, null, DatabaseVersion, 0, null, null, false);
        DATA_BASE_W = getWritableDatabase();
        DATA_BASE_R = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("DBHelper", "onCreate");
        db.execSQL("create table " + NAME_TABLE + " (" +
                _ID + " integer primary key autoincrement," +
                NAME + " varchar)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void closeDBHelper(){
        if(dbHelper != null){
            DATA_BASE_W.close();
            DATA_BASE_R.close();
            dbHelper.close();
            DATA_BASE_W = null;
            DATA_BASE_R = null;
            dbHelper = null;
        }
    }
}
