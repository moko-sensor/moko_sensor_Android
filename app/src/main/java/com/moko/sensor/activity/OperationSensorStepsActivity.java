package com.moko.sensor.activity;

import android.os.Bundle;
import android.view.View;

import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;

import butterknife.ButterKnife;

/**
 * @Date 2018/6/11
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.OperationSensorStepsActivity
 */
public class OperationSensorStepsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_operation_steps);
        ButterKnife.bind(this);
    }

    public void back(View view) {
        finish();
    }

    public void plugBlinking(View view) {
        setResult(RESULT_OK);
        finish();
    }
}
