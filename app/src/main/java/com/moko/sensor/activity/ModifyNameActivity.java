package com.moko.sensor.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.moko.sensor.AppConstants;
import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;
import com.moko.sensor.db.DBTools;
import com.moko.sensor.entity.MokoDevice;
import com.moko.sensor.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.ModifyNameActivity
 */
public class ModifyNameActivity extends BaseActivity {
    public static String TAG = "ModifyNameActivity";

    @Bind(R.id.et_nick_name)
    EditText etNickName;
    private MokoDevice device;

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_device_name);
        ButterKnife.bind(this);
        device = (MokoDevice) getIntent().getSerializableExtra("mokodevice");
        etNickName.setText(device.nickName);
        etNickName.setSelection(etNickName.getText().toString().length());
        etNickName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(20)});
        etNickName.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) etNickName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etNickName, 0);
            }
        }, 300);
    }


    public void modifyDone(View view) {
        String nickName = etNickName.getText().toString();
        if (TextUtils.isEmpty(nickName)) {
            ToastUtils.showToast(this, R.string.modify_device_name_empty);
            return;
        }
        device.nickName = nickName;
        DBTools.getInstance(this).updateDevice(device);
        // 跳转首页，刷新数据
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY, TAG);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
