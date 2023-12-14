package com.zolix.sqlcipherdemo;

import android.os.Environment;

import java.io.File;

public interface DBConst {
    String DatabasePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"database.db";
    int DatabaseVersion = 1;

    String NAME_TABLE = "name_table";
    String _ID = "_id";
    String NAME = "name";
}
