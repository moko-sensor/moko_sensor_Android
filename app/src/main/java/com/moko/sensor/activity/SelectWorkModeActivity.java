package com.moko.sensor.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.SelectWorkModeActivity
 */
public class SelectWorkModeActivity extends BaseActivity {


    @Bind(R.id.rg_work_mode)
    RadioGroup rgWorkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_work_mode);
        ButterKnife.bind(this);
    }

    public void back(View view) {
        finish();
    }

    public void selectWorkDone(View view) {
        int id = rgWorkMode.getCheckedRadioButtonId();
        Intent i;
        switch (id) {
            case R.id.rb_work_mode_gprs:
                i = new Intent();
                i.putExtra("work_mode", 1);
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.rb_work_mode_wifi:
                i = new Intent();
                i.putExtra("work_mode", 2);
                setResult(RESULT_OK);
                finish();
                break;
        }
    }
}
