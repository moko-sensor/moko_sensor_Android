package com.moko.support.entity;


import java.io.Serializable;

public class DeviceResult implements Serializable {
    public int header;
    public String device_function;
    public String device_name;
    public String device_specifications;
    public String device_mac;
    public int device_GPRS;
    public DeviceType device_type;
}
