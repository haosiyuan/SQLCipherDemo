package com.zolix.sqlcipherdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String[] needPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //在使用之前，加载SQLCipher
        System.loadLibrary("sqlcipher");

        initView();
        initData();
        setView();
        checkPermissions(needPermissions);
    }

    private EditText database_pwd_et, name_et, new_pwd_et;
    private Button open_database_bt, add_bt, refresh_bt, update_pwd_bt;
    private ListView list;
    private void initView() {
        database_pwd_et = findViewById(R.id.database_pwd_et);
        name_et = findViewById(R.id.name_et);
        open_database_bt = findViewById(R.id.open_database_bt);
        add_bt = findViewById(R.id.add_bt);
        refresh_bt = findViewById(R.id.refresh_bt);
        list = findViewById(R.id.list);
        new_pwd_et = findViewById(R.id.new_pwd_et);
        update_pwd_bt = findViewById(R.id.update_pwd_bt);
    }

    private List<Name> names;
    private ListAdapter listAdapter;
    private void initData() {
        names = new ArrayList<>();
        listAdapter = new ListAdapter(this,names);
    }

    private void setView() {
        list.setAdapter(listAdapter);
        open_database_bt.setOnClickListener(this);
        add_bt.setOnClickListener(this);
        refresh_bt.setOnClickListener(this);
        update_pwd_bt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == open_database_bt.getId()){
            try{
                String s = database_pwd_et.getText().toString();
                DBHelper.getInstance(this, s);
                names.clear();
                names.addAll(NameDao.QueryNames());
                listAdapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId() == add_bt.getId()){
            String s = name_et.getText().toString();
            if(s.isEmpty()){
                Toast.makeText(this, "名字不能为空", Toast.LENGTH_SHORT).show();
            }else{
                NameDao.InsertName(s);
                Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                names.clear();
                names.addAll(NameDao.QueryNames());
                listAdapter.notifyDataSetChanged();
                name_et.setText("");
            }
        }else if(v.getId() == refresh_bt.getId()){
            try {
                names.clear();
                names.addAll(NameDao.QueryNames());
                listAdapter.notifyDataSetChanged();
            }catch (Exception e){
                Toast.makeText(this, "数据库未打开", Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId() == update_pwd_bt.getId()){
            String oldPwd = database_pwd_et.getText().toString();
            String newPwd = new_pwd_et.getText().toString();
            if(oldPwd.isEmpty() && !newPwd.isEmpty()){
                //原来没密码，现在有密码，采用加密功能
                DBUtil.DatabaseEncrypted(this, oldPwd, newPwd);
            }else if(!oldPwd.isEmpty() && !newPwd.isEmpty()){
                //原来有密码，新的有密码，采用修改密码功能
                DBUtil.DatabaseChangePassword(oldPwd, newPwd);
            }else if(!oldPwd.isEmpty() && newPwd.isEmpty()){
                //原来有密码，现在没密码，采用解密功能
                DBUtil.DatabaseUnencrypted(this, oldPwd, newPwd);
            }
            Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 下面部分是动态授权功能
     * @param permissions
     * @since 2.5.0
     */
    public void checkPermissions(String... permissions) {
        try {
            if (getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (needRequestPermissonList.size() > 0) {
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    Method method = getClass().getMethod("requestPermissions", new Class[]{String[].class, int.class});
                    method.invoke(this, array, 0);
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (getApplicationInfo().targetSdkVersion >= 23) {
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer) checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean) shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                        needRequestPermissonList.add(perm);
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // 先判断有没有权限
                    if (!Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + getPackageName()));
//                        startActivityForResult(intent, 1024);
                        startActivity(intent);
                    }
                }

            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    /**
     * 检测是否所有的权限都已经授权
     *
     * @param grantResults
     * @return
     * @since 2.5.0
     */
    public boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}