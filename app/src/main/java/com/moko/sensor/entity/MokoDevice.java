package com.moko.sensor.entity;


import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

public class MokoDevice implements Serializable {
    public static final String DEVICE_TOPIC_OTA_UPGRADE_STATE = "device/ota_upgrade_state";
    public static final String DEVICE_TOPIC_DELETE_DEVICE = "device/delete_device";

    public static final String DEVICE_TOPIC_DEVICE_INFO = "device/device_info";
    public static final String DEVICE_TOPIC_DEVICE_HEART_BEAT = "device/heartbeat";
    public static final String DEVICE_TOPIC_DEVICE_SENSOR_DATA = "device/sensor_data";

    public static final String APP_TOPIC_RESET = "app/reset";
    public static final String APP_TOPIC_UPGRADE = "app/upgrade";
    public static final String APP_TOPIC_READ_FIRMWARE_INFOR = "app/read_firmware_infor";

    public int id;
    public String name;
    public String nickName;
    public String function;
    public String specifications;
    public String mac;
    public String sim;
    public int gprs;
    public String type;
    public String topicPre;
    public String company_name;
    public String production_date;
    public String product_model;
    public String firmware_version;
    public boolean isOnline;

    public int temperature;
    public int humidity;
    public int nh3;
    public int co2;
    public int illumination;
    public int pm2_5;
    public int voc;
    public int laser_ranging;

    public boolean isSensorDataEmpty() {
        if (temperature == 0 && humidity == 0 && nh3 == 0
                && co2 == 0 && illumination == 0 && pm2_5 == 0
                && voc == 0 && laser_ranging == 0) {
            return true;
        }
        return false;
    }

    public ArrayList<String> subscribeTopics;

    public String getTopicPre() {
        if (TextUtils.isEmpty(topicPre)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.function);
            stringBuilder.append("/");
            stringBuilder.append(this.name);
            stringBuilder.append("/");
            stringBuilder.append(this.specifications);
            stringBuilder.append("/");
            stringBuilder.append(this.mac);
            stringBuilder.append("/");
            topicPre = stringBuilder.toString();
        }
        return topicPre;
    }

    public ArrayList<String> getDeviceTopics() {
        if (subscribeTopics == null) {
            subscribeTopics = new ArrayList<>();
            subscribeTopics.add(getDeviceTopicDeviceSensorData());
            subscribeTopics.add(getDeviceTopicDeviceHeartBeat());
            subscribeTopics.add(getDeviceTopicDeleteDevice());
        }
        return subscribeTopics;
    }

    public String getDeviceTopicDeviceInfo() {
        return getTopicPre() + DEVICE_TOPIC_DEVICE_INFO;
    }

    public String getDeviceTopicDeviceHeartBeat() {
        return getTopicPre() + DEVICE_TOPIC_DEVICE_HEART_BEAT;
    }

    public String getDeviceTopicDeviceSensorData() {
        return getTopicPre() + DEVICE_TOPIC_DEVICE_SENSOR_DATA;
    }

    public String getDeviceTopicUpgradeState() {
        return getTopicPre() + DEVICE_TOPIC_OTA_UPGRADE_STATE;
    }

    public String getDeviceTopicDeleteDevice() {
        return getTopicPre() + DEVICE_TOPIC_DELETE_DEVICE;
    }

    public String getAppTopicReset() {
        return getTopicPre() + APP_TOPIC_RESET;
    }

    public String getAppTopicUpgrade() {
        return getTopicPre() + APP_TOPIC_UPGRADE;
    }

    public String getAppTopicReadFirmwareInfor() {
        return getTopicPre() + APP_TOPIC_READ_FIRMWARE_INFOR;
    }
}
