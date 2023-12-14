package com.zolix.sqlcipherdemo;


import android.content.Context;
import android.os.Environment;


import net.zetetic.database.sqlcipher.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class DBUtil {
    private static final String TAG = "DBUtil";
    /**
     * 判断数据库是否加密,如果没有加密，开头的六个字母必定是SQLite
     */
    public static boolean DatabaseIsEncrypted(String DB_PATH) throws IOException {
        FileInputStream in = new FileInputStream(DB_PATH);

        byte[] buffer = new byte[6];
        in.read(buffer, 0, 6);

        if (Arrays.equals(buffer, ("SQLite").getBytes())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 数据库加密
     * 修改DBHelper的数据库
     */
    public static void DatabaseEncrypted(Context context, String oldPwd, String newPwd) {
        //打开数据库
        SQLiteDatabase dataSql = SQLiteDatabase.openOrCreateDatabase(DBHelper.DatabasePath, oldPwd, null, null);
        //设置加密的数据库文件，如果原来有则删除文件
        String encryptedName = "encrypted.db";
        String encryptedPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File encryptedFile = new File(encryptedPath, encryptedName);
        if (encryptedFile.exists()) {
            encryptedFile.delete();
        }
        //创建加密数据库
        SQLiteDatabase encryptedDatabase = SQLiteDatabase.openOrCreateDatabase(
                encryptedPath + File.separator + encryptedName, newPwd, null, null, null);
        //设置好版本号，和原来的要一致
        encryptedDatabase.setVersion(DBConst.DatabaseVersion);
        //从原来的数据库内提取出数据，写入新的加密数据库内
        dataSql.execSQL("ATTACH DATABASE '" + encryptedFile.getAbsolutePath() + "' AS encrypted KEY '" + newPwd + "'");
        try {
            dataSql.execSQL("SELECT sqlcipher_export('encrypted')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSql.execSQL("DETACH DATABASE encrypted");
        //加密完了关闭数据库
        dataSql.close();
        encryptedDatabase.close();
        DBHelper.closeDBHelper();
        //将原来的数据库删了
        File databaseFile = new File(dataSql.getPath());
        databaseFile.delete();
        //将加密数据库改名为数据库的名称
        encryptedFile.renameTo(new File(DBConst.DatabasePath));
        //重新加载数据库
        DBHelper.getInstance(context, newPwd);
    }


    /**
     * 数据库解密
     * 修改DBHelper的数据库
     */
    public static void DatabaseUnencrypted(Context context, String oldPwd, String newPwd) {
        //打开数据库
        SQLiteDatabase dataSql = SQLiteDatabase.openOrCreateDatabase(DBHelper.DatabasePath, oldPwd, null, null);
        //设置解密的数据库文件，如果原来有则删除文件
        String plaintextName = "plaintext.db";
        String plaintextPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File plaintextFile = new File(plaintextPath, plaintextName);
        if (plaintextFile.exists()) {
            plaintextFile.delete();
        }
        SQLiteDatabase plaintextDatabase = SQLiteDatabase.openOrCreateDatabase(
                plaintextPath + File.separator + plaintextName, "", null, null, null);
        plaintextDatabase.setVersion(DBConst.DatabaseVersion);
        dataSql.rawQuery("PRAGMA key = '"+oldPwd+"'");
        dataSql.execSQL("ATTACH DATABASE '" + plaintextFile.getAbsolutePath() + "' AS plaintext KEY ''");
        try {
            dataSql.execSQL("SELECT sqlcipher_export('plaintext')");
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataSql.execSQL("DETACH DATABASE plaintext");
        //解密完了关闭数据库
        dataSql.close();
        plaintextDatabase.close();
        DBHelper.closeDBHelper();
        //将原来的数据库删除
        File databaseFile = new File(dataSql.getPath());
        databaseFile.delete();
        //将解密数据库改名为数据库的名称
        plaintextFile.renameTo(new File(DBConst.DatabasePath));
        //重新加载数据库
        DBHelper.getInstance(context, "");
    }


    /**
     * 数据库改密码
     */
    public static void DatabaseChangePassword(String oldPassword, String newPassword) {
        DBHelper.DATA_BASE_W.rawQuery("PRAGMA key = '" + oldPassword + "'");
        DBHelper.DATA_BASE_W.rawQuery("PRAGMA rekey = '" + newPassword + "'");
    }

}
