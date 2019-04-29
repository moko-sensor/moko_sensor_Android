package com.moko.sensor.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.moko.sensor.AppConstants;
import com.moko.sensor.R;
import com.moko.sensor.base.BaseActivity;
import com.moko.sensor.db.DBTools;
import com.moko.sensor.entity.MokoDevice;
import com.moko.sensor.entity.SensorData;
import com.moko.support.MokoConstants;
import com.moko.support.entity.DeviceType;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2018/6/7
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.activity.MokoSensorDetailActivity
 */
public class MokoSensorDetailActivity extends BaseActivity {

    @Bind(R.id.rl_title)
    RelativeLayout rlTitle;
    @Bind(R.id.tv_temperature)
    TextView tvTemperature;
    @Bind(R.id.rl_temperature)
    RelativeLayout rlTemperature;
    @Bind(R.id.tv_humidity)
    TextView tvHumidity;
    @Bind(R.id.rl_humidity)
    RelativeLayout rlHumidity;
    @Bind(R.id.tv_nh3)
    TextView tvNh3;
    @Bind(R.id.rl_nh3)
    RelativeLayout rlNh3;
    @Bind(R.id.tv_co2)
    TextView tvCo2;
    @Bind(R.id.rl_co2)
    RelativeLayout rlCo2;
    @Bind(R.id.tv_illumination)
    TextView tvIllumination;
    @Bind(R.id.rl_illumination)
    RelativeLayout rlIllumination;
    @Bind(R.id.tv_pm2_5)
    TextView tvPm25;
    @Bind(R.id.rl_pm2_5)
    RelativeLayout rlPm25;
    @Bind(R.id.tv_voc)
    TextView tvVoc;
    @Bind(R.id.rl_voc)
    RelativeLayout rlVoc;
    @Bind(R.id.tv_laser_ranging)
    TextView tvLaserRanging;
    @Bind(R.id.rl_laser_ranging)
    RelativeLayout rlLaserRanging;
    @Bind(R.id.tv_infra_red_temp)
    TextView tvInfraRedTemp;
    @Bind(R.id.rl_infra_red_temp)
    RelativeLayout rlInfraRedTemp;
    private MokoDevice mokoDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moko_sensor_detail);
        ButterKnife.bind(this);
        if (getIntent().getExtras() != null) {
            mokoDevice = (MokoDevice) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_DEVICE);
            DeviceType deviceType = new Gson().fromJson(mokoDevice.type, DeviceType.class);
            if (deviceType.env_temp == 1) {
                rlTemperature.setVisibility(View.VISIBLE);
                if (mokoDevice.temperature == 0xffff) {
                    tvTemperature.setTextColor(Color.RED);
                    tvTemperature.setText("--");
                } else {
                    tvTemperature.setText(new DecimalFormat("0.0").format(0.1f * mokoDevice.temperature) + "℃");
                }
            }
            if (deviceType.humidity == 1) {
                rlHumidity.setVisibility(View.VISIBLE);
                if (mokoDevice.humidity == 0xffff) {
                    tvHumidity.setTextColor(Color.RED);
                    tvHumidity.setText("--");
                } else {
                    tvHumidity.setText(new DecimalFormat("0.0").format(0.1f * mokoDevice.humidity) + "%RH");
                }
            }
            if (deviceType.PM2_5 == 1) {
                rlPm25.setVisibility(View.VISIBLE);
                if (mokoDevice.pm2_5 == 0xffff) {
                    tvPm25.setTextColor(Color.RED);
                    tvPm25.setText("--");
                } else {
                    tvPm25.setText(mokoDevice.pm2_5 + "μg/m³");
                }
            }
            if (deviceType.NH3 == 1) {
                rlNh3.setVisibility(View.VISIBLE);
                if (mokoDevice.nh3 == 0xffff) {
                    tvNh3.setTextColor(Color.RED);
                    tvNh3.setText("--");
                } else {
                    tvNh3.setText(mokoDevice.nh3 + "ppm");
                }
            }
            if (deviceType.CO2 == 1) {
                rlCo2.setVisibility(View.VISIBLE);
                if (mokoDevice.co2 == 0xffff) {
                    tvCo2.setTextColor(Color.RED);
                    tvCo2.setText("--");
                } else {
                    tvCo2.setText(mokoDevice.co2 + "ppm");
                }
            }
            if (deviceType.distance == 1) {
                rlLaserRanging.setVisibility(View.VISIBLE);
                if (mokoDevice.laser_ranging == 0xffff || mokoDevice.laser_ranging == 0xfffe) {
                    tvLaserRanging.setTextColor(Color.RED);
                    tvLaserRanging.setText("--");
                } else {
                    tvLaserRanging.setText(mokoDevice.laser_ranging + "cm");
                }
            }
            if (deviceType.illumination == 1) {
                rlIllumination.setVisibility(View.VISIBLE);
                if (mokoDevice.illumination == 0xffff) {
                    tvIllumination.setTextColor(Color.RED);
                    tvIllumination.setText("--");
                } else {
                    tvIllumination.setText(mokoDevice.illumination + "LX");
                }
            }
            if (deviceType.VOC == 1) {
                rlVoc.setVisibility(View.VISIBLE);
                if (mokoDevice.voc == 0xffff) {
                    tvVoc.setTextColor(Color.RED);
                    tvVoc.setText("--");
                } else {
                    tvVoc.setText(mokoDevice.voc + "ppm");
                }
            }
            if (deviceType.infra_red_temp == 1) {
                rlInfraRedTemp.setVisibility(View.VISIBLE);
                if (mokoDevice.infra_red_temp == 0xffff) {
                    tvInfraRedTemp.setTextColor(Color.RED);
                    tvInfraRedTemp.setText("--");
                } else {
                    tvInfraRedTemp.setText(new DecimalFormat("0.0").format(0.1f * mokoDevice.infra_red_temp) + "℃");
                }
            }
            if (mokoDevice.isSensorDataEmpty()) {
                showLoadingProgressDialog(getString(R.string.wait));
            }
        }
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(MokoConstants.ACTION_MQTT_CONNECTION);
        filter.addAction(MokoConstants.ACTION_MQTT_RECEIVE);
        filter.addAction(MokoConstants.ACTION_MQTT_PUBLISH);
        filter.addAction(AppConstants.ACTION_MODIFY_NAME);
        filter.addAction(AppConstants.ACTION_DEVICE_STATE);
        registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MokoConstants.ACTION_MQTT_CONNECTION.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_CONNECTION_STATE, 0);
            }
            if (MokoConstants.ACTION_MQTT_RECEIVE.equals(action)) {
                dismissLoadingProgressDialog();
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mokoDevice.getDeviceTopicDeviceSensorData())) {
                    mokoDevice.isOnline = true;
                    String message = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_MESSAGE);
                    SensorData sensorData = new Gson().fromJson(message, SensorData.class);
                    if (rlTemperature.getVisibility() == View.VISIBLE) {
                        if (sensorData.env_temp == 0xffff) {
                            tvTemperature.setTextColor(Color.RED);
                            tvTemperature.setText("--");
                        } else {
                            tvTemperature.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvTemperature.setText(new DecimalFormat("0.0").format(0.1f * sensorData.env_temp) + "℃");
                        }
                    }
                    if (rlHumidity.getVisibility() == View.VISIBLE) {
                        if (sensorData.humidity == 0xffff) {
                            tvHumidity.setTextColor(Color.RED);
                            tvHumidity.setText("--");
                        } else {
                            tvHumidity.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvHumidity.setText(new DecimalFormat("0.0").format(0.1f * sensorData.humidity) + "%RH");
                        }
                    }
                    if (rlPm25.getVisibility() == View.VISIBLE) {
                        if (sensorData.PM2_5 == 0xffff) {
                            tvPm25.setTextColor(Color.RED);
                            tvPm25.setText("--");
                        } else {
                            tvPm25.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvPm25.setText(sensorData.PM2_5 + "μg/m³");
                        }
                    }
                    if (rlNh3.getVisibility() == View.VISIBLE) {
                        if (sensorData.NH3 == 0xffff) {
                            tvNh3.setTextColor(Color.RED);
                            tvNh3.setText("--");
                        } else {
                            tvNh3.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvNh3.setText(sensorData.NH3 + "ppm");
                        }
                    }
                    if (rlCo2.getVisibility() == View.VISIBLE) {
                        if (sensorData.CO2 == 0xffff) {
                            tvCo2.setTextColor(Color.RED);
                            tvCo2.setText("--");
                        } else {
                            tvCo2.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvCo2.setText(sensorData.CO2 + "ppm");
                        }
                    }
                    if (rlLaserRanging.getVisibility() == View.VISIBLE) {
                        if (sensorData.distance == 0xffff || sensorData.distance == 0xfffe) {
                            tvLaserRanging.setTextColor(Color.RED);
                            tvLaserRanging.setText("--");
                        } else {
                            tvLaserRanging.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvLaserRanging.setText(sensorData.distance + "cm");
                        }
                    }
                    if (rlIllumination.getVisibility() == View.VISIBLE) {
                        if (sensorData.illumination == 0xffff) {
                            tvIllumination.setTextColor(Color.RED);
                            tvIllumination.setText("--");
                        } else {
                            tvIllumination.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvIllumination.setText(sensorData.illumination + "LX");
                        }
                    }
                    if (rlVoc.getVisibility() == View.VISIBLE) {
                        if (sensorData.VOC == 0xffff) {
                            tvVoc.setTextColor(Color.RED);
                            tvVoc.setText("--");
                        } else {
                            tvVoc.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvVoc.setText(sensorData.VOC + "ppm");
                        }
                    }
                    if (rlInfraRedTemp.getVisibility() == View.VISIBLE) {
                        if (sensorData.infra_red_temp == 0xffff) {
                            tvInfraRedTemp.setTextColor(Color.RED);
                            tvInfraRedTemp.setText("--");
                        } else {
                            tvInfraRedTemp.setTextColor(ContextCompat.getColor(MokoSensorDetailActivity.this, R.color.grey_808080));
                            tvInfraRedTemp.setText(new DecimalFormat("0.0").format(0.1f * sensorData.infra_red_temp) + "℃");
                        }
                    }
                }
            }
            if (MokoConstants.ACTION_MQTT_PUBLISH.equals(action)) {
                int state = intent.getIntExtra(MokoConstants.EXTRA_MQTT_STATE, 0);
                dismissLoadingProgressDialog();
            }
            if (AppConstants.ACTION_DEVICE_STATE.equals(action)) {
                String topic = intent.getStringExtra(MokoConstants.EXTRA_MQTT_RECEIVE_TOPIC);
                if (topic.equals(mokoDevice.getDeviceTopicDeviceHeartBeat())) {
                    mokoDevice.isOnline = false;
                }
            }
            if (AppConstants.ACTION_MODIFY_NAME.equals(action)) {
                MokoDevice device = DBTools.getInstance(MokoSensorDetailActivity.this).selectDevice(mokoDevice.mac);
                mokoDevice.nickName = device.nickName;
            }
        }
    };

    public void back(View view) {
        finish();
    }

    public void more(View view) {
        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_DEVICE, mokoDevice);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
