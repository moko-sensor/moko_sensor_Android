package com.moko.sensor.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.sensor.AppConstants;
import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;
import com.moko.sensor.entity.MQTTConfig;
import com.moko.sensor.service.MokoService;
import com.moko.sensor.utils.SPUtiles;
import com.moko.sensor.utils.ToastUtils;
import com.moko.support.MokoConstants;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.SetAppMqttActivity
 */
public class SetAppMqttActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    @Bind(R.id.et_mqtt_host)
    EditText etMqttHost;
    @Bind(R.id.et_mqtt_port)
    EditText etMqttPort;
    @Bind(R.id.iv_clean_session)
    ImageView ivCleanSession;
    @Bind(R.id.rg_conn_mode)
    RadioGroup rgConnMode;
    @Bind(R.id.tv_qos)
    TextView tvQos;
    @Bind(R.id.et_mqtt_client_id)
    EditText etMqttClientId;
    @Bind(R.id.et_mqtt_username)
    EditText etMqttUsername;
    @Bind(R.id.et_mqtt_password)
    EditText etMqttPassword;
    @Bind(R.id.et_keep_alive)
    EditText etKeepAlive;
    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.rl_client_id)
    RelativeLayout rlClientId;

    private String[] mQosArray = new String[]{"0", "1", "2"};


    private MQTTConfig mqttConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_device);
        ButterKnife.bind(this);
        String mqttConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        title.setText(R.string.settings_mqtt_app);
        rlClientId.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(mqttConfigStr)) {
            mqttConfig = new MQTTConfig();
            mqttConfig.clientId = UUID.randomUUID().toString().replaceAll("-", "");
        } else {
            Gson gson = new Gson();
            mqttConfig = gson.fromJson(mqttConfigStr, MQTTConfig.class);
        }
        initData();
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
                if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS) {
                    ToastUtils.showToast(SetAppMqttActivity.this, getString(R.string.success));
                    String mqttConfigStr = SPUtiles.getStringValue(SetAppMqttActivity.this, AppConstants.SP_KEY_MQTT_CONFIG, "");
                    if (TextUtils.isEmpty(mqttConfigStr)) {
                        startActivity(new Intent(SetAppMqttActivity.this, SetDeviceMqttActivity.class));
                    }
                    dismissLoadingProgressDialog();
                    SetAppMqttActivity.this.finish();
                }
            }
        }
    };

    private void initData() {
        etMqttHost.setText(mqttConfig.host);
        etMqttHost.setSelection(mqttConfig.host.length());
        etMqttPort.setText(mqttConfig.port);
        tvQos.setText(mQosArray[mqttConfig.qos]);
        ivCleanSession.setImageDrawable(ContextCompat.getDrawable(this, mqttConfig.cleanSession ? R.drawable.checkbox_open : R.drawable.checkbox_close));
        rgConnMode.check(mqttConfig.connectMode == 0 ? R.id.rb_conn_mode_tcp : R.id.rb_conn_mode_ssl);
        rgConnMode.setOnCheckedChangeListener(this);
        etKeepAlive.setText(mqttConfig.keepAlive + "");
        etMqttClientId.setText(mqttConfig.clientId);
        etMqttUsername.setText(mqttConfig.username);
        etMqttPassword.setText(mqttConfig.password);
    }

    public void back(View view) {
        finish();
    }

    public void clearSettings(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Clear All Parameters")
                .setMessage("Please confirm whether to clear all parameters?")
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mqttConfig.reset();
                        initData();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    public void checkQos(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setSingleChoiceItems(mQosArray, mqttConfig.qos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mqttConfig.qos = which;
                        tvQos.setText(mQosArray[mqttConfig.qos]);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    public void saveSettings(View view) {
        mqttConfig.host = etMqttHost.getText().toString().replaceAll(" ", "");
        mqttConfig.port = etMqttPort.getText().toString();
        String keepAlive = etKeepAlive.getText().toString();
        mqttConfig.keepAlive = Integer.parseInt(TextUtils.isEmpty(keepAlive) ? "0" : keepAlive);
        mqttConfig.clientId = etMqttClientId.getText().toString().replaceAll(" ", "");
        mqttConfig.username = etMqttUsername.getText().toString().replaceAll(" ", "");
        mqttConfig.password = etMqttPassword.getText().toString().replaceAll(" ", "");
        if (mqttConfig.isError(this)) {
            return;
        }
        String clientId = etMqttClientId.getText().toString();
        if (TextUtils.isEmpty(clientId)) {
            ToastUtils.showToast(this, getString(R.string.mqtt_verify_client_id_empty));
            return;
        }
        String mqttConfigStr = new Gson().toJson(mqttConfig, MQTTConfig.class);
        SPUtiles.setStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, mqttConfigStr);
        stopService(new Intent(this, MokoService.class));
        showLoadingProgressDialog(getString(R.string.mqtt_connecting));
        etKeepAlive.postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(new Intent(SetAppMqttActivity.this, MokoService.class));
            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void cleanSession(View view) {
        mqttConfig.cleanSession = !mqttConfig.cleanSession;
        ivCleanSession.setImageDrawable(ContextCompat.getDrawable(this, mqttConfig.cleanSession ? R.drawable.checkbox_open : R.drawable.checkbox_close));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.rb_conn_mode_tcp:
                mqttConfig.connectMode = 0;
                mqttConfig.port = "1883";
                break;
            case R.id.rb_conn_mode_ssl:
                mqttConfig.connectMode = 1;
                mqttConfig.port = "8883";
                break;
        }
        etMqttPort.setText(mqttConfig.port);
        etMqttPort.setSelection(mqttConfig.port.length());
    }
}
