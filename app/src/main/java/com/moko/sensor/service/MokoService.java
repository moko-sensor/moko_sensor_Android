package com.moko.sensor.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.moko.sensor.AppConstants;
import com.moko.sensor.entity.MQTTConfig;
import com.moko.sensor.utils.SPUtiles;
import com.moko.support.MokoSupport;
import com.moko.support.log.LogModule;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.sensor.service.MokoService
 */
public class MokoService extends Service {
    private IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogModule.i("启动后台服务");
        String mqttAppConfigStr = SPUtiles.getStringValue(this, AppConstants.SP_KEY_MQTT_CONFIG_APP, "");
        if (!TextUtils.isEmpty(mqttAppConfigStr)) {
            MQTTConfig mqttConfig = new Gson().fromJson(mqttAppConfigStr, MQTTConfig.class);
            if (!mqttConfig.isError(null)) {
                MokoSupport.getInstance().creatClient(mqttConfig.host, mqttConfig.port, mqttConfig.clientId, mqttConfig.connectMode == 1);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setAutomaticReconnect(true);
                connOpts.setCleanSession(mqttConfig.cleanSession);
                connOpts.setKeepAliveInterval(mqttConfig.keepAlive);
                connOpts.setUserName(mqttConfig.username);
                connOpts.setPassword(mqttConfig.password.toCharArray());
                if (mqttConfig.connectMode == 1) {
                    TrustManager[] trustAllCerts = new TrustManager[1];
                    TrustManager tm = new AllTM();
                    trustAllCerts[0] = tm;
                    SSLContext sc = null;
                    try {
                        sc = SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts, null);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }
                    SocketFactory factory = sc.getSocketFactory();
                    connOpts.setSocketFactory(factory);
                }
                try {
                    MokoSupport.getInstance().connectMqtt(connOpts);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        LogModule.i("关闭后台服务");
        super.onDestroy();
        try {
            MokoSupport.getInstance().disconnectMqtt();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    static class AllTM implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            LogModule.i("isServerTrusted");
            for (java.security.cert.X509Certificate certificate : certs) {
                LogModule.w("Accepting:" + certificate);
            }
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            LogModule.i("isClientTrusted");
            for (java.security.cert.X509Certificate certificate : certs) {
                LogModule.w("Accepting:" + certificate);
            }
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            LogModule.i("Server authtype=" + authType);
            for (java.security.cert.X509Certificate certificate : certs) {
                LogModule.w("Accepting:" + certificate);
            }
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
            LogModule.i("Client authtype=" + authType);
            for (java.security.cert.X509Certificate certificate : certs) {
                LogModule.w("Accepting:" + certificate);
            }
            return;
        }
    }
}
