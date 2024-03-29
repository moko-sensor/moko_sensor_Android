package com.moko.sensor.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.sensor.AppConstants;
import com.moko.sensor.R;
import com.moko.sensor.adapter.DeviceAdapter;
import com.moko.sensor.base.BaseActivity;
import com.moko.sensor.db.DBTools;
import com.moko.sensor.entity.MQTTConfig;
import com.moko.sensor.entity.MokoDevice;
import com.moko.sensor.entity.SensorData;
import com.moko.sensor.service.MokoService;
import com.moko.sensor.utils.SPUtiles;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.DeviceType;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description 设备列表
 * @ClassPath com.moko.sensor.activity.MainActivity
 */
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.cl_empty)
    ConstraintLayout clEmpty;
    @Bind(R.id.lv_device_list)
    ListView lvDeviceList;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    private ArrayList<MokoDevice> devices;
    private DeviceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        devices = DBTools.getInstance(this).selectAllDevice();
        adapter = new DeviceAdapter(this);
        adapter.setItems(devices);
        lvDeviceList.setAdapter(adapter);
        lvDeviceList.setOnItemClickListener(this);
        if (devices.isEmpty()) {
            clEmpty.setVisibility(View.VISIBLE);
            lvDeviceList.setVisibility(View.GONE);
        } else {
            lvDeviceList.setVisibility(View.VISIBLE);
            clEmpty.setVisibility(View.GONE);
        }
        mHandler = new OfflineHandler(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_SUBSCRIBE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        registerReceiver(mReceiver, filter);
        startService(new Intent(this, MokoService.class));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
                String title = "";
                if (state == MokoConstants.MQTT_CONN_STATUS_LOST) {
                    title = getString(R.string.mqtt_connecting);
                } else if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS) {
                    title = getString(R.string.guide_center);
                } else if (state == MokoConstants.MQTT_CONN_STATUS_FAILED) {
                    title = getString(R.string.mqtt_connect_failed);
                }
                tvTitle.setText(title);
                if (state == MokoConstants.MQTT_CONN_STATUS_SUCCESS) {
                    if (devices.isEmpty()) {
                        return;
                    }
                    for (MokoDevice device : devices) {
                        String mqttConfigAppStr = SPUtiles.getStringValue(MainActivity.this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
                        MQTTConfig appMqttConfig = new Gson().fromJson(mqttConfigAppStr, MQTTConfig.class);
                        // 订阅
                        for (String topic : device.getDeviceTopics()) {
                            try {
                                MokoSupport.getInstance().subscribe(topic, appMqttConfig.qos);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_SUBSCRIBE.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                dismissLoadingProgressDialog();
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                final String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (devices.isEmpty()) {
                    return;
                }
                if (topic.contains(MokoDevice.DEVICE_TOPIC_DEVICE_HEART_BEAT)) {
                    for (final MokoDevice device : devices) {
                        if (device.getDeviceTopicDeviceHeartBeat().equals(topic)) {
                            device.isOnline = true;
                            if (mHandler.hasMessages(device.id)) {
                                mHandler.removeMessages(device.id);
                            }
                            Message message = Message.obtain(mHandler, new Runnable() {
                                @Override
                                public void run() {
                                    device.isOnline = false;
                                    LogModule.i(device.mac + "离线");
                                    adapter.notifyDataSetChanged();
                                    Intent i = new Intent(AppConstants.ACTION_DEVICE_STATE);
                                    i.putExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC, topic);
                                    MainActivity.this.sendBroadcast(i);
                                }
                            });
                            message.what = device.id;
                            mHandler.sendMessageDelayed(message, 62 * 1000);
                            adapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
                if (topic.contains(MokoDevice.DEVICE_TOPIC_DEVICE_SENSOR_DATA)) {
                    String receive = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                    SensorData sensorData = new Gson().fromJson(receive, SensorData.class);
                    for (final MokoDevice device : devices) {
                        if (device.getDeviceTopicDeviceSensorData().equals(topic)) {
                            DeviceType deviceType = new Gson().fromJson(device.type, DeviceType.class);
                            if (deviceType.env_temp == 1) {
                                device.temperature = sensorData.env_temp;
                            }
                            if (deviceType.humidity == 1) {
                                device.humidity = sensorData.humidity;
                            }
                            if (deviceType.PM2_5 == 1) {
                                device.pm2_5 = sensorData.PM2_5;
                            }
                            if (deviceType.NH3 == 1) {
                                device.nh3 = sensorData.NH3;
                            }
                            if (deviceType.CO2 == 1) {
                                device.co2 = sensorData.CO2;
                            }
                            if (deviceType.distance == 1) {
                                device.laser_ranging = sensorData.distance;
                            }
                            if (deviceType.illumination == 1) {
                                device.illumination = sensorData.illumination;
                            }
                            if (deviceType.VOC == 1) {
                                device.voc = sensorData.VOC;
                            }
                            if (deviceType.infra_red_temp == 1) {
                                device.infra_red_temp = sensorData.infra_red_temp;
                            }
                            break;
                        }
                    }
                }
            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                devices.clear();
                devices.addAll(DBTools.getInstance(MainActivity.this).selectAllDevice());
                adapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogModule.i("onNewIntent...");
        setIntent(intent);
        if (getIntent().getExtras() != null) {
            String from = getIntent().getStringExtra(AppConstants.EXTRA_KEY_FROM_ACTIVITY);
            if (ModifyNameActivity.TAG.equals(from)
                    || MoreActivity.TAG.equals(from)) {
                devices.clear();
                devices.addAll(DBTools.getInstance(this).selectAllDevice());
                adapter.notifyDataSetChanged();
                if (!devices.isEmpty()) {
                    lvDeviceList.setVisibility(View.VISIBLE);
                    clEmpty.setVisibility(View.GONE);
                } else {
                    lvDeviceList.setVisibility(View.GONE);
                    clEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(new Intent(this, MokoService.class));
    }

    public void mainSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void mainAddDevices(View view) {
        String mqttAppConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        String mqttDeviceConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG, "");
        if (TextUtils.isEmpty(mqttDeviceConfigStr)) {
            startActivity(new Intent(this, SetDeviceMqttActivity.class));
            return;
        }
        if (TextUtils.isEmpty(mqttAppConfigStr)) {
            startActivity(new Intent(this, SetAppMqttActivity.class));
            return;
        }
        MQTTConfig mqttConfig = new Gson().fromJson(mqttAppConfigStr, MQTTConfig.class);
        if (TextUtils.isEmpty(mqttConfig.host)) {
            startActivity(new Intent(this, SetAppMqttActivity.class));
            return;
        }
        startActivity(new Intent(this, SelectDeviceTypeActivity.class));
    }

    public OfflineHandler mHandler;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogModule.i("跳转详情");
        MokoDevice device = (MokoDevice) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, MokoSensorDetailActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, device);
        startActivity(intent);
    }

    public class OfflineHandler extends BaseMessageHandler<MainActivity> {

        public OfflineHandler(MainActivity activity) {
            super(activity);
        }

        @Override
        protected void handleMessage(MainActivity activity, Message msg) {
        }
    }
}
