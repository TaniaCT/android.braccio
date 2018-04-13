package com.upcstudios.tania.braccioapp3;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;


public class GlobalClasses extends Application {

    public BluetoothClass MyBluetooth;
    public List<String> CommandArray = new ArrayList<>();
    public String Code = "";
    public boolean ArduinoFree = true;

    @Override
    public void onCreate() {
        super.onCreate();
        MyBluetooth = new BluetoothClass(getApplicationContext());
    }
}
