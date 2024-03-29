package com.moko.sensor.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.moko.sensor.AppConstants;
import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;
import com.moko.sensor.db.DBTools;
import com.moko.sensor.dialog.CustomDialog;
import com.moko.sensor.entity.MQTTConfig;
import com.moko.sensor.entity.MokoDevice;
import com.moko.sensor.utils.SPUtiles;
import com.moko.sensor.utils.ToastUtils;
import com.moko.sensor.utils.Utils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.DeviceResponse;
import com.moko.support.entity.DeviceResult;
import com.moko.support.log.LogModule;
import com.moko.support.service.SocketService;

import org.eclipse.paho.client.mqttv3.MqttException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.AddMokoSensorActivity
 */
public class AddMokoSensorActivity extends BaseActivity {


    @Bind(R.id.not_blinking_tips)
    TextView notBlinkingTips;
    private CustomDialog wifiAlertDialog;
    private CustomDialog mqttConnDialog;
    private DonutProgress donutProgress;
    private SocketService mService;
    private String mWifiSSID;
    private String mWifiPassword;
    private DeviceResult mDeviceResult;
    private MQTTConfig mDeviceMqttConfig;
    private MQTTConfig mAppMqttConfig;
    private boolean isSettingSuccess;
    private String mTopicPre;
    private boolean isDeviceConnectSuccess;
    private boolean mIsGPRS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        ButterKnife.bind(this);
        notBlinkingTips.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        notBlinkingTips.getPaint().setAntiAlias(true);//抗锯齿
        String mqttConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG, "");
        mDeviceMqttConfig = new Gson().fromJson(mqttConfigStr, MQTTConfig.class);
        String mqttConfigAppStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        mAppMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
        bindService(new Intent(this, SocketService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogModule.i("连接服务onServiceConnected...");
            mService = ((SocketService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_AP_CONNECTION);
            filter.addAction(MokoConstants.ACTION_AP_SET_DATA_RESPONSE);
            filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
            filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
            registerReceiver(mReceiver, filter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogModule.i("断开服务onServiceDisconnected...");
            // mMokoService = null;
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_AP_CONNECTION.equals(action)) {
                int status = intent.getIntExtra(MokoConstants.EXTRA_AP_CONNECTION, -1);
                if (status == MokoConstants.CONN_STATUS_SUCCESS) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("header", MokoConstants.HEADER_GET_DEVICE_INFO);
                            mService.sendMessage(jsonObject.toString());
                        }
                    }).start();
                } else {
                    dismissLoadingProgressDialog();
                }
            }
            if (MokoConstants.ACTION_AP_SET_DATA_RESPONSE.equals(action)) {
                DeviceResponse response = (DeviceResponse) intent.getSerializableExtra(MokoConstants.EXTRA_AP_SET_DATA_RESPONSE);
                if (response.code == MokoConstants.RESPONSE_SUCCESS) {
                    switch (response.result.header) {
                        case MokoConstants.HEADER_GET_DEVICE_INFO:
                            mDeviceResult = response.result;
                            mTopicPre = mDeviceResult.device_function
                                    + "/" + mDeviceResult.device_name
                                    + "/" + mDeviceResult.device_specifications
                                    + "/" + mDeviceResult.device_mac
                                    + "/" + "device"
                                    + "/";
                            // 获取设备信息，设置MQTT信息
                            final JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("header", MokoConstants.HEADER_SET_MQTT_INFO);
                            jsonObject.addProperty("host", mDeviceMqttConfig.host);
                            jsonObject.addProperty("port", Integer.parseInt(mDeviceMqttConfig.port));
                            jsonObject.addProperty("connect_mode", mDeviceMqttConfig.connectMode);
                            jsonObject.addProperty("username", mDeviceMqttConfig.username);
                            jsonObject.addProperty("password", mDeviceMqttConfig.password);
                            jsonObject.addProperty("keepalive", mDeviceMqttConfig.keepAlive);
                            jsonObject.addProperty("qos", mDeviceMqttConfig.qos);
                            jsonObject.addProperty("clean_session", mDeviceMqttConfig.cleanSession ? 1 : 0);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mService.sendMessage(jsonObject.toString());
                                }
                            }).start();
                            break;
                        case MokoConstants.HEADER_SET_MQTT_INFO:
                            // 设置上报通道
                            final JsonObject channel = new JsonObject();
                            channel.addProperty("header", MokoConstants.HEADER_SET_CHANNEL);
                            channel.addProperty("mqtt_channel", mIsGPRS ? 2 : 1);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mService.sendMessage(channel.toString());
                                }
                            }).start();
                            break;
                        case MokoConstants.HEADER_SET_CHANNEL:
                            if (mIsGPRS) {
                                // 设置成功，保存数据，网络可用后订阅mqtt主题
                                isSettingSuccess = true;
                            } else {
                                // 获取MQTT信息，设置WIFI信息
                                final JsonObject wifiInfo = new JsonObject();
                                wifiInfo.addProperty("header", MokoConstants.HEADER_SET_WIFI_INFO);
                                wifiInfo.addProperty("wifi_ssid", mWifiSSID);
                                wifiInfo.addProperty("wifi_pwd", mWifiPassword);
                                wifiInfo.addProperty("wifi_security", 3);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mService.sendMessage(wifiInfo.toString());
                                    }
                                }).start();
                            }
                            break;
                        case MokoConstants.HEADER_SET_WIFI_INFO:
                            // 设置成功，保存数据，网络可用后订阅mqtt主题
                            isSettingSuccess = true;
                            break;
                    }
                } else {
                    ToastUtils.showToast(AddMokoSensorActivity.this, response.message);
                }
            }
            if (action.equals(MokoConstants.ACTION_MQTT_CONNECTION)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
                if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS && isSettingSuccess) {
                    LogModule.i("连接MQTT成功");
                    // 订阅设备主题
                    String topicSwitchState = mTopicPre + "heartbeat";
                    String topicDelayTime = mTopicPre + "device_info";
                    String topicDeleteDevice = mTopicPre + "delete_device";
                    String topicElectricityInfo = mTopicPre + "sensor_data";
                    // 订阅
                    try {
                        MokoSupport.getInstance().subscribe(topicSwitchState, mAppMqttConfig.qos);
                        MokoSupport.getInstance().subscribe(topicDelayTime, mAppMqttConfig.qos);
                        MokoSupport.getInstance().subscribe(topicDeleteDevice, mAppMqttConfig.qos);
                        MokoSupport.getInstance().subscribe(topicElectricityInfo, mAppMqttConfig.qos);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (action.equals(MokoConstants.ACTION_MQTT_RECEIVE)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (TextUtils.isEmpty(topic) || TextUtils.isEmpty(mTopicPre) || isDeviceConnectSuccess) {
                    return;
                }
                if (topic.contains(mTopicPre) && !isDeviceConnectSuccess) {
                    isDeviceConnectSuccess = true;
                    donutProgress.setProgress(100);
                    donutProgress.setText(100 + "%");
                    // 关闭进度条弹框，保存数据，跳转修改设备名称页面
                    notBlinkingTips.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissConnMqttDialog();
                            MokoDevice mokoDevice = DBTools.getInstance(AddMokoSensorActivity.this).selectDevice(mDeviceResult.device_mac);
                            if (mokoDevice == null) {
                                mokoDevice = new MokoDevice();
                                mokoDevice.name = mDeviceResult.device_name;
                                mokoDevice.nickName = mDeviceResult.device_specifications;
                                mokoDevice.specifications = mDeviceResult.device_specifications;
                                mokoDevice.function = mDeviceResult.device_function;
                                mokoDevice.mac = mDeviceResult.device_mac;
                                mokoDevice.gprs = mDeviceResult.device_GPRS;
                                mokoDevice.type = new Gson().toJson(mDeviceResult.device_type);
                                DBTools.getInstance(AddMokoSensorActivity.this).insertDevice(mokoDevice);
                            } else {
                                mokoDevice.name = mDeviceResult.device_name;
                                mokoDevice.specifications = mDeviceResult.device_specifications;
                                mokoDevice.function = mDeviceResult.device_function;
                                mokoDevice.type = new Gson().toJson(mDeviceResult.device_type);
                                DBTools.getInstance(AddMokoSensorActivity.this).updateDevice(mokoDevice);
                            }
                            Intent modifyIntent = new Intent(AddMokoSensorActivity.this, ModifyNameActivity.class);
                            modifyIntent.putExtra("mokodevice", mokoDevice);
                            startActivity(modifyIntent);
                        }
                    }, 500);
                }
            }
        }
    };

    public void back(View view) {
        finish();
    }

    /**
     * @Date 2018/6/12
     * @Author wenzheng.liu
     * @Description 查看打开AP步骤
     * @ClassPath com.moko.sensor.activity.AddMokoPlugActivity
     */
    public void notBlinking(View view) {
        startActivityForResult(new Intent(this, OperationSensorStepsActivity.class), AppConstants.REQUEST_CODE_OPERATION_STEP);
    }

    /**
     * @Date 2018/6/12
     * @Author wenzheng.liu
     * @Description 判断是否连接设备wifi
     */
    public void sensorBlinking(View view) {
        isDeviceConnectSuccess = false;
        checkWifiInfo();
    }

    private void checkWifiInfo() {
        if (!isWifiCorrect()) {
            View wifiAlertView = LayoutInflater.from(this).inflate(R.layout.wifi_setting_content, null);
            ImageView iv_wifi_alert = ButterKnife.findById(wifiAlertView, R.id.iv_wifi_alert);
            iv_wifi_alert.setImageResource(R.drawable.sensor_wifi_alert);
            wifiAlertDialog = new CustomDialog.Builder(this)
                    .setContentView(wifiAlertView)
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转系统WIFI页面
                            Intent intent = new Intent();
                            intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                            startActivityForResult(intent, AppConstants.REQUEST_CODE_WIFI_SETTING);
                        }
                    })
                    .create();
            wifiAlertDialog.show();
        } else {
            // 如果热点名称为ED001W-XXXX， 则设备只有WIFI模组； 如果热点名称为ED001G-XXXX， 则设备同时具有WIFI和GPRS模组；
            String ssid = Utils.getWifiSSID(this);
            if (ssid.contains("ED001W-")) {
                // 弹出输入WIFI弹框
                showWifiInputDialog();
            } else {
                // 选择模组
                startActivityForResult(new Intent(this, SelectWorkModeActivity.class), AppConstants.REQUEST_CODE_SELECT_WORK_MODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_WIFI_SETTING) {
            if (isWifiCorrect()) {
                // 弹出输入WIFI弹框
                if (wifiAlertDialog != null && !isFinishing() && wifiAlertDialog.isShowing()) {
                    wifiAlertDialog.dismiss();
                }
                // 如果热点名称为ED001W-XXXX， 则设备只有WIFI模组； 如果热点名称为ED001G-XXXX， 则设备同时具有WIFI和GPRS模组；
                String ssid = Utils.getWifiSSID(this);
                if (ssid.contains("ED001-W")) {
                    showWifiInputDialog();
                } else if (ssid.contains("ED001-GW")) {
                    // 选择模组
                    startActivityForResult(new Intent(this, SelectWorkModeActivity.class), AppConstants.REQUEST_CODE_SELECT_WORK_MODE);
                }
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_OPERATION_STEP) {
            if (resultCode == RESULT_OK) {
                checkWifiInfo();
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_SELECT_WORK_MODE) {
            if (resultCode == RESULT_OK) {
                int mode = data.getIntExtra("work_mode", 1);
                if (mode == 1) {
                    // GPRS
                    mIsGPRS = true;
                    showLoadingProgressDialog(getString(R.string.wait));
                    notBlinkingTips.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismissLoadingProgressDialog();
                            if (isWifiCorrect()) {
                                // 弹出加载弹框
                                showConnMqttDialog();
                                // 连接设备
                                mService.startSocket();
                            }
                        }
                    }, 2000);
                } else {
                    mIsGPRS = false;
                    showWifiInputDialog();
                }
            }
        }
    }

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.equals(" ") || source.toString().contentEquals("\n")) return "";
            else return null;
        }
    };

    private void showWifiInputDialog() {
        View wifiInputView = LayoutInflater.from(this).inflate(R.layout.wifi_input_content, null);
        final EditText etSSID = ButterKnife.findById(wifiInputView, R.id.et_ssid);
        etSSID.setFilters(new InputFilter[]{filter});
        final EditText etPassword = ButterKnife.findById(wifiInputView, R.id.et_password);
        CustomDialog dialog = new CustomDialog.Builder(this)
                .setContentView(wifiInputView)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWifiSSID = etSSID.getText().toString();
                        // 获取WIFI后，连接成功后发给设备
                        if (TextUtils.isEmpty(mWifiSSID)) {
                            ToastUtils.showToast(AddMokoSensorActivity.this, getString(R.string.wifi_verify_empty));
                            return;
                        }
                        dialog.dismiss();
                        showLoadingProgressDialog(getString(R.string.wait));
                        notBlinkingTips.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissLoadingProgressDialog();
                                if (isWifiCorrect()) {
                                    mWifiPassword = etPassword.getText().toString();
                                    // 弹出加载弹框
                                    showConnMqttDialog();
                                    // 连接设备
                                    mService.startSocket();
                                }
                            }
                        }, 2000);
                    }
                })
                .create();
        dialog.show();
    }

    private int progress;

    private void showConnMqttDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.mqtt_conn_content, null);
        donutProgress = ButterKnife.findById(view, R.id.dp_progress);
        mqttConnDialog = new CustomDialog.Builder(this)
                .setContentView(view)
                .create();
        mqttConnDialog.setCancelable(false);
        mqttConnDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                progress = 0;
                while (progress <= 100 && !isDeviceConnectSuccess) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            donutProgress.setProgress(progress);
                            donutProgress.setText(progress + "%");
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress++;
                }
            }
        }).start();
        notBlinkingTips.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDeviceConnectSuccess) {
                    isDeviceConnectSuccess = true;
                    dismissConnMqttDialog();
                    ToastUtils.showToast(AddMokoSensorActivity.this, getString(R.string.mqtt_connecting_timeout));
                }
            }
        }, 90 * 1000);
    }

    private void dismissConnMqttDialog() {
        if (mqttConnDialog != null && !isFinishing() && mqttConnDialog.isShowing()) {
            mqttConnDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    public boolean isWifiCorrect() {
        String ssid = Utils.getWifiSSID(this);
        if (TextUtils.isEmpty(ssid) || !ssid.startsWith("\"ED")) {
            return false;
        } else {
            return true;
        }
    }
}
