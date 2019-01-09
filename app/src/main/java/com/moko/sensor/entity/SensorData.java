package com.moko.sensor.entity;

import java.io.Serializable;

public class SensorData implements Serializable {
    public int humidity;
    public int env_temp;
    public int PM2_5;
    public int NH3;
    public int CO2;
    public int distance;
    public int illumination;
    public int VOC;
}