package com.upcstudios.tania.braccioapp3;

import android.app.Application;

/**
 * Created by tania on 13/11/2017.
 */

public class GlobalClasses extends Application {
    public BluetoothClass MyBluetooth;

    @Override
    public void onCreate() {
        super.onCreate();
        MyBluetooth = new BluetoothClass(getApplicationContext());
    }
}