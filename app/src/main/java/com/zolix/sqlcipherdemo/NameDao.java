package com.zolix.sqlcipherdemo;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class NameDao implements DBConst {

    public static void InsertName(String name) {
        DBHelper.DATA_BASE_W.execSQL("insert into " + NAME_TABLE + "(" + NAME + ") values(?)",
                new Object[]{name});
    }

    public static void  DeleteNameForID(int _id){
        DBHelper.DATA_BASE_W.execSQL("delete from " + NAME_TABLE + " where " + _ID + "=?",new Object[]{_id});
    }

    public static List<Name> QueryNames(){
        List<Name> names = new ArrayList<>();
        Cursor cursor = DBHelper.DATA_BASE_R.rawQuery("select "+_ID + "," + NAME + " from " + NAME_TABLE);
        while (cursor.moveToNext()){
            Name name = new Name(cursor.getInt(0), cursor.getString(1));
            names.add(name);
        }
        cursor.close();
        return names;
    }
}
